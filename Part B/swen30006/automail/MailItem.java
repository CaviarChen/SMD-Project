package automail;

import java.util.Map;
import java.util.TreeMap;

/**
 * Represents a mail item
 */
public class MailItem {

    // for hashcode
    static int count = 0;
    static Map<Integer, Integer> hashMap = new TreeMap<Integer, Integer>();


    // Represents the destination floor to which the mail is intended to go
    protected final int destinationFloor;
    // The mail identifier
    protected final String id;
    // The time the mail item arrived
    protected final int arrivalTime;
    // The weight in grams of the mail item
    protected final int weight;
    // true when this item is delivered
    private boolean isDelivered;

    /**
     * Constructor for a MailItem
     *
     * @param destFloor   the destination floor intended for this mail item
     * @param arrivalTime the time that the mail arrived
     * @param weight       the weight of this mail item
     */
    public MailItem(int destFloor, int arrivalTime, int weight) {
        this.destinationFloor = destFloor;
        this.id = String.valueOf(hashCode());
        this.arrivalTime = arrivalTime;
        this.weight = weight;
        this.isDelivered = false;
    }

    @Override
    public String toString() {
        return String.format("Mail Item:: ID: %11s | Arrival: %4d | Destination: %2d | Weight: %4d", id,
                                                                        arrivalTime, destinationFloor, weight);
    }

    @Override
    public int hashCode() {
        Integer hash0 = super.hashCode();
        Integer hash = hashMap.get(hash0);
        if (hash == null) { hash = count++; hashMap.put(hash0, hash); }
        return hash;
    }

    /**
     * @return the destination floor of the mail item
     */
    public int getDestFloor() {
        return destinationFloor;
    }

    /**
     * @return the ID of the mail item
     */
    public String getId() {
        return id;
    }

    /**
     * @return the arrival time of the mail item
     */
    public int getArrivalTime() {
        return arrivalTime;
    }

    /**
     * @return the weight of the mail item
     */
    public int getWeight() {
        return weight;
    }

    /**
     * @return if the item is delivered
     */
    public boolean isDelivered() {
        return isDelivered;
    }

    /**
     * mark this item as delivered
     */
    public void markAsDelivered() {
        isDelivered = true;
    }
}
