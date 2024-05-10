import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Config {
    public static DateTimeFormatter FORMATTER_LONG = DateTimeFormatter.ofPattern("'week'w_MM-dd'T'HH:mm:ss");
    public static DateTimeFormatter FORMATTER_DATE = DateTimeFormatter.ofPattern("E, MM-dd");
    public static DateTimeFormatter FORMATTER_WEEK = DateTimeFormatter.ofPattern("E HH:mm");
    public static DateTimeFormatter FORMATTER_DAY = DateTimeFormatter.ofPattern("HH:mm");
    public static final LocalTime[] MEAL_TIMES = new LocalTime[] {
        LocalTime.of(9, 0),
        LocalTime.of(13, 0),
        LocalTime.of(19, 0)
    };
    public static LocalDate getDateOfNearestDay(int dayOfWeek) {
        LocalDate date = LocalDate.now();
        int currentDayOfWeek = date.getDayOfWeek().getValue();
        int daysToNearestDay = dayOfWeek - currentDayOfWeek;
        if (daysToNearestDay < 0) {
            daysToNearestDay += 7;
        }
        return date.plusDays(daysToNearestDay);
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

    public LocalTime getTime() {
        switch (this) {
            case BREAKFAST:
                return Config.MEAL_TIMES[0];
            case LUNCH:
                return Config.MEAL_TIMES[1];
            case DINNER:
                return Config.MEAL_TIMES[2];
            default:
                throw new RuntimeException("Invalid meal: " + this);
        }
    }
}