package automail;

public class Clock {
	
	/** Represents the current time **/
    private static int Time = 0;

    public static int Time() {
    	return Time;
    }
    
    public static void Tick() {
    	Time++;
    }
}
