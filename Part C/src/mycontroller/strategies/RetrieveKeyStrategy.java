package mycontroller.strategies;

import mycontroller.MapRecorder;
import mycontroller.MyAIController;
import mycontroller.RoutingData;
import tiles.LavaTrap;
import utilities.Coordinate;

/**
 * Strategy to navigate the user to explore the entire map
 * and collect keys in order.
 */
public class RetrieveKeyStrategy implements Strategy {

    /** Directions that the car may rush across a key. */
    private static final int[][] DIRECTIONS =
            new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    /**
     * get the targets decided by the strategy
     * @param myAIController the main controller
     * @return relative routing data
     */
    @Override
    public RoutingData getTargets(MyAIController myAIController) {

        RoutingData routingData = new RoutingData();

        // Number of the key the car has now
        int keyCount = myAIController.getKey();

        // Check if coordinate of all keys are known
        boolean allKeyFound = true;
        for (int i = keyCount - 2; i >= 0; i--)
            allKeyFound &= myAIController.mapRecorder.keysCoord[i] != null;

        // Continue to explore the map if a key is not discovered yet.
        if (!allKeyFound) {
            routingData.targets.addAll(
                    myAIController.mapRecorder.coordinatesToExplore());
        }

        // Try to collect the next key if its coordinate is known.
        if (keyCount > 1 &&
                myAIController.mapRecorder.keysCoord[keyCount - 2] != null) {
            getKeyRouting(routingData,
                    myAIController.mapRecorder,
                    keyCount - 2);
        }

        return routingData;
    }

    /**
     * Add the coordinate of the key to the routing data, and
     * attempt to let the car rush across it if possible by
     * recording the pair of coordinates on both side of the key
     * horizontally and vertically if applicable.
     *
     * @param routingData routing data to put data in
     * @param mapRecorder current map recorder
     * @param keyID array index of the key to collect.
     */
    private void getKeyRouting(RoutingData routingData,
                               MapRecorder mapRecorder,
                               int keyID) {

        // Coordinate of the key
        Coordinate keyCoord = mapRecorder.keysCoord[keyID];

        // Record the coordinates on 4 sides of the key where the car
        // could rush along.
        Coordinate[] rushCoord = new Coordinate[DIRECTIONS.length];

        // Calculate valid coordinates on each direction
        for (int i = 0; i < DIRECTIONS.length; i++) {
            int x = keyCoord.x;
            int y = keyCoord.y;
            int selectedX = -1;
            int selectedY = -1;
            // Number of tiles the pointed tile is away from lava trap
            int awayFromLava = 0;
            // make sure it is in the map
            while (mapRecorder.inRange(x, y)) {
                // Traverse the map along the direction
                x += DIRECTIONS[i][0];
                y += DIRECTIONS[i][1];

                // Stop if the coordinate is not reachable
                // (e.g. wall, enclosed space, etc.)
                if (mapRecorder.mapStatus[x][y] ==
                        MapRecorder.TileStatus.UNREACHABLE)
                    break;

                // Put the coordinate as an candidate if it is not a lava trap
                if (!(mapRecorder.mapTiles[x][y] instanceof LavaTrap)) {
                    // find a road
                    selectedX = x;
                    selectedY = y;
                    awayFromLava += 1;
                    // Stop if the tile is 2 tiles away from lava
                    if (awayFromLava == 2) break;
                } else {
                    // Stop if it encounters lava again
                    if (awayFromLava > 0) break;
                }
            }

            // Record the candidate coordinate if available
            if (selectedX != -1) {
                rushCoord[i] = new Coordinate(selectedX, selectedY);
            }
        }

        boolean hasRushPoint = false;

        // Record the pair of coordinates on each orientation if both
        // candidates in the direction is available.
        for (int i = 0; i + 1 < DIRECTIONS.length; i += 2) {
            if (rushCoord[i] != null && rushCoord[i + 1] != null) {
                hasRushPoint = true;
                Coordinate first = rushCoord[i], second = rushCoord[i + 1];
                routingData.targetPairs.put(first, second);
                routingData.targetPairs.put(second, first);
                routingData.targets.add(first);
                routingData.targets.add(second);

            }
        }

        // Add the key it self if it is not possible to rush across it
        if (!hasRushPoint) {
            routingData.targets.add(keyCoord);
        }

    }

    /**
     * check if the current strategy is finished (and will be pop out
     * from the strategy stack)
     * @param myAIController the main controller
     * @return true if the strategy has finished its task
     */
    @Override
    public boolean isFinished(MyAIController myAIController) {
        return (myAIController.getKey() == 1 &&
                myAIController.getRoutingData().path.size() == 0);
    }
}
