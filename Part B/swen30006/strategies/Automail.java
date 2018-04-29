// Group 44
package strategies;

import automail.*;
import exceptions.ExcessiveDeliveryException;
import exceptions.ItemTooHeavyException;

public class Automail {

    // number of robots
    private static final int NUM_OF_ROBOT = 2;


    public Robot[] robots;
    public IMailPool mailPool;

    /**
     * Constructor for automail
     * @param delivery IMailDelivery Class
     */
    public Automail(IMailDelivery delivery) {

        robots = new Robot[NUM_OF_ROBOT];

        // Initialize the MailPool
        mailPool = new StandardMailPool();

        // Initialize robots
        for(int i=0; i<NUM_OF_ROBOT; i++) {
            robots[i] = new Robot(new StandardRobotBehaviour(), delivery,
                    PropertyManager.getInstance().getRobotType(i+1));

            // notify the pool if there is a weak robot
            if (robots[i].getType() == Robot.RobotType.WEAK) mailPool.notifyWeakRobot(true);

        }

        decideRobotRole();

    }

    /**
     * decide which robot is upper and which is lower
     */
    private void decideRobotRole() {

        int[] roleScore = new int[NUM_OF_ROBOT];

        // each type have different score
        for(int i=0; i<NUM_OF_ROBOT; i++) {
            switch (robots[i].getType()) {
                case WEAK:
                    roleScore[i] = 3;
                    break;
                case BIG:
                    roleScore[i] = 2;
                    break;
                case STRONG:
                    roleScore[i] = 1;
                    break;
            }
        }

        // robot with higher role score will be the Upper one
        if (roleScore[0]>=roleScore[1]) {
            robots[0].setRole(Robot.RobotRole.UPPER);
            robots[1].setRole(Robot.RobotRole.LOWER);
        } else {
            robots[0].setRole(Robot.RobotRole.LOWER);
            robots[1].setRole(Robot.RobotRole.UPPER);
        }

    }

    /**
     * This is called on every time step
     *
     * @throws ExcessiveDeliveryException if robot delivers more than the capacity of the tube without refilling
     * @throws ItemTooHeavyException if a robot takes a MailItem from its StorageTube which is too heavy for that robot
     */
    public void step() throws ExcessiveDeliveryException, ItemTooHeavyException {

        for(Robot robot: robots) {

            // if robot is at mail room, then refill it
            if(robot.atMailRoom()) {
                refillRobot(robot);
            }

            robot.step();
        }
    }

    /**
     * notify robots when a priority mail item arrived
     * @param priority is that of the priority mail item which just arrived.
     * @param weight   is that of the same item.
     */
    public void priorityArrival(int priority, int weight) {
        for(Robot robot: robots) {
            robot.behaviour.priorityArrival(priority, weight);
        }
    }

    /**
     * refill a robot that is at mail room
     * @param robot a robot that need to be refilled
     */
    private void refillRobot(Robot robot) {
        // do nothing if robot is about to leave
        if (robot.getState() == Robot.RobotState.DELIVERING) {
            return;
        }
        // robot just arrived or waiting

        // clean current tube
        while (!robot.tube.isEmpty()) {
            MailItem mailItem = robot.tube.pop();
            mailPool.addToPool(mailItem);
            System.out.printf("T: %3d > old addToPool [%s]%n", Clock.Time(), mailItem.toString());
        }

        // fill tube
        mailPool.fillStorageTube(robot.tube, robot.getType(), robot.getRole());

    }

}
