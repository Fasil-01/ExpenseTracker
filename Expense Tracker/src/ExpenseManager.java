import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.Month;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.BufferedReader;
import java.time.format.DateTimeFormatter;

public class ExpenseManager {
    private List<Expense> expenses;
    private Map<String, Double> categoryBudgets = new HashMap<>(); // Category-specific budgets
    private double globalMonthlyBudget = 1000.0; // Default overall budget
    private static final String FILE_NAME = "expense.json";
    public ExpenseManager() {
        this.expenses = load();
    }

    public void add(String description, float amount) {
        int id = (this.expenses.isEmpty()) ? 1 : this.expenses.get(this.expenses.size() - 1).getId() + 1;
        Expense expense = new Expense(id, description, amount);
        if(checkBudgets(""+expense.getDate().getMonthValue()+"",amount)){
            this.expenses.add(expense);
            this.save(this.expenses);
            this.messageSucces("added", expense.getId());
        }
        
       
    }

    public void delete(int id) {
        boolean isRemoved = this.expenses.removeIf(expense -> expense.getId() == id);
        if (isRemoved) {
            this.save(this.expenses);
            this.expenses = load();
            this.messageSucces("deleted", id);
        } else {
            this.messageErrorId();
        }
    }

    public void update(int id, Map<String, String> updates) {
        System.out.println(updates.get("description"));
        int index = getIndex(id);
        if (index == -1) {
            this.messageErrorId();
        } else {
            Expense newExpense = expenses.get(index);
            updates.forEach((key, value) -> {
                switch (key.toLowerCase()) {
                    case "description" -> newExpense.setDescription(value);
                    case "amount" ->{
                        if(checkBudgets(""+newExpense.getDate().getMonthValue()+"",Double.parseDouble(value))) {
                            newExpense.setAmount(Double.parseDouble(value));
                        }
                    } 
                    default -> System.err.println("Unknown update field: " + key);
                }
                System.out.println(key+" "+value);
            });
           expenses.set(index, newExpense);
           save(expenses);
           messageSucces("updated", id);
        } 
            
    }

    public Expense get(int id) {
        int index = getIndex(id);
        if (index == -1) {
            return null;
        } else {
            return this.expenses.get(index);
        }
    }

    public void list() {
        System.out.println("ID    Date    Description Amount");
        this.expenses.forEach(expense -> System.out.println(expense.toString()));
    }
    public void list(String month) {
        System.out.println("ID    Date    Description Amount");
        expenses.stream()
                    .filter(expense -> expense.getDate().getMonth() == Month.of(Integer.parseInt(month)))
                    .forEach(expense -> System.out.println(expense.toString()));
    }

    public List<Expense> getAll() {
        return this.expenses;
    }

    public void summary() {
        Double result = expenses.stream().mapToDouble(expense -> expense.getAmount())
               .sum();
        System.out.println("Total expenses: $" + result);
    }

    public void summary(String month) {
        int monthInt = Integer.parseInt(month);
        if (monthInt < 1 || monthInt > 12) {
            System.out.println("Invalid month. Please enter a number between 1 and 12.");
        } else {
            Double total = totalForMonth(month);
            System.out.println("Total expenses for " + Month.of(Integer.parseInt(month)) + ": $" + total);
        }

    }

    void save(List<Expense> expenses) {
        File jsonOutputFile = new File("expense.json");

        try (PrintWriter pw = new PrintWriter(jsonOutputFile)) {
            pw.println("["); // Start of the JSON array

            // Convert each expense to JSON and write to the file
            String jsonContent = expenses.stream()
                    .map(Expense::stringify)
                    .collect(Collectors.joining(",\n"));

            pw.println(jsonContent);
            pw.println("]"); // End of the JSON array

        } catch (FileNotFoundException e) {
            System.err.println("Error creating file: " + e.getMessage());
        }
    }

    public List<Expense> load() {
       List<Expense> expenses = new ArrayList<>();
        if (!Files.exists(Paths.get(FILE_NAME))) {
            return expenses;
        }
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(FILE_NAME))) {
            String jsonContent = reader.lines().collect(Collectors.joining());
            if (!jsonContent.trim().isEmpty()) {
                Arrays.stream(jsonContent.replace("[", "").replace("]", "").split("(?<=\\}),\\s*(?=\\{)"))
                        .map(this::parseExpense)
                        .forEach(expenses::add);
            }
        } catch (IOException e) {
            System.err.println("Error reading " + FILE_NAME + ": " + e.getMessage());
        }
        return expenses;
    }

    // Method to parse a single JSON object into an Expense object
    private Expense parseExpense(String json) {
        Expense expense = new Expense();
        Arrays.stream(json.replace("{", "").replace("}", "").split(",\\s*"))
                .map(field -> field.split(":"))
                .forEach(keyValue -> {
                    String key = keyValue[0].trim().replace("\"", "");
                    String value = keyValue[1].trim().replace("\"", "");
                    switch (key) {
                        case "id" -> expense.setId(Integer.parseInt(value));
                        case "date" -> expense.setDate(LocalDate.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                        case "description" -> expense.setDescription(value);
                        case "amount" -> expense.setAmount(Double.parseDouble(value));
                    }
                });
        return expense;
    }

    int getIndex(int id) {
        for (int i = 0; i < expenses.size(); i++) {
            if (expenses.get(i).getId() == id) {
                return i;
            }

        }
        return -1;

    }

    void messageSucces(String type, int id) {
        System.out.println("Expense " + type + " successfully (ID: " + id + ")");
    }

    void messageErrorId() {
        System.out.println("Expense not found with the provided ID.");
    }
    public void setCategoryBudget(String category, double budget) {
        categoryBudgets.put(category, budget);
        System.out.println("Budget for category '" + category + "' set to $" + budget);
    }

    // Set global monthly budget
    public void setGlobalMonthlyBudget(double budget) {
        this.globalMonthlyBudget = budget;
        System.out.println("Global monthly budget set to $" + budget);
    }
    private Boolean checkBudgets(String month, double amount) {
        double totalForMonth = totalForMonth(month);
        Boolean ok = true;
        if (totalForMonth+amount >= globalMonthlyBudget) {
            System.out.println("Warning: You have exceeded the global monthly budget for " + Month.of(Integer.parseInt(month)));
            ok = false;
        }
        return ok;
    }
    private double totalForMonth(String month){
        return expenses.stream()
                    .filter(expense -> expense.getDate().getMonth() == Month.of(Integer.parseInt(month)))
                    .mapToDouble(expense -> expense.getAmount())
                    .sum();
    }
    /*
     * Add expense categories and allow users to filter expenses by category.
     */
}
