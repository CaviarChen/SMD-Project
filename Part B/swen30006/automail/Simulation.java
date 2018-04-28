package automail;

import exceptions.ExcessiveDeliveryException;
import exceptions.ItemTooHeavyException;
import exceptions.MailAlreadyDeliveredException;
import strategies.Automail;

import java.util.ArrayList;

/**
 * This class simulates the behaviour of AutoMail
 */
public class Simulation {

    /* Constant for the mail generator */
    private static final int MAIL_TO_CREATE = 180;

    // Penalty for longer delivery times
    private static final double TIME_PENALTY = 1.1;


    private static int deliveredCount;
    private static double total_score = 0;

    public static void main(String[] args) {

        deliveredCount = 0;

        // Override seed with command line option
        if (args.length > 1 && args[1].matches("-?\\d+")) {
            PropertyManager.getInstance().setSeed(args[1]);
        }

        Automail automail = new Automail(new ReportDelivery());
        MailGenerator generator = new MailGenerator(MAIL_TO_CREATE);

        while (deliveredCount != generator.mailCount) {

            ArrayList<MailItem> mailItems = generator.getMailsAt(Clock.Time());

            if (mailItems != null) {
                for(MailItem mailItem: mailItems) {
                    if (mailItem instanceof PriorityMailItem) {
                        automail.priorityArrival(((PriorityMailItem)mailItem).getPriorityLevel(), mailItem.getWeight());
                    }

                    System.out.printf("T: %3d > new addToPool [%s]%n", Clock.Time(), mailItem.toString());
                    automail.mailPool.addToPool(mailItem);
                }
            }


            try {
                automail.step();
            } catch (ExcessiveDeliveryException | ItemTooHeavyException e) {
                e.printStackTrace();
                System.out.println("Simulation unable to complete.");
                System.exit(0);
            }
            Clock.Tick();
        }
        printResults();
    }

    private static double calculateDeliveryScore(MailItem deliveryItem) {

        double priority_weight = 0;
        // Take (delivery time - arrivalTime)**penalty * (1+sqrt(priority_weight))
        if (deliveryItem instanceof PriorityMailItem) {
            priority_weight = ((PriorityMailItem) deliveryItem).getPriorityLevel();
        }
        return Math.pow(Clock.Time() - deliveryItem.getArrivalTime(), TIME_PENALTY) * (1 + Math.sqrt(priority_weight));
    }

    public static void printResults() {
        System.out.println("T: " + Clock.Time() + " | Simulation complete!");
        System.out.println("Final Delivery time: " + Clock.Time());
        System.out.printf("Final Score: %.2f%n", total_score);
    }

    static class ReportDelivery implements IMailDelivery {

        /* Confirm the delivery and calculate the total score */
        public void deliver(MailItem deliveryItem) {

            if (!deliveryItem.isDelivered()) {
                System.out.printf("T: %3d > Delivered     [%s]%n", Clock.Time(), deliveryItem.toString());

                deliveryItem.markAsDelivered();
                deliveredCount += 1;
                // Calculate delivery score
                total_score += calculateDeliveryScore(deliveryItem);
            } else {
                try {
                    throw new MailAlreadyDeliveredException();
                } catch (MailAlreadyDeliveredException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
