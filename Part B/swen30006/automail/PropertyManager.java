package automail;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * Property Manager, read properties from file,
 * offers values set in the file.
 */
public class PropertyManager {

    // Instance for the singleton
    private static PropertyManager instance = null;

    // Storage of the proterty
    private Properties automailProperties;

    /**
     * @return Obtain the singleton instance of Property Manager
     */
    public static PropertyManager getInstance() {
        if (instance == null) {
            instance = new PropertyManager();
        }
        return instance;
    }

    /**
     * Initialize the property manager
     */
    public PropertyManager() {
        // Create the storage
        automailProperties = new Properties();

        // Set defaults
        automailProperties.setProperty("Number_of_Floors", "14");
        automailProperties.setProperty("Delivery_Penalty", "1.1");
        automailProperties.setProperty("Last_Delivery_Time", "300");
        automailProperties.setProperty("Mail_to_Create", "180");
        automailProperties.setProperty("Robot_Type_1", "weak");
        automailProperties.setProperty("Robot_Type_2", "strong");

        // Load properties from file
        try (FileReader inStream = new FileReader("automail.properties")) {
            automailProperties.load(inStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return true if seed is defined, false otherwise.
     */
    public boolean hasSeed() {
        return automailProperties.getProperty("Seed", null) != null;
    }

    /**
     * Obtain seed configured.
     */
    public int getSeed() {
        return Integer.parseInt(automailProperties.getProperty("Seed"));
    }

    /**
     * Set the seed used
     */
    public void setSeed(String seed) {
        automailProperties.setProperty("Seed", seed);
    }

    /**
     * Get the height of the building
     */
    public int getNumberOfFloors() {
        return Integer.parseInt(automailProperties.getProperty("Number_of_Floors"));
    }

    /**
     * Get the factor of the delivery penalty
     */
    public double getDeliveryPenalty() {
        return Double.parseDouble(automailProperties.getProperty("Delivery_Penalty"));
    }

    /**
     * Get the maximum delivery time among all generated mail items
     */
    public int getLastDeliveryTime() {
        return Integer.parseInt(automailProperties.getProperty("Last_Delivery_Time"));
    }

    /**
     * Get the base amount of mails to be created.
     */
    public int getMailToCreate() {
        return Integer.parseInt(automailProperties.getProperty("Mail_to_Create"));
    }

    /**
     * Get the generated robot type of a specific robot.
     * @param robotID ID of the robot, 1 or 2
     * @return Robot type configured
     */
    public Robot.RobotType getRobotType(int robotID) {
        if (robotID < 1 || robotID > 2)
            throw new IndexOutOfBoundsException();
        String type = automailProperties.getProperty("Robot_Type_" + robotID);
        return Robot.RobotType.valueOf(type.toUpperCase());
    }
}
