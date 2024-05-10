import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class App {
    public static void main(String[] args) throws IOException {
        // Load all recipes from recipes.csv and create Recipe objects
        Path recipesPath = Path.of("lib", "recipes.csv");
        List<Recipe> recipes = new RecipesParser(recipesPath).getRecipes();
        // Load weekly plan (.md file from command line)
        Path previousAgenda; // Could also be a new agenda with only the meal plan section
        if (args.length > 0) {
            previousAgenda = Path.of(args[0]);
        } else {
            previousAgenda = Path.of("lib", "agenda.md");
        }
        Agenda agenda = new Agenda(recipes, previousAgenda);
        // Generate a new agenda based on the recipes and the previous agenda
        agenda.output();
    }
}