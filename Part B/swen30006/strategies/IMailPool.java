package strategies;

import automail.MailItem;
import automail.Robot;
import automail.StorageTube;

/**
 * addToPool is called when there are mail items newly arrived at the building to add to the MailPool or
 * if a robot returns with some undelivered items - these are added back to the MailPool.
 * The data structure and algorithms used in the MailPool is your choice.
 */
public interface IMailPool {

    /**
     * Adds an item to the mail pool
     *
     * @param mailItem the mail item being added.
     */
    void addToPool(MailItem mailItem);

    /**
     * @param tube   refers to the pack the robot uses to deliver mail.
     * @param type   is the type of the robot.
     * @param role   is the role of the robot.
     */
    void fillStorageTube(StorageTube tube, Robot.RobotType type, Robot.RobotRole role);

    /**
     * Noityfy the mailpool that there is a week robot
     * @param hasWeakRobot
     */
    void notifyWeakRobot(boolean hasWeakRobot);

}
