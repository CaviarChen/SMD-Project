package strategies;

import automail.MailItem;
import automail.PriorityMailItem;
import automail.Robot;
import automail.StorageTube;

public class StandardRobotBehaviour implements IRobotBehaviour {

    private boolean strong;
    private int newPriority; // Used if we are notified that a priority item has arrived.

    public void startDelivery() {
        newPriority = 0;
    }

    @Override
    public void priorityArrival(int priority, int weight) {
        if (priority > newPriority) {
            newPriority = priority;  // Only the strong robot will deliver priority items so weight of no interest
        }
    }

    @Override
    public void setRobot(Robot robot) {
        this.strong = robot.getType() != Robot.RobotType.WEAK;
    }

    private int tubePriority(StorageTube tube) {  // Assumes at least one item in tube
        MailItem item = tube.peek();
        return (item instanceof PriorityMailItem) ? ((PriorityMailItem) item).getPriorityLevel() : 0;
    }

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

}
