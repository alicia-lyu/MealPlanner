import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.io.*;
import static java.nio.file.StandardOpenOption.*;

enum Day {
    MONDAY("monday"),
    TUESDAY("tuesday"),
    WEDNESDAY("wednesday"),
    THURSDAY("thursday"),
    FRIDAY("friday"),
    SATURDAY("saturday"),
    SUNDAY("sunday");

    public String value;

    Day(String dayOfWeek) {
        this.value = dayOfWeek;
    }

    public static Day getDay(String dayOfWeek) {
        for (Day day : Day.values()) {
            if (day.value.equalsIgnoreCase(dayOfWeek)) {
                return day;
            }
        }
        throw new RuntimeException("Invalid day of the week: " + dayOfWeek);
    }
}

enum Meal {
    BREAKFAST("breakfast"),
    LUNCH("lunch"),
    DINNER("dinner");

    public String value;

    Meal(String meal) {
        this.value = meal;
    }

    public static Meal getMeal(String meal) {
        for (Meal m : Meal.values()) {
            if (meal.equalsIgnoreCase(m.value)) {
                return m;
            }
        }
        throw new RuntimeException("Invalid meal: " + meal);
    }
}

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
    public Map<String, List<Recipe>> shoppingCart;
    public TreeMap<LocalDateTime, List<String>> prepareInAdvance; // 7 days // TODO: change all to localDateTime
    public TreeMap<LocalDateTime, Recipe> mealAgenda;
    private Recipe[] recipes;
    private Map<LocalDateTime, String> completedSteps;
    private Map<LocalDateTime, String> newSteps;
    private Map<LocalDateTime, String> shoppedItems;
    private Map<LocalDateTime, String> newItems;

    Agenda(Recipe[] recipes, String weeklyPlanFileName) {
        this.recipes = recipes;
        parseWeeklyPlan(weeklyPlanFileName);
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
                if (line == null)
                    break;
                // Parse the line
                line = line.toLowerCase();
                String[] lineSegs = line.split("[\s:]");
                if (lineSegs.length == 0) continue;

                if (lineSegs[0].contains("##")) {
                    System.out.println("Processing" + line);
                    AgendaSection section = AgendaSection.getSection(line);
                    if (section == null) continue;
                    if (section != currentSection) { // process the last section

                    }
                } else if (
                    lineSegs[0].equals("-[ ]") ||
                    lineSegs[0].equals("-[x]")) 
                {
                    currentPart.add(line);
                } else {
                    System.out.println("Ignored " + line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void updateWithRecipe(LocalDateTime mealDateTime, Recipe recipe) {
        System.out.printf("Processing recipe %s for %t", recipe.name, mealDateTime);
        // Update mealAgenda
        if (mealAgenda.containsKey(mealDateTime)) {
            throw new RuntimeException("Duplicate meal for " + mealDateTime);
        }
        mealAgenda.put(mealDateTime, recipe);
        // Update shoppingCart
        for (String ingredient : recipe.ingredients) {
            shoppingCart.putIfAbsent(ingredient, new ArrayList<>());
            shoppingCart.get(ingredient).add(recipe);
        }
        // Update prepareInAdvance
        for (Map.Entry<Duration, String> entry : recipe.preparationSteps.entrySet()) {
            Duration d = entry.getKey();
            String s = entry.getValue();
            LocalDateTime preparationTime = mealDateTime.minus(d);
            prepareInAdvance.putIfAbsent(preparationTime, new ArrayList<>());
            prepareInAdvance.get(preparationTime).add(s);
        }
    }

    private static LocalDate getDateOfNearestDay(int dayOfWeek) {
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
        String formattedTime = dateNow.format(FORMATTER_LONG);
        int weekOfYear = dateNow.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
        Path mealRecordPath = Paths.get("bin", "meal-" + weekOfYear + "-" + formattedTime + ".csv");
        Path cartRecordPath = Paths.get("bin", "cart-" + weekOfYear + "-" + formattedTime + ".csv");
        Path prepareRecordPath = Paths.get("bin", "agenda-" + weekOfYear + "-" + formattedTime + ".csv");
        Path calendarPath = Paths.get("out", "agenda-" + weekOfYear + "-" + formattedTime + ".md");

        try (OutputStream mealRecordOut = new BufferedOutputStream(
                Files.newOutputStream(mealRecordPath, CREATE, WRITE));
            OutputStream calendarOut = new BufferedOutputStream(
                Files.newOutputStream(calendarPath, CREATE, WRITE));
        ) {
            mealRecordOut.write("Time,Recipe\n".getBytes());
            for (Map.Entry<LocalDateTime, Recipe> entry : mealAgenda.entrySet()) {
                LocalDateTime mealDateTime = entry.getKey();
                Recipe recipe = entry.getValue();
                String line = String.format("%s,%s\n", 
                    mealDateTime.format(FORMATTER_SHORT),
                    recipe.name);
                mealRecordOut.write(line.getBytes());
            }
            calendarOut.write("# Meal Plan\n".getBytes());
            calendarOut.write("## Meals\n".getBytes());
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
                if (date.getHour() < 11) breakfast = recipe;
                else if (date.getHour() < 15) lunch = recipe;
                else dinner = recipe;
            }
        } catch (IOException x) {
            System.err.println(x);
        }

        try (OutputStream cartRecordOut = new BufferedOutputStream(
                Files.newOutputStream(cartRecordPath, CREATE, WRITE));
            OutputStream calendarOut = new BufferedOutputStream(
                Files.newOutputStream(calendarPath, CREATE, APPEND));
        ) {
            cartRecordOut.write("Ingredients\n".getBytes());
            for (Map.Entry<String, List<Recipe>> entry : shoppingCart.entrySet()) {
                String ingredient = entry.getKey();
                List<Recipe> recipes = entry.getValue();
                String line = String.format("%s,'%s'\n", ingredient, recipes.toString());
                cartRecordOut.write(line.getBytes());
            }
            calendarOut.write("## Shopping List\n".getBytes());
            for (Map.Entry<String, List<Recipe>> entry : shoppingCart.entrySet()) {
                String ingredient = entry.getKey();
                String line = String.format("-[ ] %s for %s\n", ingredient, recipes.toString()); // TODO: use a stock file to check off
                calendarOut.write(line.getBytes());
            }
        } catch (IOException x) {
            System.err.println(x);
        }

        try (OutputStream agendaRecordOut = new BufferedOutputStream(
            Files.newOutputStream(prepareRecordPath, CREATE, WRITE));
            OutputStream calendarOut = new BufferedOutputStream(
            Files.newOutputStream(calendarPath, APPEND, WRITE));
        ) {
            agendaRecordOut.write("Time,Step\n".getBytes());
            for (Map.Entry<LocalDateTime, List<String>> entry : prepareInAdvance.entrySet()) {
                LocalDateTime preparationTime = entry.getKey();
                List<String> steps = entry.getValue();
                for (String step : steps) {
                    String line = String.format("%s,%s\n", preparationTime.format(FORMATTER_SHORT), step);
                    agendaRecordOut.write(line.getBytes());
                }
            }
            calendarOut.write("## Prepare In Advance\n".getBytes());
            LocalDate lastDate = null;
            for (Map.Entry<LocalDateTime, List<String>> entry : prepareInAdvance.entrySet()) {
                LocalDateTime preparationTime = entry.getKey();
                if (preparationTime.toLocalDate() != lastDate) { // Start a new h3
                    String day = preparationTime.getDayOfWeek().toString();
                    String line = String.format("### %s\n", day);
                    calendarOut.write(line.getBytes());
                    lastDate = preparationTime.toLocalDate();
                }
                List<String> steps = entry.getValue();
                for (String step : steps) {
                    String line = String.format("-[ ] %s %s\n", preparationTime.format(FORMATTER_MIN), step); 
                    // TODO: use a stock file and this.completedSteps to check off
                    calendarOut.write(line.getBytes());
                }
            }
        } catch (IOException x) {
            System.err.println(x);
        }
    }
}
