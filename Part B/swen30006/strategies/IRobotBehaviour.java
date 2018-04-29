// Group 44
package strategies;

import automail.Robot;
import automail.StorageTube;

public interface IRobotBehaviour {

    /**
     * startDelivery() provides the robot the opportunity to initialise state
     * in support of the other methods below.
     */
    void startDelivery();

    /**
     * The robot will always return to the mail room when the tube is empty.
     * This method allows the robot to return with items still in the tube,
     * if circumstances make this desirable.
     * @param tube refers to the pack the robot uses to deliver mail.
     * @return When this is true, the robot is returned to the mail room.
     */
    boolean returnToMailRoom(StorageTube tube);

    /**
     * The automail system broadcasts this information to all robots
     * when a new priority mail items arrives at the building.
     * @param priority is that of the priority mail item which just arrived.
     * @param weight   is that of the same item.
     */
    void priorityArrival(int priority, int weight);

    /**
     * @param robot Update the current robot that is using this behaviour object
     */
    void setRobot(Robot robot);
}
