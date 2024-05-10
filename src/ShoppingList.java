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

    ShoppingList(List<String> lines) {
        shoppingCart = new HashMap<>();
        checkedOffItems = new HashSet<>();
        for (String line: lines) {
            parse(line);
        }
    }

    private void parse(String line) {
        Pattern p = Pattern.compile("(-\\[ \\]|-\\[x\\]) (.+) for \\[(.+\\)]");
        Matcher m = p.matcher(line);
        String start = m.group(1);
        String ingredient = m.group(1);
        // String recipes = m.group(2); Not honored
        if (start.equals("-[x]")) {
            checkedOffItems.add(ingredient);
        }
    }
    
    public void updateWithRecipe(LocalDateTime mealDateTime, Recipe recipe) {
        for (String ingredient : recipe.ingredients) {
            shoppingCart.putIfAbsent(ingredient, new ArrayList<>()); // Will ignore duplicates
            shoppingCart.get(ingredient).add(recipe);
        }
    }

    public void output(OutputStream calendarOut, String postFix) {
        Path cartRecordPath = Paths.get("bin", "cart-" + postFix + ".csv");
        try (OutputStream cartRecordOut = new BufferedOutputStream(
                Files.newOutputStream(cartRecordPath, CREATE, WRITE))
        ) {
            cartRecordOut.write("Ingredients\n".getBytes());
            calendarOut.write("## Shopping List\n\n".getBytes());
            calendarOut.write("Changes in this section not honored except checking the boxes.\n\n".getBytes());
            for (Map.Entry<String, List<Recipe>> entry : shoppingCart.entrySet()) {
                String ingredient = entry.getKey();
                List<Recipe> recipes = entry.getValue();
                String recordLine = String.format("%s,'%s'\n", ingredient, recipes.toString());
                cartRecordOut.write(recordLine.getBytes());
                String calendarLine = String.format("-[ ] %s for %s\n", ingredient, recipes.toString()); 
                // TODO: use a stock file to check off
                calendarOut.write(calendarLine.getBytes());
            }
        } catch (IOException x) {
            System.err.println(x);
        }
    }
}
