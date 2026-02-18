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

            while ((line = br.readLine()) != null) {

                String[] columns = line.split(",");

                // Handle header row
                if (isHeader) {
                    for (int i = 0; i < columns.length; i++) {
                        if (columns[i].equalsIgnoreCase("date_of_interest")) {
                            dateIndex = i;
                        }
                        if (columns[i].equalsIgnoreCase("death_count_7day_avg")) {
                            deathAvgIndex = i;
                        }
                    }
                    isHeader = false;
                    continue;
                }

                // Skip if columns missing
                if (dateIndex == -1 || deathAvgIndex == -1) {
                    System.out.println("Required columns not found.");
                    return;
                }

                if (columns.length <= deathAvgIndex) {
                    continue;
                }

                String date = columns[dateIndex].trim();
                String deathAvgStr = columns[deathAvgIndex].trim();

                if (date.isEmpty() || deathAvgStr.isEmpty()) {
                    continue;
                }

                String year = date.substring(0, 4);

                if (!maxByYear.containsKey(year)) {
                    continue;
                }

                try {
                    double deathAvg = Double.parseDouble(deathAvgStr);

                    if (deathAvg > maxByYear.get(year)) {
                        maxByYear.put(year, deathAvg);
                    }

                } catch (NumberFormatException e) {
                    // Skip invalid numbers
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Print results in required order
        System.out.println("2019: " + maxByYear.get("2019"));
        System.out.println("2020: " + maxByYear.get("2020"));
        System.out.println("2021: " + maxByYear.get("2021"));
        System.out.println("2022: " + maxByYear.get("2022"));
    }
}
