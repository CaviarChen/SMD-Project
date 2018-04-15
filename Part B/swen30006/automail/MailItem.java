package automail;

// import java.util.UUID;

/**
 * Represents a mail item
 */
public class MailItem {
	
    /** Represents the destination floor to which the mail is intended to go */
    protected final int destination_floor;
    /** The mail identifier */
    protected final String id;
    /** The time the mail item arrived */
    protected final int arrival_time;
    /** The weight in grams of the mail item */
    protected final int weight;

    /**
     * Constructor for a MailItem
     * @param dest_floor the destination floor intended for this mail item
     * @param arrival_time the time that the mail arrived
     * @param weight the weight of this mail item
     */
    public MailItem(int dest_floor, int arrival_time, int weight){
        this.destination_floor = dest_floor;
        this.id = String.valueOf(hashCode());
        this.arrival_time = arrival_time;
        this.weight = weight;
    }

    @Override
    public String toString(){
        return String.format("Mail Item:: ID: %11s | Arrival: %4d | Destination: %2d | Weight: %4d", id, arrival_time, destination_floor, weight );
    }

    /**
     *
     * @return the destination floor of the mail item
     */
    public int getDestFloor() {
        return destination_floor;
    }
    
    /**
     *
     * @return the ID of the mail item
     */
    public String getId() {
        return id;
    }

    /**
     *
     * @return the arrival time of the mail item
     */
    public int getArrivalTime(){
        return arrival_time;
    }

    /**
    *
    * @return the weight of the mail item
    */
   public int getWeight(){
       return weight;
   }
}
