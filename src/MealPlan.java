import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import static java.nio.file.StandardOpenOption.*;
import java.util.Iterator;

public class MealPlan {
    public TreeMap<LocalDateTime, Recipe> mealAgenda;
    private List<Recipe> recipes;

    MealPlan(List<String> lines, List<Recipe> recipes) {
        this.recipes = recipes;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] lineSegs = line.split("\s*\\|\s*");
            switch (i) {
                case 0:
                    assert lineSegs[0].equalsIgnoreCase("day");
                case 1:
                    assert lineSegs[0].contains("-");
                default:
                    parse(lineSegs);
            }
        }
    }

    private void parse(String[] lineSegs) {
        Day day = Day.getDay(lineSegs[0]);
        LocalDate date = Agenda.getDateOfNearestDay(day.value);
        for (int i = 1; i < lineSegs.length; i++) {
            Recipe recipe = null;
            for (Recipe r: recipes) {
                if (r.name.equalsIgnoreCase(lineSegs[i])) {
                    recipe = r;
                }
            }
            if (recipe == null) continue;
            LocalDateTime mealDateTime = LocalDateTime.of(date, Config.MEAL_TIMES[i - 1]);
            mealAgenda.put(mealDateTime, recipe);
        }
    }

    public Iterator<Map.Entry<LocalDateTime, Recipe>> getIt() {
        return mealAgenda.entrySet().iterator();
    }

    public void output(OutputStream calendarOut, String postFix) {
        Path mealRecordPath = Paths.get("bin", "meal-" + postFix + ".csv");
        try (OutputStream mealRecordOut = new BufferedOutputStream(
                Files.newOutputStream(mealRecordPath, CREATE, WRITE))) {
            mealRecordOut.write("Time,Recipe\n".getBytes());
            for (Map.Entry<LocalDateTime, Recipe> entry : mealAgenda.entrySet()) {
                LocalDateTime mealDateTime = entry.getKey();
                Recipe recipe = entry.getValue();
                String line = String.format("%s,%s\n",
                        mealDateTime.format(Agenda.FORMATTER_SHORT),
                        recipe.name);
                mealRecordOut.write(line.getBytes());
            }
            calendarOut.write("# Meal Plan\n\n".getBytes());
            calendarOut.write("## Meals\n\n".getBytes());
            calendarOut.write("Changes in this section will only reflect in other sections after regenerating agenda with this markdown file.\n\n".getBytes());
            calendarOut.write("| Day | Breakfast | Lunch | Dinner |\n".getBytes());
            calendarOut.write("| --- | --------- | ----- | ------ |\n".getBytes());
            LocalDate lastDate = mealAgenda.firstKey().toLocalDate();
            String breakfast = null, lunch = null, dinner = null;
            for (Map.Entry<LocalDateTime, Recipe> entry : mealAgenda.entrySet()) {
                LocalDateTime date = entry.getKey();
                if (date.toLocalDate() != lastDate) {
                    String day = lastDate.getDayOfWeek().toString();
                    String line = String.format("| %s | %s | %s | %s |\n", day, breakfast, lunch, dinner);
                    calendarOut.write(line.getBytes());
                    lastDate = date.toLocalDate();
                    breakfast = lunch = dinner = null;
                }
                String recipe = entry.getValue().name;
                if (date.getHour() < 11)
                    breakfast = recipe;
                else if (date.getHour() < 15)
                    lunch = recipe;
                else
                    dinner = recipe;
            }
        } catch (IOException x) {
            System.err.println(x);
        }
    }
}
