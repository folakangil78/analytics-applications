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
            System.out.println("Usage: java CovidMaxDeathsByYear <input_file>");
            return;
        }

        String filePath = args[0];

        double max2019 = 0;
        double max2020 = 0;
        double max2021 = 0;
        double max2022 = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String header = br.readLine().replace("\"", "");
            if (header == null) {
                System.out.println("Empty file.");
                return;
            }

            String[] headers = header.split(",");

            int dateIndex = -1;
            int deathAvgIndex = -1;

            // Locate needed columns
            for (int i = 0; i < headers.length; i++) {

                String col = headers[i].replace("\"", "").trim();

                if (col.equalsIgnoreCase("date_of_interest")) {
                    dateIndex = i;
                }

                if (col.equalsIgnoreCase("DEATH_COUNT_7DAY_AVG")) {
                    deathAvgIndex = i;
                }
            }

            if (dateIndex == -1 || deathAvgIndex == -1) {
                System.out.println("Required columns not found.");
                return;
            }

            String line;

            while ((line = br.readLine()) != null) {
                // Remove all quotes first
                line = line.replace("\"", "");

                String[] columns = line.split(",");

                if (columns.length <= deathAvgIndex) {
                    continue;
                }

                String date = columns[dateIndex].trim();
                String deathStr = columns[deathAvgIndex].trim();

                if (date.isEmpty() || deathStr.isEmpty()) {
                    continue;
                }

                String year = date.substring(0, 4);

                double value;

                try {
                    value = Double.parseDouble(deathStr);
                } catch (NumberFormatException e) {
                    continue;
                }

                if (year.equals("2019") && value > max2019) {
                    max2019 = value;
                }
                if (year.equals("2020") && value > max2020) {
                    max2020 = value;
                }
                if (year.equals("2021") && value > max2021) {
                    max2021 = value;
                }
                if (year.equals("2022") && value > max2022) {
                    max2022 = value;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("2019  " + (int) max2019);
        System.out.println("2020  " + (int) max2020);
        System.out.println("2021  " + (int) max2021);
        System.out.println("2022  " + (int) max2022);
    }
}