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
    public static final int LAST_DELIVERY_TIME = 300;

    public final int MAIL_TO_CREATE;
    private final Random random;
    private int mailCreated;
    /* This seed is used to make the behaviour deterministic */
    private boolean complete;

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
        // Vary arriving mail by +/-20%
        MAIL_TO_CREATE = mailToCreate * 4 / 5 + random.nextInt(mailToCreate * 2 / 5);
        // System.out.println("Num Mail Items: "+MAIL_TO_CREATE);
        mailCreated = 0;
        complete = false;
        allMail = new HashMap<>();
    }

    /**
     * @return a new mail item that needs to be delivered
     */
    private MailItem generateMail() {
        int dest_floor = generateDestinationFloor();
        int priority_level = generatePriorityLevel();
        int arrival_time = generateArrivalTime();
        int weight = generateWeight();
        // Check if arrival time has a priority mail
        if ((random.nextInt(6) > 0) ||  // Skew towards non priority mail
                (allMail.containsKey(arrival_time) &&
                        allMail.get(arrival_time).stream().anyMatch(e -> PriorityMailItem.class.isInstance(e)))) {
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
        return random.nextInt(4) > 0 ? 10 : 100;
    }

    /**
     * @return a random weight
     */
    private int generateWeight() {
        final double mean = 200.0; // grams for normal item
        final double stddev = 700.0; // grams
        double base = random.nextGaussian();
        if (base < 0) base = -base;
        int weight = (int) (mean + base * stddev);
        return weight > 5000 ? 5000 : weight;
    }

    /**
     * @return a random arrival time before the last delivery time
     */
    private int generateArrivalTime() {
        return 1 + random.nextInt(LAST_DELIVERY_TIME);
    }

    /**
     * Returns a random element from an array
     *
     * @param array of objects
     */
    private Object getRandom(Object[] array) {
        return array[random.nextInt(array.length)];
    }

    /**
     * This class initializes all mail and sets their corresponding values,
     */
    public void generateAllMail() {
        while (!complete) {
            MailItem newMail = generateMail();
            int timeToDeliver = newMail.getArrivalTime();
            /* Check if key exists for this time */
            if (allMail.containsKey(timeToDeliver)) {
                /* Add to existing array */
                allMail.get(timeToDeliver).add(newMail);
            } else {
                /** If the key doesn't exist then set a new key along with the array of MailItems to add during
                 * that time step.
                 */
                ArrayList<MailItem> newMailList = new ArrayList<MailItem>();
                newMailList.add(newMail);
                allMail.put(timeToDeliver, newMailList);
            }
            /* Mark the mail as created */
            mailCreated++;

            /* Once we have satisfied the amount of mail to create, we're done!*/
            if (mailCreated == MAIL_TO_CREATE) {
                complete = true;
            }
        }

    }

    /**
     * While there are steps left, create a new mail item to deliver
     *
     * @return Priority
     */
    public PriorityMailItem step(IMailPool mailPool) {
        PriorityMailItem priority = null;
        // Check if there are any mail to create
        if (this.allMail.containsKey(Clock.Time())) {
            for (MailItem mailItem : allMail.get(Clock.Time())) {
                if (mailItem instanceof PriorityMailItem) priority = ((PriorityMailItem) mailItem);
                System.out.printf("T: %3d > new addToPool [%s]%n", Clock.Time(), mailItem.toString());
                mailPool.addToPool(mailItem);
            }
        }
        return priority;
    }

}
