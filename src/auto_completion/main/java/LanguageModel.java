package auto_completion.main.java;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.*;

/**
 * @author yifengguo
 * instead of comparing probabilities of all possibile following words, better compare the frequency of following
 * words for the denominators are all the same
 *
 *  P(word | phrase) = Count(phrase + word) / Count(phrase)
 *  for all cases with the same phrase, the count is equal, so compare Count(phrase + word) directly
 *
 *  MapReduce Job1 built the N-Gram Library so currently we have library like
 *      This is         20
 *      is cool         10
 *      cool since      18
 *      is cool since   8
 *      cool since this 12
 *
 * The Mapper of Language Model should furthermore cut the phrase to (n - 1) + 1 form
 *                 Mapper
 * This is 20     -------->     This | is = 20
 * is cool        -------->     is   | cool = 10
 * ...
 *
 *
 *
 */
public class LanguageModel {
    public static class LanguageModelMapper extends Mapper<LongWritable, Text, Text, Text> {
        int threshold;
        @Override
        public void setup(Context context) {
            Configuration conf = context.getConfiguration();
            // name is the key for threshold mapping to variable in args[]
            // 20 is the default value to avoid the NullPointerException
            threshold = conf.getInt("threshold", 20);

        }
        /**
         * input is the output of first MapReduce Job
         *        key            value
         * This is cool weather\t10
         *
         * output:
         *          key          value
         *       This is cool    weather = 10
         *       This is         cool = 20
         *       ...             ...
         *
         * @param key
         * @param value
         * @param context
         * @throws IOException
         * @throws InterruptedException
         */
        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException{
            // input form: This is cool weather\t10
            String data = value.toString().trim();

            String[] sentence = data.split("\t");  // This is cool weather   \t   10

            if (sentence.length < 2) {
                return;
            }

            String[] words = sentence[0].split("\\s+"); // split words with whitespace
            int count = Integer.valueOf(sentence[1]);  // need to set a threshold to filter out rare frequency cases
                                                       // the threshold had better initialized by args[]
                                                       // and sent by configuration

            // filter out following word sum < threshold
            if (count < threshold) {
                return;
            }

            // key of the output: This is cool
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < words.length - 1; i++) {
                sb.append(words[i]);
                sb.append(" ");
            }

            // value of the output: weather=10
            String output = words[words.length - 1] + "=" + count;

            context.write(new Text(sb.toString().trim()), new Text(output));

        }
    }


    public static class LanguageModelReducer extends Reducer<Text, Text, DBOutputWritable, NullWritable> {
        int topK;
        @Override
        public void setup(Context context) {
            Configuration conf = context.getConfiguration();
            // name is the key for threshold mapping to variable in args[]
            // 20 is the default value to avoid the NullPointerException
            topK = conf.getInt("topK", 5);

        }
        /**
         * The reducer is to find top k following words given starting_phrase
         *    Demo:
         *       input       key                value
         *                 This is cool         weather=10
         *                 This is cool         joke=20
         *                 This is cool         dance=6
         *                 This is              cool=50
         *                 This is              bad=14
         *                 ...                  ...
         *
         *
         *       output      key               value
         *                   10                <cool, weather>
         *                   20                <cool, joke>
         *                   6                 <cool, dance>
         *
         *  The reducer will write the output into Database
         * @param key
         * @param values
         * @param context
         * @throws IOException
         * @throws InterruptedException
         */
        @Override
        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException{
            // This is cool
            // <10, <weather, day, boy>, <5, <kill, course>>>
            TreeMap<Integer, List<String>> tm = new TreeMap<>(Collections.<Integer>reverseOrder());

            // construct the treemap
            for (Text value : values) {
                int freq = Integer.parseInt(value.toString().split("=")[1].trim());
                String word = value.toString().trim().split("=")[0];
                if (!tm.containsKey(freq)) {
                    List<String> list = new ArrayList<>();
                    list.add(word);
                    tm.put(freq, list);
                } else {
                    tm.get(freq).add(word);
                }
            }

            Iterator<Integer> it = tm.keySet().iterator();
            for (int j = 0; it.hasNext() && j < topK;) {
                int keyCount = it.next();
                List<String> words = tm.get(keyCount);
                for (int i = 0; i < words.size() && j < topK; i++) {
                    context.write(new DBOutputWritable(key.toString(), words.get(i), keyCount), NullWritable.get());
                    j++;
                }
            }
        }
    }
}
