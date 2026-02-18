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

                String[] columns = line.split(",");

                if (columns.length <= deathAvgIndex) continue;

                String date = columns[dateIndex].replace("\"", "").trim();
                String deathAvgStr = columns[deathAvgIndex].replace("\"", "").trim();

                if (deathAvgStr.isEmpty()) continue;

                try {
                    double deathAvg = Double.parseDouble(deathAvgStr);
                    int year = Integer.parseInt(date.substring(0, 4));

                    if (year == 2019 && deathAvg > max2019) max2019 = deathAvg;
                    if (year == 2020 && deathAvg > max2020) max2020 = deathAvg;
                    if (year == 2021 && deathAvg > max2021) max2021 = deathAvg;
                    if (year == 2022 && deathAvg > max2022) max2022 = deathAvg;

                } catch (Exception e) {
                    // ignore bad rows
                }
            }
        }
    }
}