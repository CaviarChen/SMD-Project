package strategies;

import automail.IMailDelivery;
import automail.PropertyManager;
import automail.Robot;
import exceptions.ExcessiveDeliveryException;
import exceptions.ItemTooHeavyException;

public class Automail {

    private static final int NUM_OF_ROBOT = 2;


    public Robot[] robots;
    public IMailPool mailPool;

    public Automail(IMailDelivery delivery) {

        robots = new Robot[NUM_OF_ROBOT];


        /* Initialize the MailPool */
        mailPool = new StandardMailPool();

        for(int i=0; i<NUM_OF_ROBOT; i++) {
            robots[i] = new Robot(new StandardRobotBehaviour(), delivery,this,
                                    PropertyManager.getInstance().getRobotType(i+1));

            if (robots[i].getType() == Robot.RobotType.WEAK) mailPool.notifyWeakRobot(true);

        }

        decideRobotRole();

    }

    private void decideRobotRole() {

        int[] roleScore = new int[NUM_OF_ROBOT];

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

    public void step() throws ExcessiveDeliveryException, ItemTooHeavyException {
        for(Robot robot: robots) {
            robot.step();
        }
    }

    public void priorityArrival(int priority, int weight) {
        for(Robot robot: robots) {
            robot.behaviour.priorityArrival(priority, weight);
        }
    }

}
