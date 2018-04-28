package strategies;

import automail.MailItem;
import automail.PriorityMailItem;
import automail.Robot;
import automail.StorageTube;

public class StandardRobotBehaviour implements IRobotBehaviour {

    private boolean strong;
    private int newPriority; // Used if we are notified that a priority item has arrived.

    /**
     * startDelivery() provides the robot the opportunity to initialise state
     * in support of the other methods below.
     */
    public void startDelivery() {
        newPriority = 0;
    }

    /**
     * The automail system broadcasts this information to all robots
     * when a new priority mail items arrives at the building.
     * @param priority is that of the priority mail item which just arrived.
     * @param weight   is that of the same item.
     */
    @Override
    public void priorityArrival(int priority, int weight) {
        if (priority > newPriority) {
            newPriority = priority;  // Only the strong robot will deliver priority items so weight of no interest
        }
    }

    /**
     * @param robot Update the current robot that is using this behaviour object
     */
    @Override
    public void setRobot(Robot robot) {
        this.strong = robot.getType() != Robot.RobotType.WEAK;
    }

    /**
     * The robot will always return to the mail room when the tube is empty.
     * This method allows the robot to return with items still in the tube,
     * if circumstances make this desirable.
     * @param tube refers to the pack the robot uses to deliver mail.
     * @return When this is true, the robot is returned to the mail room.
     */
    @Override
    public boolean returnToMailRoom(StorageTube tube) {
        if (tube.isEmpty()) {
            return false; // Empty tube means we are returning anyway
        } else {
            // Return true for the strong robot if the one waiting is higher priority than the one we have
            // Assumes that the one at the top of the tube has the highest priority
            return strong && newPriority > tubePriority(tube);
        }
    }

    /**
     * @param tube a given tube
     * @return return the priority level of the first item in a given tube
     */
    private int tubePriority(StorageTube tube) {  // Assumes at least one item in tube
        MailItem item = tube.peek();
        return (item instanceof PriorityMailItem) ? ((PriorityMailItem) item).getPriorityLevel() : 0;
    }

}
