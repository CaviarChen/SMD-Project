package automail;

import strategies.IMailPool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * This class generates the mail.
 * Constants in this class are based on observations of typical mail arrivals
 */
public class MailGenerator {

    /* The threshold for the latest time for mail to arrive */
    private static final int LAST_DELIVERY_TIME = 300;

    private static final double ARRIVING_MAIL_VARIATION = 0.2;
    private static final double WEIGHT_MEAN = 200.0;
    private static final double WEIGHT_STD = 700.0;

    private static Random random;

    public static HashMap<Integer, ArrayList<MailItem>> generateMails(int mailToCreate) {

        if (PropertyManager.getInstance().hasSeed()) {
            random = new Random((long) PropertyManager.getInstance().getSeed());
        } else {
            random = new Random();
        }

        // Vary arriving mail by +/-20%
        double randomVariation = random.nextDouble() * 2.0 - 1.0;  // Generate a random number in [-1.0, 1.0)
        mailToCreate = mailToCreate + (int) Math.round(mailToCreate * randomVariation * ARRIVING_MAIL_VARIATION);
        HashMap<Integer, ArrayList<MailItem>> allMail = new HashMap<>();
        generateAllMail(allMail, mailToCreate);
        return allMail;
    }

    /**
     * @return a new mail item that needs to be delivered
     */
    private static MailItem generateMail(HashMap<Integer, ArrayList<MailItem>> allMail) {
        int dest_floor = generateDestinationFloor();
        int priority_level = generatePriorityLevel();
        int arrival_time = generateArrivalTime();
        int weight = generateWeight();
        // Check if arrival time has a priority mail
        if ((random.nextInt(6) > 0) ||  // Skew towards non priority mail
                (allMail.containsKey(arrival_time) &&
                        allMail.get(arrival_time).stream().anyMatch(PriorityMailItem.class::isInstance))) {
            return new MailItem(dest_floor, arrival_time, weight);
        } else {
            return new PriorityMailItem(dest_floor, arrival_time, weight, priority_level);
        }
    }

    /**
     * @return a destination floor between the ranges of GROUND_FLOOR to FLOOR
     */
    private static int generateDestinationFloor() {
        return Building.LOWEST_FLOOR + random.nextInt(Building.FLOORS);
    }

    /**
     * @return a random priority level selected from 10 and 100
     */
    private static int generatePriorityLevel() {
        return random.nextInt(4) > 0 ? 10 : 100;
    }

    /**
     * @return a random weight
     */
    private static int generateWeight() {
        double base = random.nextGaussian();
        if (base < 0) base = -base;
        int weight = (int) (WEIGHT_MEAN + base * WEIGHT_STD);
        return weight > 5000 ? 5000 : weight;
    }

    /**
     * @return a random arrival time before the last delivery time
     */
    private static int generateArrivalTime() {
        return 1 + random.nextInt(LAST_DELIVERY_TIME);
    }

    /**
     * Returns a random element from an array
     *
     * @param array of objects
     */
    private static Object getRandom(Object[] array) {
        return array[random.nextInt(array.length)];
    }

    /**
     * This class initializes all mail and sets their corresponding values,
     */
    private static void generateAllMail(HashMap<Integer, ArrayList<MailItem>> allMail, int mailToCreate) {
        boolean complete = false;
        int mailCreated = 0;
        while (!complete) {
            MailItem newMail = generateMail(allMail);
            int timeToDeliver = newMail.getArrivalTime();
            /* Check if key exists for this time */
            if (allMail.containsKey(timeToDeliver)) {
                /* Add to existing array */
                allMail.get(timeToDeliver).add(newMail);
            } else {
                /* If the key doesn't exist then set a new key along with the array of MailItems to add during
                 * that time step.
                 */
                ArrayList<MailItem> newMailList = new ArrayList<>();
                newMailList.add(newMail);
                allMail.put(timeToDeliver, newMailList);
            }
            /* Mark the mail as created */
            mailCreated++;

            /* Once we have satisfied the amount of mail to create, we're done!*/
            if (mailCreated == mailToCreate) {
                complete = true;
            }
        }

    }


}
