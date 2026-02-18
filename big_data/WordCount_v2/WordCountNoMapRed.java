import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class WordCountNoMapRed {
    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Usage: java CovidMaxDeathsByYear <input_file>");
            return;
        }

        String filePath = args[0];

        // Store max death averages per year
        Map<String, Double> maxByYear = new HashMap<>();

        // Initialize target years
        maxByYear.put("2019", Double.MIN_VALUE);
        maxByYear.put("2020", Double.MIN_VALUE);
        maxByYear.put("2021", Double.MIN_VALUE);
        maxByYear.put("2022", Double.MIN_VALUE);

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String line;
            boolean isHeader = true;

            int dateIndex = -1;
            int deathAvgIndex = -1;
            
    }
}
