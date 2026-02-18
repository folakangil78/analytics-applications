import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * TERMINAL USAGE FOR GRADERS:
 * javac WordCountNoMapRed.java for compilation
 * java WordCountNoMapRed <MAKE SURE TO INSERT FILE PATH TO COVID DATA HERE" as second argument
 * @param args
 */

public class WordCountNoMapRed {
    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Usage: java CovidMaxDeathsByYear <Covid19_cases_deaths.csv>");
            System.out.println("Make sure to add the file path to the covid data from the " +
            "assignment for correct compilation");
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

            String headerLine = br.readLine();
            if (headerLine == null) {
                System.out.println("Empty file.");
                return;
            }

            String[] headers = headerLine.split(",");

            int dateIndex = -1;
            int deathAvgIndex = -1;

            // Normalize and locate columns
            for (int i = 0; i < headers.length; i++) {
                String col = headers[i].trim().toLowerCase();

                if (col.equals("date_of_interest")) {
                    dateIndex = i;
                }

                if (col.equals("death_count_7day_avg")) {
                    deathAvgIndex = i;
                }
            }
            if (dateIndex == -1 || deathAvgIndex == -1) {
                System.out.println("Required columns not found.");
                System.out.println("Detected headers:");
                for (String h : headers) {
                    System.out.println(h);
                }
                return;
            }

            String line;

            while ((line = br.readLine()) != null) {

                String[] columns = line.split(",");

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

                } catch (NumberFormatException ignored) {
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("2019: " + maxByYear.get("2019"));
        System.out.println("2020: " + maxByYear.get("2020"));
        System.out.println("2021: " + maxByYear.get("2021"));
        System.out.println("2022: " + maxByYear.get("2022"));
    }
}