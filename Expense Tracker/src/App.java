import java.io.Console;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class App {
    static ExpenseManager cli = new ExpenseManager();
    public static void main(String[] args) throws Exception {
        run();
    }

    int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            System.err.println("Invalid number format: " + value);
            throw e;
        }
    }

    static float parseFloat(String value) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            System.err.println("Invalid number format: " + value);
            throw e;
        }
    }

    private static void run() {
        Console console = System.console();

        if (console == null) {
            System.out.println("No console available.");
            return;
        }

        while (true) {
            String commandInput = console.readLine("ExpenseManager> ").trim();
            String[] arguments = commandInput.split(" ");
            String command = arguments[0].toLowerCase();

            switch (command) {
                case "add":
                    if (arguments.length >= 5 && arguments[1].equals("--description") && arguments[3].equals("--amount")) {
                        String description = arguments[2];
                        float amount = parseFloat(arguments[4]);
                        cli.add(description, amount);
                    } else {
                        System.out.println("Invalid syntax for 'add' command.");
                    }
                    break;

                case "list":
                    if (arguments.length == 1) {
                       cli.list(); 
                    } else if (arguments.length == 3 && arguments[1].equals("--month")) {
                        String month = arguments[2];
                        cli.list(month); 
                    } else {
                        System.out.println("Invalid syntax for 'list' command.");
                    }
                    break;

                case "delete":
                    if (arguments.length == 3 && arguments[1].equals("--id")) {
                        int id = Integer.parseInt(arguments[2]);
                        cli.delete(id); 
                    } else {
                        System.out.println("Invalid syntax for 'delete' command.");
                    }
                    break;

                    case "update":
                    // Validate that there are enough arguments
                    if (arguments.length < 4) {
                        System.out.println("Insufficient arguments provided for update operation.");
                        break;
                    }
                
                    // Map of valid options for updating
                    Map<String, String> validOptions = Map.of(
                        "--description", "description",
                        "--amount", "amount"
                    );
                
                    // Use Stream to process arguments and collect into a Map
                    Map<String, String> updates = IntStream.range(3, arguments.length - 1)
                        .filter(i -> i % 2 == 0) // Ensure we only consider even indices for keys
                        .boxed()
                        .collect(Collectors.toMap(
                            i -> arguments[i], // key
                            i -> arguments[i + 1], // value
                            (existing, replacement) -> replacement // Merge function if duplicate keys are found
                        ))
                        .entrySet()
                        .stream()
                        .filter(entry -> validOptions.containsKey(entry.getKey())) // Keep only valid options
                        .collect(Collectors.toMap(
                            entry -> validOptions.get(entry.getKey()), // Map to valid key names
                            Map.Entry::getValue
                        ));
                
                    // Parse the ID and perform the update
                    Optional<Integer> idOptional = Optional.ofNullable(arguments[2])
                        .filter(idStr -> idStr.matches("\\d+")) // Ensure ID is a valid number
                        .map(Integer::parseInt);
                
                    if (idOptional.isPresent()) {
                        cli.update(idOptional.get(), updates);
                    } else {
                        System.out.println("Invalid ID format: " + arguments[2]);
                    }
                    break;
                case "export":
                    try {
                        System.out.println("Exporting to file...");
                        Utils.givenDataArray_whenConvertToCSV_thenOutputCreated(cli.getAll()); // Assuming this method exists
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                case "exit":
                    System.exit(0);
                    break;
                case "budget":
                   setGlobalMonthlyBudget(double budget);
                break;
                case "summary":
                if (arguments.length == 2) {
                  cli.summary(arguments[1]);
                }else
                   cli.summary();
                break;
                case "help":
                    System.out.println("Available commands: add, list, delete, edit, export, help, exit");
                    break;
                default:
                    System.out.println("Unknown command. Type 'help' for a list of available commands.");
                    break;
            }
        }
    }
}
