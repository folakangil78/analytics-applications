import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class WordCountNoMapRed {

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Usage: java CovidMaxDeathsByYear <input_file>");
            return;
        }

        String filePath = args[0];

        int max2019 = 0;
        int max2020 = 0;
        int max2021 = 0;
        int max2022 = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String header = br.readLine();
            if (header == null) {
                System.out.println("Empty file.");
                return;
            }

            header = header.replace("\"", "");
            String[] headers = header.split(",", -1);

            int dateIndex = -1;
            int deathAvgIndex = -1;

            for (int i = 0; i < headers.length; i++) {
                String col = headers[i].trim();
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

                line = line.replace("\"", "");
                String[] columns = line.split(",", -1);

                if (columns.length <= deathAvgIndex) continue;

                String date = columns[dateIndex].trim();
                String deathAvgStr = columns[deathAvgIndex].trim();

                if (date.isEmpty() || deathAvgStr.isEmpty()) continue;

                try {
                    String[] dateParts = date.split("/");
                    if (dateParts.length != 3) continue;

                    int year = Integer.parseInt(dateParts[2]);
                    int deathAvg = (int) Math.round(Double.parseDouble(deathAvgStr));

                    if (year == 2019 && deathAvg > max2019) max2019 = deathAvg;
                    if (year == 2020 && deathAvg > max2020) max2020 = deathAvg;
                    if (year == 2021 && deathAvg > max2021) max2021 = deathAvg;
                    if (year == 2022 && deathAvg > max2022) max2022 = deathAvg;

                } catch (Exception e) {
                    // skip bad rows
                }
            }

            System.out.println("2019  " + max2019);
            System.out.println("2020  " + max2020);
            System.out.println("2021  " + max2021);
            System.out.println("2022  " + max2022);

        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }
}
