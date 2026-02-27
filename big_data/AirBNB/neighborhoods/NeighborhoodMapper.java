import java.io.IOException;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class NeighborhoodMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
    private final static IntWritable one = new IntWritable(1);
    private Text neighborhoodKey = new Text();

    @Override
    protected void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {

        String line = value.toString();

        // Skip header
        if (line.startsWith("id")) {
            return;
        }

        // Split CSV safely
        String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

        if (fields.length > 5) {

            String neighbourhoodGroup = fields[4].trim();
            String neighbourhood = fields[5].trim();

            String outputKey = neighbourhoodGroup + " " + neighbourhood;

            neighborhoodKey.set(outputKey);
            context.write(neighborhoodKey, one);
        }
    }
}