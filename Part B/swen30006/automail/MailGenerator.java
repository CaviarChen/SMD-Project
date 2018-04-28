package automail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * This class generates the mail.
 * Constants in this class are based on observations of typical mail arrivals
 */
public class MailGenerator {

    // total number of mails
    public final int mailCount;


    // constants for random mail generation
    private static final double MAIL_COUNT_VARIATION = 0.2;
    private static final double WEIGHT_MEAN = 200.0;
    private static final double WEIGHT_STD = 700.0;

    private static final int WEIGHT_PRIORITY_ITEM = 6;
    private static final int WEIGHT_PRIORITY_LEVEL = 4;

    private static final int PRIORITY_LEVEL_LOW = 10;
    private static final int PRIORITY_LEVEL_HIGH = 100;

    private static final int MAX_ITEM_WEIGHT = 5000;

    private final Random random;

    // store all MailItems based on time
    private HashMap<Integer, ArrayList<MailItem>> allMail;


    /**
     * Constructor for mail generation
     *
     * @param mailToCreate roughly how many mail items to create
     */
    public MailGenerator(int mailToCreate) {


        if (PropertyManager.getInstance().hasSeed()) {
            this.random = new Random((long) PropertyManager.getInstance().getSeed());
        } else {
            this.random = new Random();
        }

        // calculate the actual number of mail to create
        // Vary arriving mail by +/-20% (ARRIVING_MAIL_VARIATION = 0.2)
        this.mailCount = (int) (mailToCreate * (1 - MAIL_COUNT_VARIATION)) +
                         random.nextInt((int)(mailToCreate * MAIL_COUNT_VARIATION * 2));


        allMail = new HashMap<>();

        this.generateAllMail();
    }

    /**
     * @param time a given time
     * @return mails that should be add to the mailpool at a given time
     */
    public ArrayList<MailItem> getMailsAt(int time) {
        if (this.allMail.containsKey(time)) {
            return allMail.get(time);
        }
        return null;
    }


    /**
     * @return  a new mail item that needs to be delivered
     */
    private MailItem generateMail() {
        int dest_floor = generateDestinationFloor();
        int priority_level = generatePriorityLevel();
        int arrival_time = generateArrivalTime();
        int weight = generateWeight();
        // Check if arrival time has a priority mail
        if ((random.nextInt(WEIGHT_PRIORITY_ITEM) > 0) ||  // Skew towards non priority mail
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
    private int generateDestinationFloor() {
        return Building.LOWEST_FLOOR + random.nextInt(Building.FLOORS);
    }

    /**
     * @return a random priority level selected from 10 and 100
     */
    private int generatePriorityLevel() {
        return random.nextInt(WEIGHT_PRIORITY_LEVEL) > 0 ? PRIORITY_LEVEL_LOW : PRIORITY_LEVEL_HIGH;
    }

    /**
     * @return a random weight
     */
    private int generateWeight() {
        double base = random.nextGaussian();
        if (base < 0) base = -base;
        int weight = (int) (WEIGHT_MEAN + base * WEIGHT_STD);
        return Math.min(weight, MAX_ITEM_WEIGHT);
    }

    /**
     * @return a random arrival time before the last delivery time
     */
    private int generateArrivalTime() {
        return 1 + random.nextInt(PropertyManager.getInstance().getLastDeliveryTime());
    }

    /**
     * This class initializes all mail and sets their corresponding values,
     */
    private void generateAllMail() {
        for (int i=0; i<this.mailCount; i++) {

            MailItem newMail = generateMail();

            int timeToDeliver = newMail.getArrivalTime();
            /* Check if key exists for this time */


            if (!allMail.containsKey(timeToDeliver)) {
                /* If the key doesn't exist then set a new key along with the array of MailItems to add during
                 * that time step.
                 */
                allMail.put(timeToDeliver, new ArrayList<>());
            }

            /* Add to existing array */
            allMail.get(timeToDeliver).add(newMail);
        }

    }

}
