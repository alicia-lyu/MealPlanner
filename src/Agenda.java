import java.nio.file.*;
import java.sql.Time;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.io.*;
import static java.nio.file.StandardOpenOption.*;

/**
 * Create a weekly agenda based on
 */
public class Agenda {
    public static String[] DAYS = new String[] { "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday" };
    public static String[] MEALS = new String[] { "breakfast", "lunch", "dinner" };
    public List<String> shoppingCart;
    public Map<Integer, Map<Time, String>> prepareInAdvance; // 7 days // TODO: change all to localDateTime
    public Map<Integer, Map<Time, String>> mealAgenda;
    public int firstDayOfWeek;
    private Recipe[] recipes;

    Agenda(Recipe[] recipes, Map<Time, String>[] completedSteps, String weeklyPlanFileName) {
        this.recipes = recipes;
        parseWeeklyPlan(weeklyPlanFileName);
        updateCompletedSteps(completedSteps);
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
                BufferedReader br = new BufferedReader(inputStream);) {
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
                updateAgendaWithLine(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateAgendaWithLine(String line) {
        int currentDay = -1;
        String[] parts = line.split("[\s:]");
        // Day of the week
        if (parts[0].equals("#")) {
            assert parts.length == 2;
            for (int i = 0; i < 7; i++) {
                if (parts[1].equalsIgnoreCase(DAYS[i])) {
                    if (currentDay == -1) {
                        firstDayOfWeek = i;
                    }
                    if (currentDay == i) {
                        throw new RuntimeException("Duplicate day of the week: " + parts[1]);
                    }
                    currentDay = i;
                    System.out.println("Setting current day to " + DAYS[i]);
                    break;
                }
            }
            throw new RuntimeException("Invalid day of the week: " + parts[1]);
        } else
        // A meal
        if (parts[0].equals("-")
                || parts[0].equals("*")
                || parts[0].equals("+")) {
            Recipe recipe = null;
            Time mealTime = null;
            for (int i = 0; i < 3; i++) {
                if (line.contains(MEALS[i])) {
                    String recipeString = parts[-1];
                    mealTime = Config.MEAL_TIMES[i];
                    for (Recipe r : recipes) {
                        if (r.name.equalsIgnoreCase(recipeString)) {
                            recipe = r;
                            System.out.printf("Processing recipe %s for %t", recipeString, mealTime);
                            mealAgenda.getOrDefault(currentDay, new HashMap<>()).put(mealTime, recipeString);
                            break;
                        }
                    }
                }
            }
            if (recipe == null || mealTime == null) {
                throw new RuntimeException("Invalid recipe from " + line);
            }
            // Add ingredients and preparation steps to shoppingCart and prepareInAdvance
            shoppingCart.addAll(recipe.ingredients);

            for (Map.Entry<Duration, String> entry : recipe.preparationSteps.entrySet()) {
                Duration d = entry.getKey();
                String s = entry.getValue();
                Duration sameDayOffset = d;
                int subtractedDays = 0;
                while (sameDayOffset.toDays() > 0) {
                    sameDayOffset = sameDayOffset.minusDays(1);
                    subtractedDays++;
                }
                Time t = new Time(mealTime.getTime() - sameDayOffset.toMillis());
                prepareInAdvance.getOrDefault(currentDay - subtractedDays, new HashMap<>()).put(t, s);
            }
        }
    }

    private void updateCompletedSteps(Map<Time, String>[] completedSteps) {
        // Read and parse previous agenda (.md), if any, and memorize accomplished steps
    }

    public void output() {
        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd'T'HH:mm:ss");
        String formattedTime = date.format(formatter);
        int weekOfYear = date.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
        Path mealRecordPath = Paths.get("bin", "meal-" + weekOfYear + "-" + formattedTime + ".csv");
        Path cartRecordPath = Paths.get("bin", "cart-" + weekOfYear + "-" + formattedTime + ".csv");
        Path prepareRecordPath = Paths.get("bin", "agenda-" + weekOfYear + "-" + formattedTime + ".csv");
        Path calendarPath = Paths.get("out", "agenda-" + weekOfYear + "-" + formattedTime + ".md");

        try (OutputStream mealRecordOut = new BufferedOutputStream(
                Files.newOutputStream(cartRecordPath, CREATE, WRITE));
            OutputStream calendarOut = new BufferedOutputStream(
                Files.newOutputStream(calendarPath, CREATE, WRITE));
        ) {
            mealRecordOut.write("Day,Time,Meal,Recipe\n".getBytes());
            List<Integer> days = mealAgenda.keySet().stream().sorted().collect(Collectors.toList());
            List<Integer> orderedDays = days.subList(firstDayOfWeek, days.size());
            orderedDays.addAll(days.subList(0, firstDayOfWeek));
            for (int day : orderedDays) {
                Map<Time, String> meals = mealAgenda.get(day);
                for (Time t : Config.MEAL_TIMES) {
                    String recipe = meals.get(t);
                    DateTimeFormatter formatterShort = DateTimeFormatter.ofPattern("HH:mm");
                    String formattedTimeShort = t.toLocalTime.format(formatterShort);
                    mealRecordOut.write(String.format("%s,%t,%s,%s\n", DAYS[day], t, recipe).getBytes());
                }
            }
        } catch (IOException x) {
            System.err.println(x);
        }

        try (OutputStream cartRecordOut = new BufferedOutputStream(
                Files.newOutputStream(cartRecordPath, CREATE, WRITE));
            OutputStream calendarOut = new BufferedOutputStream(
                Files.newOutputStream(calendarPath, CREATE, APPEND));
        ) {
            
        } catch (IOException x) {
            System.err.println(x);
        }

        try (OutputStream agendaRecordOut = new BufferedOutputStream(
            Files.newOutputStream(prepareRecordPath, CREATE, WRITE));
            OutputStream calendarOut = new BufferedOutputStream(
            Files.newOutputStream(calendarPath, APPEND, WRITE));
        ) {

        } catch (IOException x) {
            System.err.println(x);
        }
    }
}
