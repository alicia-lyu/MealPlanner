import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipesParser {
    public List<Recipe> recipes;

    public RecipesParser(Path p) throws IOException {
        recipes = new ArrayList<>();
        List<String> lines = Files.readAllLines(p);
        for (String line : lines) {
            String[] lineSegs = line.split("\s*,\s*");
            String name = lineSegs[0];
            String[] ingredients = lineSegs[1].split("\s*;\s*");
            Map<Duration, String> preparationSteps = parsePreparationSteps(lineSegs[2]);
            Recipe r = new Recipe(name, Arrays.asList(ingredients), preparationSteps);
            recipes.add(r);
        }
    }

    public RecipesParser() throws IOException {
        this(Path.of("lib", "recipes.csv"));
    }

    private Map<Duration, String> parsePreparationSteps(String s) {
        Map<Duration, String> preparationSteps = new HashMap<>();
        String[] steps = s.split("\s*;\s*");
        for (String step : steps) {
            System.out.println("Step: " + step);
            String[] stepSegs = step.split("\s*:\s*");
            assert stepSegs.length == 2;
            Duration d = Duration.parse(stepSegs[0]);
            String stepText = stepSegs[1];
            preparationSteps.put(d, stepText);
        }
        return preparationSteps;
    }

    

    public List<Recipe> getRecipes() {
        return recipes;
    }
}
