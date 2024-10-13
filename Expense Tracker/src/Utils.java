import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import java.io.PrintWriter;
import java.util.stream.Collectors;

public class Utils {
    private static final String CSV_FILE_NAME = "output.csv"; // Replace with your desired filename
    private static String escapeSpecialCharacters(String data) {
        if (data == null) {
            throw new IllegalArgumentException("Input data cannot be null");
        }
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }
    public static void givenDataArray_whenConvertToCSV_thenOutputCreated(List<Expense> expenses) throws IOException {
        File csvOutputFile = new File(CSV_FILE_NAME);
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            expenses.stream()
             .map(e->e.toArray())
             .map(Utils::convertToCSV)
            .forEach(pw::println); 
        }
    }
    private static String convertToCSV(String [] data) {

        return Stream.of(data)
          .map(Utils::escapeSpecialCharacters)
          .collect(Collectors.joining(","));
    }
}
