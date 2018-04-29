// Group 44
package automail;

public class PriorityMailItem extends MailItem {

    /* The priority of the mail item from 1 low to 100 high */
    private final int priorityLevel;

    /**
     * Constructor for a PriorityMailItem
     *
     * @param destFloor   the destination floor intended for this mail item
     * @param arrivalTime the time that the mail arrived
     * @param weight       the weight of this mail item
     * @param priorityLevel the priority_level of this mail item
     */
    public PriorityMailItem(int destFloor, int arrivalTime, int weight, int priorityLevel) {
        super(destFloor, arrivalTime, weight);
        this.priorityLevel = priorityLevel;
    }

     /**
     * @return the priority level of a mail item
     */
    public int getPriorityLevel() {
        return priorityLevel;
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" | Priority: %3d", priorityLevel);
    }

}
