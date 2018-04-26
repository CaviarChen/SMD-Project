package strategies;

import automail.*;
import exceptions.TubeFullException;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Queue;

public class StandardMailPool implements IMailPool {
    private static final int MAX_WEIGHT = 2000;
    private LinkedList<MailItem> mailItems; // records all items in the pool
    private int divider;
    private boolean hasWeakRobot = false;

    public StandardMailPool() {
        // Start empty
        mailItems = new LinkedList<>();
        divider = Building.FLOORS / 2;  // divider between lower and upper role
    }

    private int priority(MailItem m) {
        return (m instanceof PriorityMailItem) ? ((PriorityMailItem) m).getPriorityLevel() : 0;
    }

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

    @Override
    public void notifyWeakRobot(boolean hasWeakRobot) {
        this.hasWeakRobot = hasWeakRobot;
    }

    // return true when the current robot should deliver the given mail item
    private boolean checkMailAssignment(MailItem mailItem, Robot.RobotType type, Robot.RobotRole role) {

        // handle overweight items
        if (hasWeakRobot && mailItem.getWeight()>MAX_WEIGHT) {
            return (type != Robot.RobotType.WEAK);
        }

        // divide upper and lower and Priority items go to lower
        if (mailItem instanceof PriorityMailItem || mailItem.getDestFloor() <= divider) {
            return (role == Robot.RobotRole.LOWER);
        } else {
            return (role == Robot.RobotRole.UPPER);
        }

    }



}
