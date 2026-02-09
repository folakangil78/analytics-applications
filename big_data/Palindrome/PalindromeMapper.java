import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class PalindromeMapper
        extends Mapper<Object, Text, Text, IntWritable> {

    private static final IntWritable ONE = new IntWritable(1);
    private Text word = new Text();

    @Override
    protected void map(Object key, Text value, Context context)
            throws IOException, InterruptedException {

        String line = value.toString().trim().toLowerCase();

        // Skip empty lines
        if (line.isEmpty()) {
            return;
        }

        // Only consider words longer than 3 characters
        if (line.length() > 3 && isPalindrome(line)) {
            word.set(line);
            context.write(word, ONE);
        }
    }

    private boolean isPalindrome(String str) {
        int left = 0;
        int right = str.length() - 1;

        while (left < right) {
            if (str.charAt(left) != str.charAt(right)) {
                return false;
            }
            left++;
            right--;
        }
        return true;
    }
}
