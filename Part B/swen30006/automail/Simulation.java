package automail;

import exceptions.ExcessiveDeliveryException;
import exceptions.ItemTooHeavyException;
import exceptions.MailAlreadyDeliveredException;
import strategies.Automail;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class simulates the behaviour of AutoMail
 */
public class Simulation {

    /* Constant for the mail generator */
    private static final int MAIL_TO_CREATE = 180;


    private static ArrayList<MailItem> MAIL_DELIVERED;
    private static double total_score = 0;

    public static void main(String[] args) {

        MAIL_DELIVERED = new ArrayList<>();

        /* Used to see whether a seed is initialized or not */
        HashMap<Boolean, Integer> seedMap = new HashMap<>();

        /* Read the first argument and save it as a seed if it exists */
        if (args.length != 0) {
            int seed = Integer.parseInt(args[0]);
            seedMap.put(true, seed);
        } else {
            seedMap.put(false, 0);
        }
        Automail automail = new Automail(new ReportDelivery());
        MailGenerator generator = new MailGenerator(MAIL_TO_CREATE, seedMap);

        /* Initiate all the mail */
        generator.generateAllMail();
        PriorityMailItem priority;
        while (MAIL_DELIVERED.size() != generator.MAIL_TO_CREATE) {
            // System.out.println("-- Step: "+Clock.Time());
            priority = generator.step(automail.mailPool);
            if (priority != null) {
                automail.priorityArrival(priority.getPriorityLevel(), priority.getWeight());
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
        // Penalty for longer delivery times
        final double penalty = 1.1;
        double priority_weight = 0;
        // Take (delivery time - arrivalTime)**penalty * (1+sqrt(priority_weight))
        if (deliveryItem instanceof PriorityMailItem) {
            priority_weight = ((PriorityMailItem) deliveryItem).getPriorityLevel();
        }
        return Math.pow(Clock.Time() - deliveryItem.getArrivalTime(), penalty) * (1 + Math.sqrt(priority_weight));
    }

    public static void printResults() {
        System.out.println("T: " + Clock.Time() + " | Simulation complete!");
        System.out.println("Final Delivery time: " + Clock.Time());
        System.out.printf("Final Score: %.2f%n", total_score);
    }

    static class ReportDelivery implements IMailDelivery {

        /* Confirm the delivery and calculate the total score */
        public void deliver(MailItem deliveryItem) {
            if (!MAIL_DELIVERED.contains(deliveryItem)) {
                System.out.printf("T: %3d > Delivered     [%s]%n", Clock.Time(), deliveryItem.toString());
                MAIL_DELIVERED.add(deliveryItem);
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
