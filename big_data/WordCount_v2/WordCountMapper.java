import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class WordCountMapper
        extends Mapper<LongWritable, Text, Text, DoubleWritable> {

    private int dateIndex = -1;
    private int deathAvgIndex = -1;
    private boolean headerParsed = false;

    @Override
    protected void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {

        String line = value.toString().replace("\"", "");
        String[] columns = line.split(",", -1);

        // Parse header first
        if (!headerParsed) {
            for (int i = 0; i < columns.length; i++) {
                String col = columns[i].trim();
                if (col.equalsIgnoreCase("date_of_interest")) dateIndex = i;
                if (col.equalsIgnoreCase("DEATH_COUNT_7DAY_AVG")) deathAvgIndex = i;
            }
            headerParsed = true;
            return; // header processed
        }

        if (dateIndex == -1 || deathAvgIndex == -1) return;
        if (columns.length <= deathAvgIndex) return;

        String dateStr = columns[dateIndex].trim();
        String deathStr = columns[deathAvgIndex].trim();

        if (dateStr.isEmpty() || deathStr.isEmpty()) return;

        try {
            // Extract year from MM/DD/YYYY
            String[] dateParts = dateStr.split("/");
            if (dateParts.length != 3) return;
            int year = Integer.parseInt(dateParts[2]);

            // Only consider years 2019-2022
            if (year < 2019 || year > 2022) return;

            double deathAvg = Double.parseDouble(deathStr);

            // Emit year as key, deathAvg as value
            context.write(new Text(String.valueOf(year)), new DoubleWritable(deathAvg));

        } catch (Exception e) {
            // skip malformed rows
        }
    }
}
