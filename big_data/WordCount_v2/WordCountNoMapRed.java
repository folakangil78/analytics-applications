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

        // Max values per year
        double max2019 = 0.0;
        double max2020 = 0.0;
        double max2021 = 0.0;
        double max2022 = 0.0;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String header = br.readLine();
            if (header == null) {
                System.out.println("Empty file.");
                return;
            }

            // Remove quotes and split header
            header = header.replace("\"", "");
            String[] headers = header.split(",", -1);

            int dateIndex = -1;
            Map<String, Integer> boroughDeathIndex = new HashMap<>();

            // Find date column and borough death 7-day avg columns
            for (int i = 0; i < headers.length; i++) {
                String col = headers[i].trim();
                if (col.equalsIgnoreCase("date_of_interest")) {
                    dateIndex = i;
                }
                // These columns contain borough-specific death 7-day averages
                if (col.equalsIgnoreCase("BX_DEATH_COUNT_7DAY_AVG") ||
                    col.equalsIgnoreCase("BK_DEATH_COUNT_7DAY_AVG") ||
                    col.equalsIgnoreCase("MN_DEATH_COUNT_7DAY_AVG") ||
                    col.equalsIgnoreCase("QN_DEATH_COUNT_7DAY_AVG") ||
                    col.equalsIgnoreCase("SI_DEATH_COUNT_7DAY_AVG")) {
                    boroughDeathIndex.put(col, i);
                }
            }

            if (dateIndex == -1 || boroughDeathIndex.size() != 5) {
                System.out.println("Required columns not found.");
                return;
            }

            String line;
            while ((line = br.readLine()) != null) {

                line = line.replace("\"", "");
                String[] columns = line.split(",", -1);

                if (columns.length <= dateIndex) continue;

                String date = columns[dateIndex].trim();
                if (date.isEmpty()) continue;

                try {
                    // Date format MM/DD/YYYY
                    String[] dateParts = date.split("/");
                    if (dateParts.length != 3) continue;

                    int fullYear = Integer.parseInt(dateParts[2]);

                    // Sum borough-specific death 7-day averages
                    double rowTotal = 0.0;
                    for (int idx : boroughDeathIndex.values()) {
                        if (idx < columns.length) {
                            String valStr = columns[idx].trim();
                            if (!valStr.isEmpty()) {
                                rowTotal += Double.parseDouble(valStr);
                            }
                        }
                    }

                    // Update yearly max
                    if (fullYear == 2019 && rowTotal > max2019) max2019 = rowTotal;
                    if (fullYear == 2020 && rowTotal > max2020) max2020 = rowTotal;
                    if (fullYear == 2021 && rowTotal > max2021) max2021 = rowTotal;
                    if (fullYear == 2022 && rowTotal > max2022) max2022 = rowTotal;

                } catch (Exception e) {
                    // skip bad rows
                }
            }

            // Print final maxes
            System.out.println("2019  " + (int) max2019);
            System.out.println("2020  " + (int) max2020);
            System.out.println("2021  " + (int) max2021);
            System.out.println("2022  " + (int) max2022);

        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }
}
