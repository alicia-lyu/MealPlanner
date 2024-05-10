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
    MEAL_PLAN("meal plan"),
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
    public static DateTimeFormatter FORMATTER_LONG = DateTimeFormatter.ofPattern("w--MM-dd'T'HH:mm:ss");
    public static DateTimeFormatter FORMATTER_SHORT = DateTimeFormatter.ofPattern("E--HH:mm");
    public static DateTimeFormatter FORMATTER_MIN = DateTimeFormatter.ofPattern("HH:mm");
    private MealPlan mealPlan;
    private ShoppingList shoppingList;
    private PrepareInAdvance prepareInAdvance;
    private List<Recipe> recipes;

    Agenda(List<Recipe> recipes, String weeklyPlanFileName) {
        this.recipes = recipes;
        parseWeeklyPlan(weeklyPlanFileName);
        updateWithMealPlan();
    }

    private void parseWeeklyPlan(String weeklyPlanFileName) {
        // Parse the file and populate weeklyAgenda
        InputStreamReader inputStream;
        try {
            File file = new File(weeklyPlanFileName);
            inputStream = new FileReader(file);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        try (
            BufferedReader br = new BufferedReader(inputStream);
        ) {
            List<String> currentPart = new ArrayList<>();
            AgendaSection currentSection = null;
            while (true) {
                String line;
                try {
                    line = br.readLine();
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                if (line == null) break;
                // Parse the line
                line = line.toLowerCase();
                String[] lineSegs = line.split("[\s:]");
                if (lineSegs.length == 0) continue;

                if (lineSegs[0].contains("##")) {
                    System.out.println("Processing" + line);
                    AgendaSection section = AgendaSection.getSection(line);
                    if (section == null) continue;
                    if (section != currentSection) { // process the last section
                        List<String> clonedCurrentPart = new ArrayList<>(currentPart);
                        switch (currentSection) {
                            case MEAL_PLAN:
                                mealPlan = new MealPlan(clonedCurrentPart, recipes);
                            case SHOPPING_LIST:
                                shoppingList = new ShoppingList(clonedCurrentPart);
                            case PREPARE_IN_ADVANCE:
                                prepareInAdvance = new PrepareInAdvance(clonedCurrentPart);
                        }
                        currentPart.clear();
                        currentSection = section;
                    }
                } else if (
                    lineSegs[0].equals("-[ ]") ||
                    lineSegs[0].equals("-[x]")) 
                {
                    currentPart.add(line);
                } else if (
                    currentSection == AgendaSection.MEAL_PLAN
                )
                    currentPart.add(line);
                else {
                    System.out.println("Ignored " + line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    public void output() {
        LocalDate dateNow = LocalDate.now();
        String postFix = dateNow.format(FORMATTER_LONG);
        Path calendarPath = Paths.get("out", "agenda-" + postFix + ".md");
        try (
            OutputStream calendarOut = new BufferedOutputStream(
                Files.newOutputStream(calendarPath, CREATE, WRITE))
        ) {
            mealPlan.output(calendarOut, postFix);
            shoppingList.output(calendarOut, postFix);
            prepareInAdvance.output(calendarOut, postFix);
        } catch (IOException x) {
            System.err.println(x);
        }
    }
}
