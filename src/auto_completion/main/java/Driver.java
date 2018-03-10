package auto_completion.main.java;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.db.DBConfiguration;
import org.apache.hadoop.mapreduce.lib.db.DBOutputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @author yifengguo
 */
public class Driver {
    public static void main(String[] args) throws IOException, InterruptedException, SQLException,
    ClassNotFoundException{
        // MapReduce 1 parameters
        // inputDir
        // outputDir
        // noGram

        //MapReduce 2 parameters
        // threshold  filter out rare count cases in N-Gram Library
        // topK       only insert records with top K freq into Database

        String inputDir = args[0];
        String outputDir = args[1];
        String noGram = args[2];

        String threshold = args[3];
        String topK = args[4];

        // Initialize configuration of MapReduce 1
        Configuration conf1 = new Configuration();
        conf1.set("textinputformat.record.delimiter", ".");
        conf1.set("noGram", noGram);

        Job job1 = Job.getInstance();
        job1.setJobName("NGram");
        job1.setJarByClass(Driver.class);

        job1.setMapperClass(NGramLibraryBuilder.NGramMapper.class);
        job1.setReducerClass(NGramLibraryBuilder.NGramReducer.class);

        // set input and output class of MapReduce Job1 N-Gram Library
        job1.setOutputKeyClass(Text.class);
        job1.setOutputValueClass(IntWritable.class);

        job1.setInputFormatClass(TextInputFormat.class);
        job1.setOutputFormatClass(TextOutputFormat.class);

        TextInputFormat.setInputPaths(job1, new Path(inputDir));
        TextOutputFormat.setOutputPath(job1, new Path(outputDir));

        job1.waitForCompletion(true);

        // initialize configuration of MapReduce 2
        Configuration conf2 = new Configuration();
        conf1.set("threshold", threshold);
        conf2.set("topK", topK);

        Job job2 = Job.getInstance();
        job2.setJobName("LanguageModel");
        job2.setJarByClass(Driver.class);

        job2.setMapperClass(LanguageModel.LanguageModelMapper.class);
        job2.setReducerClass(LanguageModel.LanguageModelReducer.class);

        // connect Driver with Database
        DBConfiguration.configureDB(conf2,
                "com.mysql.jdbc.Driver",
                "jdbc:mysql://127.0.0.1:8080/auto_completion",
                "root",
                "root");

        // for job.setOutputKeyClass() is to set the key of reducer output by default
        // so when the output forms are different between Mapper and Reducer
        // the outputKeyClass and outputValueClass should be set explicitly
        job2.setMapOutputKeyClass(Text.class);
        job2.setMapOutputValueClass(Text.class);

        job2.setOutputKeyClass(DBOutputWritable.class);
        job2.setOutputValueClass(NullWritable.class);

        job2.setInputFormatClass(TextInputFormat.class);
        job2.setOutputFormatClass(DBOutputFormat.class);

        TextInputFormat.setInputPaths(job2, outputDir); // the input of MR2 is the otuput of MR1
        DBOutputFormat.setOutput(job2, "language_model",
                new String[] {"starting_phrase", "following_word", "count"});
        job1.waitForCompletion(true);


    }
}
