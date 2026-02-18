import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class WordCountReducer
        extends Reducer<Text, DoubleWritable, Text, DoubleWritable> {

    @Override
    protected void reduce(Text key, Iterable<DoubleWritable> values, Context context)
            throws IOException, InterruptedException {

        double max = 0.0;
        for (DoubleWritable val : values) {
            if (val.get() > max) {
                max = val.get();
            }
        }

        // Round to nearest integer to match previous exercise
        context.write(key, new DoubleWritable(Math.round(max)));
    }
}
