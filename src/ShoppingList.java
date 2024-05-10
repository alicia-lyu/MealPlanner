import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.StandardOpenOption.*;

public class ShoppingList {
    public Map<String, List<Recipe>> shoppingCart;
    public Set<String> checkedOffItems;
    private static final Path DEFAULT_STOCK_PATH = Path.of("lib", "stock.csv");

    ShoppingList(List<String> lines) throws IOException {
        this(lines, DEFAULT_STOCK_PATH);
    }

    ShoppingList(List<String> lines, Path stockPath) throws IOException {
        shoppingCart = new HashMap<>();
        checkedOffItems = new HashSet<>();
        System.out.println("ShoppingList constructor with lines " + lines.toString());
        for (String line : lines) {
            parse(line);
        }
        parseStock(stockPath);
    }

    private void parse(String line) {
        System.out.println("ShoppingList parse line " + line);
        Pattern p = Pattern.compile("(-\\[ \\]|-\\[x\\]) (.+) for \\[(.+\\)]");
        Matcher m = p.matcher(line);
        String start = m.group(1);
        String ingredient = m.group(2);
        // String recipes = m.group(2); Not honored
        if (start.equals("-[x]")) {
            checkedOffItems.add(ingredient);
        }
    }

    private void parseStock(Path stockPath) throws IOException {
        if (!Files.exists(stockPath)) {
            return;
        }
        List<String> stockLines = Files.readAllLines(stockPath);
        for (String line : stockLines) {
            line = line.strip();
            checkedOffItems.add(line);
        }
    }

    public void updateWithRecipe(LocalDateTime mealDateTime, Recipe recipe) {
        for (String ingredient : recipe.ingredients) {
            shoppingCart.putIfAbsent(ingredient, new ArrayList<>()); // Will ignore duplicates
            shoppingCart.get(ingredient).add(recipe);
        }
    }

    public void output(OutputStream calendarOut, String postFix) throws IOException {
        Path cartRecordPath = Paths.get("bin", "cart-" + postFix + ".csv");
        OutputStream cartRecordOut = new BufferedOutputStream(
                Files.newOutputStream(cartRecordPath, CREATE, WRITE));
        cartRecordOut.write("Ingredients\n".getBytes());
        calendarOut.write("## Shopping List\n\n".getBytes());
        calendarOut.write("Changes in this section will not be honored except checking the boxes.\n\n".getBytes());
        String bufferedCheckedItems = "";
        for (Map.Entry<String, List<Recipe>> entry : shoppingCart.entrySet()) {
            String ingredient = entry.getKey();
            List<Recipe> recipes = entry.getValue();
            String recordLine = String.format("%s,'%s'\n", ingredient, recipes.toString());
            cartRecordOut.write(recordLine.getBytes());
            String calendarLine;
            if (checkedOffItems.contains(ingredient)) {
                calendarLine = String.format("- [x] %s\n", ingredient);
                bufferedCheckedItems += calendarLine;
            } else {
                calendarLine = String.format("- [ ] %s\n", ingredient);
                calendarOut.write(calendarLine.getBytes());
            }
        }
        calendarOut.write(bufferedCheckedItems.getBytes());
        calendarOut.write("\n".getBytes());
        cartRecordOut.close();
    }
}
