// Group 44
package strategies;

import automail.*;
import exceptions.TubeFullException;

import java.util.LinkedList;
import java.util.ListIterator;

public class StandardMailPool implements IMailPool {

    // records all items in the pool
    private LinkedList<MailItem> mailItems;
    private int divider;

    private boolean hasWeakRobot = false;

    /**
     * Constructor for StandardMailPool
     */
    public StandardMailPool() {
        // Start empty
        mailItems = new LinkedList<>();

        // divider between lower and upper role
        divider = Building.FLOORS / 2;
    }

    /**
     * Adds an item to the mail pool
     * @param mailItem the mail item being added.
     */
    @Override
    public void addToPool(MailItem mailItem) {
        // This doesn't attempt to put the re-add items back in time order but there will be relatively few of them,

        if (mailItem instanceof PriorityMailItem) {
            // PriorityMailItem should be placed in front of all normal MailItem

            ListIterator<MailItem> i = mailItems.listIterator();
            while (i.hasNext()) {
                if (priority(i.next()) < priority(mailItem)) {
                    i.previous();
                    i.add(mailItem);
                    return ;
                }
            }
            mailItems.addLast(mailItem);
        } else {
            // Normal MailItem should be placed at the end
            mailItems.addLast(mailItem);
        }

    }

    /**
     * @param tube   refers to the pack the robot uses to deliver mail.
     * @param type   is the type of the robot.
     * @param role   is the role of the robot.
     */
    @Override
    public void fillStorageTube(StorageTube tube, Robot.RobotType type, Robot.RobotRole role) {
        try {
            ListIterator<MailItem> i = mailItems.listIterator();

            while (!tube.isFull() && i.hasNext()) {
                MailItem mailItem = i.next();

                if(checkMailAssignment(mailItem, type, role)) {
                    tube.addItem(mailItem);
                    i.remove();
                }
            }

        } catch (TubeFullException e) {
            e.printStackTrace();
        }
    }

    /**
     * Noityfy the mailpool that there is a week robot
     * @param hasWeakRobot
     */
    @Override
    public void notifyWeakRobot(boolean hasWeakRobot) {
        this.hasWeakRobot = hasWeakRobot;
    }

    // return true when the current robot should deliver the given mail item
    private boolean checkMailAssignment(MailItem mailItem, Robot.RobotType type, Robot.RobotRole role) {

        // handle overweight items
        if (hasWeakRobot && mailItem.getWeight() > Robot.WEAK_MAX_WEIGHT) {
            return (type != Robot.RobotType.WEAK);
        }

        // divide upper and lower and Priority items go to lower
        if (mailItem instanceof PriorityMailItem || mailItem.getDestFloor() <= divider) {
            return (role == Robot.RobotRole.LOWER);
        } else {
            return (role == Robot.RobotRole.UPPER);
        }

    }

    /**
     * @param m a mail item
     * @return the priority level of a given mail item
     */
    private int priority(MailItem m) {
        return (m instanceof PriorityMailItem) ? ((PriorityMailItem) m).getPriorityLevel() : 0;
    }

}
