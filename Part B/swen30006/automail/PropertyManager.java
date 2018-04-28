package automail;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class PropertyManager {

    private static PropertyManager instance = null;

    private Properties automailProperties;

    public static PropertyManager getInstance() {
        if (instance == null) {
            instance = new PropertyManager();
        }
        return instance;
    }

    public PropertyManager() {
        // Should probably be using properties here
        automailProperties = new Properties();

        // Defaults
        automailProperties.setProperty("Number_of_Floors", "14");
        automailProperties.setProperty("Delivery_Penalty", "1.1");
        automailProperties.setProperty("Last_Delivery_Time", "300");
        automailProperties.setProperty("Mail_to_Create", "180");
        automailProperties.setProperty("Robot_Type_1", "weak");
        automailProperties.setProperty("Robot_Type_2", "strong");

        try (FileReader inStream = new FileReader("automail.properties")) {
            automailProperties.load(inStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean hasSeed() {
        return automailProperties.getProperty("Seed", null) != null;
    }

    public int getSeed() {
        return Integer.parseInt(automailProperties.getProperty("Seed"));
    }

    public void setSeed(String seed) { automailProperties.setProperty("Seed", seed); }

    public int getNumberOfFloors() {
        return Integer.parseInt(automailProperties.getProperty("Number_of_Floors"));
    }

    public float getDeliveryPenalty() {
        return Float.parseFloat(automailProperties.getProperty("Delivery_Penalty"));
    }

    public int getLastDeliveryTime() {
        return Integer.parseInt(automailProperties.getProperty("Last_Delivery_Time"));
    }

    public int getMailToCreate() {
        return Integer.parseInt(automailProperties.getProperty("Mail_to_Create"));
    }

    public Robot.RobotType getRobotType(int robotID) {
        if (robotID < 1 || robotID > 2)
            throw new IndexOutOfBoundsException();
        String type = automailProperties.getProperty("Robot_Type_" + robotID);
        return Robot.RobotType.valueOf(type.toUpperCase());
    }
}
