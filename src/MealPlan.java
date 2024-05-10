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

import java.util.Arrays;
import java.util.Iterator;

public class MealPlan {
    public TreeMap<LocalDateTime, Recipe> mealAgenda;
    private List<Recipe> recipes;

    MealPlan(List<String> lines, List<Recipe> recipes) {
        this.recipes = recipes;
        mealAgenda = new TreeMap<>();
        System.out.println("MealPlan constructor with lines " + lines.toString());
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] lineSegs = line.split("\\s*\\|\\s*", -1);
            lineSegs = Arrays.copyOfRange(lineSegs, 1, lineSegs.length - 1); // Ignoring first and last empty strings
            switch (i) {
                case 0:
                    assert lineSegs[0].equalsIgnoreCase("day");
                    break;
                case 1:
                    assert lineSegs[0].contains("-");
                    break;
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
            for (Recipe r : recipes) {
                if (r.name.equalsIgnoreCase(lineSegs[i])) {
                    recipe = r;
                }
            }
            if (recipe == null)
                continue;
            LocalDateTime mealDateTime = LocalDateTime.of(date, Config.MEAL_TIMES[i - 1]);
            mealAgenda.put(mealDateTime, recipe);
            System.out.println("Added " + recipe.name + " at " + mealDateTime.format(Agenda.FORMATTER_WEEK));
        }
    }

    public Iterator<Map.Entry<LocalDateTime, Recipe>> getIt() {
        return mealAgenda.entrySet().iterator();
    }

    private void outputDay(OutputStream calendarOut, String breakfast, String lunch, String dinner, LocalDate lastDate,
            LocalDate thisDate)
            throws IOException {
        if (breakfast == null)
            breakfast = "";
        if (lunch == null)
            lunch = "";
        if (dinner == null)
            dinner = "";
        String line = String.format("| %s | %s | %s | %s |\n", lastDate.getDayOfWeek().toString(), breakfast, lunch,
                dinner);
        calendarOut.write(line.getBytes());
        while (lastDate.plusDays(1).isBefore(thisDate)) {
            // Fill in missing days
            lastDate = lastDate.plusDays(1);
            line = String.format("| %s | %s | %s | %s |\n", lastDate.getDayOfWeek().toString(), "", "", "");
            calendarOut.write(line.getBytes());
        }
    }

    public void output(OutputStream calendarOut, String postFix) throws IOException {
        Path mealRecordPath = Paths.get("bin", "meal-" + postFix + ".csv");
        OutputStream mealRecordOut = new BufferedOutputStream(
                Files.newOutputStream(mealRecordPath, CREATE, WRITE));
        mealRecordOut.write("Time,Recipe\n".getBytes());
        for (Map.Entry<LocalDateTime, Recipe> entry : mealAgenda.entrySet()) {
            LocalDateTime mealDateTime = entry.getKey();
            Recipe recipe = entry.getValue();
            String line = String.format("%s,%s\n",
                    mealDateTime.format(Agenda.FORMATTER_WEEK),
                    recipe.name);
            mealRecordOut.write(line.getBytes());
        }
        calendarOut.write("# Meal Plan\n\n".getBytes());
        calendarOut.write("## Meals\n\n".getBytes());
        calendarOut.write("The week from %s to %s.\n\n".formatted(
                mealAgenda.firstKey().format(Agenda.FORMATTER_DATE),
                mealAgenda.firstKey().plusDays(7).format(Agenda.FORMATTER_DATE))
                .getBytes());
        calendarOut.write(
                "Changes in this section will only reflect in other sections after regenerating agenda with this markdown file.\n\n"
                        .getBytes());
        calendarOut.write("| Day | Breakfast | Lunch | Dinner |\n".getBytes());
        calendarOut.write("| --- | --------- | ----- | ------ |\n".getBytes());
        LocalDate lastDate = mealAgenda.firstKey().toLocalDate();
        String breakfast = null, lunch = null, dinner = null;
        for (Map.Entry<LocalDateTime, Recipe> entry : mealAgenda.entrySet()) {
            LocalDateTime date = entry.getKey();
            if (!date.toLocalDate().equals(lastDate)) {
                // Wrap up the previous day
                outputDay(calendarOut, breakfast, lunch, dinner, lastDate, date.toLocalDate());
                lastDate = date.toLocalDate();
                breakfast = lunch = dinner = null;
            }
            String recipeName = entry.getValue().name;
            if (date.getHour() < 11)
                breakfast = recipeName;
            else if (date.getHour() < 15)
                lunch = recipeName;
            else
                dinner = recipeName;
        }
        outputDay(calendarOut, breakfast, lunch, dinner, lastDate, mealAgenda.firstKey().toLocalDate().plusDays(7));
        calendarOut.write("\n".getBytes());
        mealRecordOut.close();
    }
}
