package mycontroller;

import tiles.LavaTrap;
import utilities.Coordinate;
import world.World;

import java.util.ArrayList;

public class RetrieveKeyStrategy implements Strategy {

    @Override
    public RoutingData getTargets(MyAIController myAIController) {

        RoutingData output = new RoutingData();

        int keyCount = myAIController.getKey();

        boolean allKeyFound = true;
        for (int i = keyCount - 2; i >= 0; i--)
            allKeyFound &= myAIController.mapRecorder.keysCoord[i] != null;

        if (!allKeyFound) {
            output.targets.addAll(myAIController.mapRecorder.coordinatesToExplore());
        }

        // If next key pos is known
        if (keyCount > 1 && myAIController.mapRecorder.keysCoord[keyCount - 2] != null) {
            Coordinate keyCoordinate = myAIController.mapRecorder.keysCoord[keyCount - 2];
            // Check if we can rush across the key
            int xl = keyCoordinate.x, xr = xl,
                yt = keyCoordinate.y, yb = yt;
            boolean xRush = true, yRush = true;
            // Check X direction
            while (xl >= 0) {
                if (myAIController.mapRecorder.mapStatus[xl][keyCoordinate.y] == MapRecorder.TileStatus.UNREACHABLE) {
                    xRush = false;
                    break;
                } else if (myAIController.mapRecorder.mapTiles[xl][keyCoordinate.y] instanceof LavaTrap) {
                    xl--;
                } else {
                    break;
                }
            }
            while (xr < World.MAP_WIDTH) {
                if (myAIController.mapRecorder.mapStatus[xr][keyCoordinate.y] == MapRecorder.TileStatus.UNREACHABLE) {
                    xRush = false;
                    break;
                } else if (myAIController.mapRecorder.mapTiles[xr][keyCoordinate.y] instanceof LavaTrap) {
                    xr++;
                } else {
                    break;
                }
            }
            while (yt >= 0) {
                if (myAIController.mapRecorder.mapStatus[keyCoordinate.x][yt] == MapRecorder.TileStatus.UNREACHABLE) {
                    yRush = false;
                    break;
                } else if (myAIController.mapRecorder.mapTiles[keyCoordinate.x][yt] instanceof LavaTrap) {
                    yt++;
                } else {
                    break;
                }
            }
            while (yb < World.MAP_HEIGHT) {
                if (myAIController.mapRecorder.mapStatus[keyCoordinate.x][yb] == MapRecorder.TileStatus.UNREACHABLE) {
                    yRush = false;
                    break;
                } else if (myAIController.mapRecorder.mapTiles[keyCoordinate.x][yb] instanceof LavaTrap) {
                    yb--;
                } else {
                    break;
                }
            }

            if (xRush) {
                Coordinate first = new Coordinate(xl, keyCoordinate.y),
                        second = new Coordinate(xr, keyCoordinate.y);
                output.targetPairs.put(first, second);
                output.targetPairs.put(second, first);
                output.targets.add(first);
                output.targets.add(second);
            }
            if (yRush) {
                Coordinate first = new Coordinate(keyCoordinate.x, yt),
                        second = new Coordinate(keyCoordinate.x, yb);
                output.targetPairs.put(first, second);
                output.targetPairs.put(second, first);
                output.targets.add(first);
                output.targets.add(second);
            }
            if (!xRush && !yRush)
                output.targets.add(keyCoordinate);
        }

        return output;
    }

    @Override
    public boolean isFinished(MyAIController myAIController) {
        return (myAIController.getKey() == 1);
    }
}
