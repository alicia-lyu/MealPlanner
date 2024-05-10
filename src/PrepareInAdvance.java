import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import static java.nio.file.StandardOpenOption.*;

public class PrepareInAdvance {
    public TreeMap<LocalDateTime, List<String>> prepareInAdvance;

    PrepareInAdvance(List<String> lines) {
        System.out.println("PrepareInAdvance constructor ignored lines " + lines.toString());
        prepareInAdvance = new TreeMap<>();
    }

    public void updateWithRecipe(LocalDateTime mealDateTime, Recipe recipe) {
        for (Map.Entry<Duration, String> entry : recipe.preparationSteps.entrySet()) {
            Duration d = entry.getKey();
            String s = entry.getValue();
            LocalDateTime preparationTime = mealDateTime.plus(d);
            prepareInAdvance.putIfAbsent(preparationTime, new ArrayList<>());
            prepareInAdvance.get(preparationTime).add(s);
        }
    }

    public void output(OutputStream calendarOut, String postFix) throws IOException {
        Path prepareRecordPath = Paths.get("bin", "agenda-" + postFix + ".csv");
        OutputStream agendaRecordOut = new BufferedOutputStream(
                Files.newOutputStream(prepareRecordPath, CREATE, WRITE));
        agendaRecordOut.write("Time,Step\n".getBytes());
        for (Map.Entry<LocalDateTime, List<String>> entry : prepareInAdvance.entrySet()) {
            LocalDateTime preparationTime = entry.getKey();
            List<String> steps = entry.getValue();
            for (String step : steps) {
                String line = String.format("%s,%s\n", preparationTime.format(Agenda.FORMATTER_SHORT), step);
                agendaRecordOut.write(line.getBytes());
            }
        }
        calendarOut.write("## Prepare In Advance\n\n".getBytes());
        calendarOut.write("Changes in this section not honored including checking the boxes.\n\n".getBytes());
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
                String line = String.format("-[ ] %s %s\n", preparationTime.format(Agenda.FORMATTER_MIN), step);
                calendarOut.write(line.getBytes());
            }
        }
    }

}
