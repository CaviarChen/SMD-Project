package automail;

// import exceptions.RobotNotInMailRoomException;

import exceptions.TubeFullException;

import java.util.EmptyStackException;
import java.util.Stack;

/**
 * The storage tube carried by the robot.
 */
public class StorageTube {

    private static final int NORMAL_CAPACITY = 4;
    private static final int BIG_CAPACITY = 6;

    private int maximumCapacity;
    public Stack<MailItem> tube;

    /**
     * Constructor for the storage tube
     */
    public StorageTube(Robot.RobotType type) {
        this.tube = new Stack<>();
        if (type == Robot.RobotType.BIG) maximumCapacity = BIG_CAPACITY;
        else maximumCapacity = NORMAL_CAPACITY;
    }

    /**
     * @return if the storage tube is full
     */
    public boolean isFull() {
        return tube.size() == maximumCapacity;
    }

    /**
     * @return if the storage tube is empty
     */
    public boolean isEmpty() {
        return tube.isEmpty();
    }

    /**
     * @return the first item in the storage tube (without removing it)
     */
    public MailItem peek() {
        return tube.peek();
    }

    /**
     * Add an item to the tube
     *
     * @param item The item being added
     * @throws TubeFullException thrown if an item is added which exceeds the capacity
     */
    public void addItem(MailItem item) throws TubeFullException {
        if (tube.size() < maximumCapacity) {
            tube.add(item);
        } else {
            throw new TubeFullException();
        }
    }

    /* @return the size of the tube */
    public int getSize() {
        return tube.size();
    }

    /**
     * @return the first item in the storage tube (after removing it)
     */
    public MailItem pop() {
        return tube.pop();
    }

    /**
     * @return destination floor of the next mail item in the tube
     * @throws EmptyStackException if the tube is empty.
     */
    public int getNextDestFloor() {
        return tube.peek().getDestFloor();
    }

    /**
     * @return Maximum capacity of the tube
     */
    public int getMaximumCapacity() { return maximumCapacity; }
}
