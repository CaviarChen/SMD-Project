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

    private static int deliveredCount;
    private static double totalScore = 0;


    public static void main(String[] args) {

        deliveredCount = 0;

        // Override seed with command line option
        if (args.length > 1 && args[1].matches("-?\\d+")) {
            PropertyManager.getInstance().setSeed(args[1]);
        }

        // create automail and mail generator
        Automail automail = new Automail(new ReportDelivery());
        MailGenerator generator = new MailGenerator(PropertyManager.getInstance().getMailToCreate());

        // start simulation
        while (deliveredCount != generator.mailCount) {

            // get mails that should be add to mailpool
            ArrayList<MailItem> mailItems = generator.getMailsAt(Clock.Time());

            if (mailItems != null) {
                // add mails to mailpool
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

    /**
     * calculate socore for a mail item
     * @param deliveryItem mail item
     * @return score
     */
    private static double calculateDeliveryScore(MailItem deliveryItem) {

        double priority_weight = 0;
        // Take (delivery time - arrivalTime)**penalty * (1+sqrt(priority_weight))
        if (deliveryItem instanceof PriorityMailItem) {
            priority_weight = ((PriorityMailItem) deliveryItem).getPriorityLevel();
        }
        return Math.pow(Clock.Time() - deliveryItem.getArrivalTime(),
                    PropertyManager.getInstance().getDeliveryPenalty()) * (1 + Math.sqrt(priority_weight));
    }

    /**
     * print final results
     */
    public static void printResults() {
        System.out.println("T: " + Clock.Time() + " | Simulation complete!");
        System.out.println("Final Delivery time: " + Clock.Time());
        System.out.printf("Final Score: %.2f%n", totalScore);
    }


    static class ReportDelivery implements IMailDelivery {

        /**
         * Confirm the delivery and calculate the total score
         * @param deliveryItem mail item
         */
        public void deliver(MailItem deliveryItem) {

            if (!deliveryItem.isDelivered()) {
                // item is delivered successfully
                System.out.printf("T: %3d > Delivered     [%s]%n", Clock.Time(), deliveryItem.toString());

                deliveryItem.markAsDelivered();
                deliveredCount += 1;
                // Calculate delivery score
                totalScore += calculateDeliveryScore(deliveryItem);
            } else {
                // item already be delivered
                try {
                    throw new MailAlreadyDeliveredException();
                } catch (MailAlreadyDeliveredException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
