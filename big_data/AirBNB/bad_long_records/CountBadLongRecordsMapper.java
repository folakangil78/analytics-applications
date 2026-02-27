import java.io.IOException;

import javax.naming.Context;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class CountBadLongRecordsMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
    private final static IntWritable one = new IntWritable(1);
    private Text badKey = new Text("Total number of long lines in AirBNB file:");

    private static final int EXPECTED_FIELDS = 16;

    @Override
    protected void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {

        String line = value.toString();

        // Skip header
        if (line.startsWith("id,")) {
            return;
        }

        // CSV-safe split
        String[] fields = line.split(",");

        if (fields.length > EXPECTED_FIELDS) {
            context.write(badKey, one);
        }
    }
}