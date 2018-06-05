package auto_completion.main.java;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/**
 * @author yifengguo
 */
public class NGramLibraryBuilder {
    public static class NGramMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
        int noGram;
        /**
         * whenever we need some data once and this data will not change, we can override setup() and set this
         * variable here
         * setup() will be invoked once and only once when mapper is initialized
         * @param context
         */
        @Override
        public void setup(Context context) {
            Configuration conf = context.getConfiguration();
            noGram = conf.getInt("noGram", 5);
        }
        /**
         * input: A sentence from document
         * demo:  This is mine
         * output:     set n = 3 (ignore n == 1)
         * This is mine    1
         * This is    1
         * is mine    1
         * @param key current reading sentence offset in the article
         * @param value the current reading sentence
         * @param context
         * @throws IOException
         * @throws InterruptedException
         */
        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            // Hadoop uses Context object with Mapper to interact with rest of the system.
            // Context object gets the configuration of the system and job in its constructor.
            // We use Context object to pass the information in setup(), cleanup() and map() methods.
            // This is an important object that makes the important information available during the map operations.

            // context allows the Mapper/Reducer to interact with the rest of the Hadoop system
            // so n of N-Gram should be set in main() and sent by context between MR and External World
//            Configuration conf = context.getConfiguration();  // initialized in setup()
//
//            int noGram = conf.getInt("noGram", 5); // get number of gram

            String sentence = value.toString().trim().toLowerCase().replaceAll("[^a-z]", " ");

            String[] words = sentence.split("\\s+");
            if (words.length <= 1) { // filter out single word case
                return;
            }

            // build and output all n-gram cases
            StringBuilder sb;
            for (int i = 0; i < words.length; i++) {
                sb = new StringBuilder();
                sb.append(words[i]);
                for (int j = 1; j + i < words.length && j < noGram; j++) {
                    sb.append(" ");
                    sb.append(words[i + j]);
                    context.write(new Text(sb.toString()), new IntWritable(1));
                }
            }
        }
    }

    public static class NGramReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
        /**
         * merge the output of mapper
         * @param key
         * @param values
         * @param context
         * @throws IOException
         * @throws InterruptedException
         */
        @Override
        public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException,
                InterruptedException {
            int sum = 0;
            for (IntWritable value : values) {
                sum += value.get();
            }
            context.write(key, new IntWritable(sum));
        }
    }
}
