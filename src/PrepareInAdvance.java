import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import static java.nio.file.StandardOpenOption.*;

public class PrepareInAdvance {
    public TreeMap<LocalDateTime, Map<String, Recipe>> prepareInAdvance;

    PrepareInAdvance(List<String> lines) {
        System.out.println("PrepareInAdvance constructor ignored lines " + lines.toString());
        prepareInAdvance = new TreeMap<>();
    }

    public void updateWithRecipe(LocalDateTime mealDateTime, Recipe recipe) {
        for (Map.Entry<Duration, String> entry : recipe.preparationSteps.entrySet()) {
            Duration d = entry.getKey();
            String s = entry.getValue();
            LocalDateTime preparationTime = mealDateTime.plus(d);
            prepareInAdvance.putIfAbsent(preparationTime, new HashMap<>());
            prepareInAdvance.get(preparationTime).put(s, recipe);
        }
    }

    public void output(OutputStream calendarOut, String postFix) throws IOException {
        Path prepareRecordPath = Paths.get("bin", "agenda-" + postFix + ".csv");
        OutputStream agendaRecordOut = new BufferedOutputStream(
                Files.newOutputStream(prepareRecordPath, CREATE, WRITE));
        agendaRecordOut.write("Time,Step\n".getBytes());

        LocalDate lastDate = LocalDate.MIN;
        calendarOut.write("## Prepare In Advance\n\n".getBytes());
        calendarOut.write("Changes in this section will not be honored including checking the boxes.\n".getBytes());

        for (Map.Entry<LocalDateTime, Map<String, Recipe>> entry : prepareInAdvance.entrySet()) {
            LocalDateTime preparationTime = entry.getKey();
            if (preparationTime.toLocalDate().isAfter(lastDate)) { 
                // Start a new h3
                String agendaLine = String.format("\n### %s\n\n", preparationTime.format(Config.FORMATTER_DATE));
                calendarOut.write(agendaLine.getBytes());
                lastDate = preparationTime.toLocalDate();
            }

            Map<String, Recipe> steps = entry.getValue();
            for (Map.Entry<String, Recipe> stepEntry : steps.entrySet()) {
                String step = stepEntry.getKey();
                Recipe recipe = stepEntry.getValue();
                String recordLine = String.format("%s,%s,%s\n", preparationTime.format(Config.FORMATTER_WEEK), step, recipe.name);
                agendaRecordOut.write(recordLine.getBytes());

                String line = String.format("- [ ] %s %s for %s\n", preparationTime.format(Config.FORMATTER_DAY), step, recipe.name);
                calendarOut.write(line.getBytes());
            }
        }
        agendaRecordOut.close();
    }

}
