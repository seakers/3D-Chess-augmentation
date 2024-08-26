package tatc.util;

/**
 * Class that represents a date in time
 */
public class AbsoluteDate {

    /**
     * The year
     */
    private final int year;

    /**
     * The month
     */
    private final int month;

    /**
     * The day
     */
    private final int day;

    /**
     * The hour
     */
    private final int hour;

    /**
     * The minute
     */
    private final int minute;

    /**
     * The second
     */
    private final int second;

    /**
     * Constructs a date object
     * @param year the year
     * @param month the month
     * @param day the day
     * @param hour the hour
     * @param minute the minute
     * @param second the second
     */
    public AbsoluteDate(int year, int month, int day, int hour, int minute, int second) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }

    /**
     * Gets the year
     * @return the year
     */
    public int getYear() {
        return year;
    }

    /**
     * Gets the month
     * @return the month
     */
    public int getMonth() {
        return month;
    }

    /**
     * Gets the day
     * @return the day
     */
    public int getDay() {
        return day;
    }

    /**
     * Gets the hour
     * @return the hour
     */
    public int getHour() {
        return hour;
    }

    /**
     * Gets the minute
     * @return the hour
     */
    public int getMinute() {
        return minute;
    }

    /**
     * Gets the second
     * @return the second
     */
    public int getSecond() {
        return second;
    }
}
