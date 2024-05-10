import java.time.LocalTime;

public class Config {
    public static final LocalTime[] MEAL_TIMES = new LocalTime[] {
        LocalTime.of(9, 0),
        LocalTime.of(13, 0),
        LocalTime.of(19, 0)
    };
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