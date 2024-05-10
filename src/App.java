import java.nio.file.Path;
import java.util.List;

public class App {
    public static void main(String[] args) throws Exception {
        // Load all recipes from recipes.csv and create Recipe objects
        Path recipesPath = Path.of("bin", "recipes.csv");
        List<Recipe> recipes = new RecipesParser(recipesPath).getRecipes();
        // Load weekly plan (.md file from command line)
        Path previousAgenda = Path.of(args[0]); // Could also be a new agenda with only the meal plan section
        Agenda agenda = new Agenda(recipes, previousAgenda.toString());
        // Generate a new agenda based on the recipes and the previous agenda
        agenda.output();
    }
}