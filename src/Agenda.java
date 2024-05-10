import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

import java.io.*;
import static java.nio.file.StandardOpenOption.*;

enum AgendaSection {
    MEALS("meals"),
    SHOPPING_LIST("shopping list"),
    PREPARE_IN_ADVANCE("prepare in advance");

    public String value;

    AgendaSection(String section) {
        this.value = section;
    }

    public static AgendaSection getSection(String sectionInLine) {
        sectionInLine = sectionInLine.toLowerCase();
        for (AgendaSection s : AgendaSection.values()) {
            if (sectionInLine.contains(s.value)) {
                return s;
            }
        }
        System.out.println("Ignored invalid section: " + sectionInLine);
        return null;
    }
}

/**
 * Create a weekly agenda based on
 */
public class Agenda {
    public static DateTimeFormatter FORMATTER_LONG = DateTimeFormatter.ofPattern("'week'w_MM-dd'T'HH:mm:ss");
    public static DateTimeFormatter FORMATTER_DATE = DateTimeFormatter.ofPattern("E, MM-dd");
    public static DateTimeFormatter FORMATTER_WEEK = DateTimeFormatter.ofPattern("E HH:mm");
    public static DateTimeFormatter FORMATTER_DAY = DateTimeFormatter.ofPattern("HH:mm");
    private MealPlan mealPlan;
    private ShoppingList shoppingList;
    private PrepareInAdvance prepareInAdvance;
    private List<Recipe> recipes;

    Agenda(List<Recipe> recipes, Path weeklyPlanPath) throws IOException {
        this.recipes = recipes;
        parseWeeklyPlan(weeklyPlanPath);
        updateWithMealPlan();
    }

    private void parseWeeklyPlan(Path weeklyPlanPath) throws IOException {
        // Parse the file and populate weeklyAgenda
        List<String> lines = Files.readAllLines(weeklyPlanPath);
        List<String> currentPart = new ArrayList<>();
        AgendaSection currentSection = null;
        for (String line : lines) {
            line = line.toLowerCase();
            if (line.startsWith("##")) {
                System.out.println("Processing " + line);
                AgendaSection section = AgendaSection.getSection(line);
                if (section == null)
                    continue;
                assert section != currentSection;
                List<String> clonedCurrentPart = new ArrayList<>(currentPart);
                wrapUpSection(currentSection, clonedCurrentPart);
                currentPart.clear();
                currentSection = section;
            } else if (line.startsWith("- [ ]") || line.startsWith("- [x]")) {
                currentPart.add(line);
            } else if (currentSection == AgendaSection.MEALS && line.startsWith("|")) {
                currentPart.add(line);
            } else {
                System.out.println("Ignored " + line);
            }
        }
        wrapUpSection(currentSection, currentPart);
    }

    private void wrapUpSection(AgendaSection section, List<String> lines) throws IOException {
        if (section != null)
            switch (section) {
                case MEALS:
                    mealPlan = new MealPlan(lines, recipes);
                    break;
                case SHOPPING_LIST:
                    shoppingList = new ShoppingList(lines);
                    break;
                case PREPARE_IN_ADVANCE:
                    prepareInAdvance = new PrepareInAdvance(lines);
                    break;
            }
    }

    private void updateWithMealPlan() {
        Iterator<Map.Entry<LocalDateTime, Recipe>> it = mealPlan.getIt();
        while (it.hasNext()) {
            Map.Entry<LocalDateTime, Recipe> entry = it.next();
            LocalDateTime mealDateTime = entry.getKey();
            Recipe recipe = entry.getValue();
            shoppingList.updateWithRecipe(mealDateTime, recipe);
            prepareInAdvance.updateWithRecipe(mealDateTime, recipe);
        }
    }

    public static LocalDate getDateOfNearestDay(int dayOfWeek) {
        LocalDate date = LocalDate.now();
        int currentDayOfWeek = date.getDayOfWeek().getValue();
        int daysToNearestDay = dayOfWeek - currentDayOfWeek;
        if (daysToNearestDay < 0) {
            daysToNearestDay += 7;
        }
        return date.plusDays(daysToNearestDay);
    }

    public void output() throws IOException {
        LocalDateTime dateNow = LocalDateTime.now();
        String postFix = dateNow.format(FORMATTER_LONG);
        Path calendarPath = Paths.get("out", "agenda-" + postFix + ".md");
        System.out.println("Outputting to " + calendarPath);
        OutputStream calendarOut = new BufferedOutputStream(
                Files.newOutputStream(calendarPath, CREATE, TRUNCATE_EXISTING, WRITE));
        mealPlan.output(calendarOut, postFix);
        shoppingList.output(calendarOut, postFix);
        prepareInAdvance.output(calendarOut, postFix);
        calendarOut.close();
    }

    public static void main(String[] args) throws IOException {
        Path templatePath = Path.of("out", "template.md");
        OutputStream templateOut = Files.newOutputStream(templatePath, CREATE, TRUNCATE_EXISTING, WRITE);
        templateOut.write("# Meal Plan\n\n".getBytes());
        templateOut.write("## Meals\n\n".getBytes());
        templateOut.write(
                "Changes in this section will only reflect in other sections after regenerating agenda with this markdown file.\n\n"
                        .getBytes());
        templateOut.write("| Day | Breakfast | Lunch | Dinner |\n".getBytes());
        templateOut.write("| --- | --------- | ----- | ------ |\n".getBytes());
        templateOut.write("| Sun |  |  |  |\n".getBytes());
        templateOut.write("| Mon |  |  |  |\n".getBytes());
        templateOut.write("| Tue |  |  |  |\n".getBytes());
        templateOut.write("| Wed |  |  |  |\n".getBytes());
        templateOut.write("| Thu |  |  |  |\n".getBytes());
        templateOut.write("| Fri |  |  |  |\n".getBytes());
        templateOut.write("| Sat |  |  |  |\n\n".getBytes());
        templateOut.write("## Shopping List\n\n".getBytes());
        templateOut.write("Changes in this section not honored except checking the boxes.\n\n".getBytes());
        templateOut.write("## Prepare In Advance\n\n".getBytes());
        templateOut.write("Changes in this section not honored including checking the boxes.\n\n".getBytes());
        templateOut.close();
    }
}
