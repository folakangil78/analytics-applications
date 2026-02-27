import java.io.IOException;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class NeighborhoodReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

}