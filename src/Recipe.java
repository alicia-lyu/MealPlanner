import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Minimal information required:
 * @ingredients (will be used to generate shopping list)
 * @preparationSteps (will be used to generate agenda for cooking preparation so that you can complete ahead of time)
 * Cooking instruction not required and will be ignored in the current version
 */
public class Recipe {
    public final String name;
    public final List<String> ingredients;
    public final Map<Duration, String> preparationSteps;
    Recipe(String name, List<String> ingredients, Map<Duration, String> preparationSteps) {
        this.name = name;
        this.ingredients = ingredients;
        this.preparationSteps = preparationSteps;
    }

    @Override
    public String toString() {
        return name;
    }
}