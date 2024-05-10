enum Day {
    SUNDAY(0),
    MONDAY(1),
    TUESDAY(2),
    WEDNESDAY(3),
    THURSDAY(4),
    FRIDAY(5),
    SATURDAY(6);

    public int value;

    Day(int dayOfWeek) {
        this.value = dayOfWeek;
    }

    public static Day getDay(String dayOfWeek) {
        dayOfWeek = dayOfWeek.toLowerCase();
        switch (dayOfWeek) {
            case "sunday":
                return SUNDAY;
            case "sun":
                return SUNDAY;
            case "monday":
                return MONDAY;
            case "mon":
                return MONDAY;
            case "tuesday":
                return TUESDAY;
            case "tue":
                return TUESDAY;
            case "wednesday":
                return WEDNESDAY;
            case "wed":
                return WEDNESDAY;
            case "thursday":
                return THURSDAY;
            case "thu":
                return THURSDAY;
            case "friday":
                return FRIDAY;
            case "fri":
                return FRIDAY;
            case "saturday":
                return SATURDAY;
            case "sat":
                return SATURDAY;
            default:
                throw new RuntimeException("Invalid day of the week: " + dayOfWeek);
        }
    }
}