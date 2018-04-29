// Group 44
package automail;

public class Clock {

    /* Represents the current time */
    private static int Time = 0;

    /**
     * get current time
     * @return current time
     */
    public static int Time() {
        return Time;
    }

    /**
     * advanced the clock by one unit
     */
    public static void Tick() {
        Time++;
    }
}
