package strategies;

import automail.IMailDelivery;
import automail.Robot;
import exceptions.ExcessiveDeliveryException;
import exceptions.ItemTooHeavyException;

public class Automail {

    public Robot robot1, robot2;
    public IMailPool mailPool;

    public Automail(IMailDelivery delivery) {
        // Swap between simple provided strategies and your strategies here

        /* Initialize the MailPool */

        //// Swap the next line for the one below
        mailPool = new WeakStrongMailPool();

        /* Initialize the RobotAction */
        //// Swap the next two lines for the two below those
        IRobotBehaviour robotBehaviourW = new MyRobotBehaviour();
        IRobotBehaviour robotBehaviourS = new MyRobotBehaviour();

        /* Initialize robot */
        robot1 = new Robot(robotBehaviourW, delivery, this, Robot.RobotType.WEAK); /* shared behaviour because identical and stateless */
        robot2 = new Robot(robotBehaviourS, delivery, this, Robot.RobotType.STRONG);
    }

    public void step() throws ExcessiveDeliveryException, ItemTooHeavyException {
        robot1.step();
        robot2.step();
    }

    public void priorityArrival(int priority, int weight) {
        robot1.behaviour.priorityArrival(priority, weight);
        robot2.behaviour.priorityArrival(priority, weight);
    }

}
