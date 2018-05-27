package mycontroller.strategies;

import mycontroller.MyAIController;
import mycontroller.RoutingData;
import mycontroller.pipeline.AStar;
import tiles.HealthTrap;
import utilities.Coordinate;

import java.util.HashSet;

/**
 * Repair strategy navigate the car back the nearest
 * known health trap and stay there if it thinks there
 * is a need to do so.
 */
public class RepairStrategy implements Strategy {

    /**
     * When the car's health reach this value, it leaves the health trap.
     */
    private static final int FINISH_THRESHOLD = 96;

    /**
     * Prevent the car go back to a health trap when it just left from it
     * for this number of cells.
     */
    private static final int LEAVE_HEALTH_TRAP = 12;

    /**
     * If the car has moved a cell since last calculation.
     */
    private boolean carMoved = false;

    /**
     * Number of steps the car has to travel that prevents it from
     * coming back to a health trap.
     */
    private int resetCount = 0;

    /**
     * Stop the car if it is on a health trap and its health is
     * below this value.
     */
    private static final int PASS_BY_RECOVER_THRESHOLD = 90;

    /**
     * Never navigate the car to a health trap if its health level
     * is above this value.
     */
    private static final int TRAVEL_TO_RECOVER_THRESHOLD = 80;

    /**
     * get the targets decided by the strategy
     *
     * @param myAIController the main controller
     * @return relative routing data
     */
    @Override
    public RoutingData getTargets(MyAIController myAIController) {

        // Current coordinate of the car
        int currentX = Math.round(myAIController.getX());
        int currentY = Math.round(myAIController.getY());

        RoutingData routingData = new RoutingData();

        // Don't move the car if it is already on a health trap
        // until the strategy is finished
        if (myAIController.mapRecorder.mapTiles[currentX][currentY]
                instanceof HealthTrap) {
            routingData.targets = null;
            return routingData;
        }

        // Navigate the car to a health trap.
        routingData.targets =
                new HashSet<>(myAIController.mapRecorder.healthCoords);

        return routingData;
    }

    /**
     * check if the current strategy is finished (and will be pop out
     * from the strategy stack)
     *
     * @param myAIController the main controller
     * @return true if the strategy has finished its task
     */
    @Override
    public boolean isFinished(MyAIController myAIController) {
        // This strategy is only finished when the car
        // has reached a certain health level
        if (myAIController.getHealth() >= FINISH_THRESHOLD) {
            // Set the "resetCount" to prevent the car from
            // returning to the health trap immediately.
            resetCount = LEAVE_HEALTH_TRAP;
            return true;
        }
        return false;

    }

    /**
     * Decide if the car need to visit a health trap immediately.
     * This allows the Repair Strategy to activate and become the
     * current activate strategy.
     *
     * @param myAIController the current "My AI Controller"
     * @return true if the car need to visit a health trap.
     */
    public boolean needTakeOver(MyAIController myAIController) {

        // Coordinate of the car
        int currentX = Math.round(myAIController.getX()),
                currentY = Math.round(myAIController.getY());

        // Stay on a health trap if the car is on one and it needs to recover
        if (myAIController.getHealth() < PASS_BY_RECOVER_THRESHOLD &&
                myAIController.mapRecorder.mapTiles[currentX][currentY]
                        instanceof HealthTrap) {
            return true;
        }

        // Do calculations only when the car has moved a cell
        // since last calculation
        if (!carMoved) return false;
        carMoved = false;

        // Never take over if no health trap is found
        if (myAIController.mapRecorder.healthCoords.isEmpty())
            return false;

        // Never take over if the car just left a health trap
        if (resetCount > 0) {
            return false;
        }

        // Do not go to a health trap if it has high health level
        if (myAIController.getHealth() > TRAVEL_TO_RECOVER_THRESHOLD)
            return false;


        // Inspect if the car now is travelling to somewhere
        int pathSize = myAIController.getRoutingData().path.size();

        if (pathSize > 0) {
            // Calculate the estimated cost to the nearest health trap
            AStar.Node node = new AStar().start(myAIController.mapRecorder,
                    new Coordinate(currentX, currentY),
                    myAIController.mapRecorder.healthCoords
            );

            // Calculate the estimated cost to the current intended destination

            HashSet<Coordinate> carDestination = new HashSet<>();
            carDestination.add(myAIController.getRoutingData().path
                    .get(pathSize - 1).toCoordinate());

            AStar.Node nodeDest =
                    new AStar().start(myAIController.mapRecorder,
                            new Coordinate(currentX, currentY),
                            carDestination
                    );

            // Get the car to a health trap if it costs less to a health trap.
            return node.G < nodeDest.G;
        }

        // Don't get the car to a health trap otherwise.
        return false;
    }

    /**
     * Inform the repair strategy if the car has travelled a cell
     */
    public void carMoved() {
        // Decrease the step count by 1 if the car is in
        // the prevent-back-to-health-trap state.
        if (resetCount > 0) {
            resetCount--;
        }

        // Record the moving notification
        carMoved = true;
    }

}
