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

    public final int mailCount;
    private final Random random;
    /* This seed is used to make the behaviour deterministic */

    private HashMap<Integer, ArrayList<MailItem>> allMail;

    /**
     * Constructor for mail generation
     *
     * @param mailToCreate roughly how many mail items to create
     */
    public MailGenerator(int mailToCreate) {

        // handle in PropertyManager? is default random?
        if (PropertyManager.getInstance().hasSeed()) {
            this.random = new Random((long) PropertyManager.getInstance().getSeed());
        } else {
            this.random = new Random();
        }

        // Vary arriving mail by +/-20%

        this.mailCount = mailToCreate * 4 / 5 + random.nextInt(mailToCreate * 2 / 5);


        allMail = new HashMap<>();

        this.generateAllMail();
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
        return random.nextInt(4) > 0 ? 10 : 100;
    }

    /**
     * @return a random weight
     */
    private int generateWeight() {
        double base = random.nextGaussian();
        if (base < 0) base = -base;
        int weight = (int) (WEIGHT_MEAN + base * WEIGHT_STD);
        return weight > 5000 ? 5000 : weight;
    }

    /**
     * @return a random arrival time before the last delivery time
     */
    private int generateArrivalTime() {
        return 1 + random.nextInt(LAST_DELIVERY_TIME);
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

    public ArrayList<MailItem> getMailsAt(int time) {
        if (this.allMail.containsKey(time)) {
            return allMail.get(time);
        }

        return null;
    }


//    /**
//     * While there are steps left, create a new mail item to deliver
//     *
//     * @return Priority
//     */
//    public PriorityMailItem step(IMailPool mailPool) {
//        PriorityMailItem priority = null;
//        // Check if there are any mail to create
//        if (this.allMail.containsKey(Clock.Time())) {
//            for (MailItem mailItem : allMail.get(Clock.Time())) {
//                if (mailItem instanceof PriorityMailItem) priority = ((PriorityMailItem) mailItem);
//                System.out.printf("T: %3d > new addToPool [%s]%n", Clock.Time(), mailItem.toString());
//                mailPool.addToPool(mailItem);
//            }
//        }
//        return priority;
//    }

}
