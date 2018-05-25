package mycontroller.stratergies;

import mycontroller.MapRecorder;
import mycontroller.MyAIController;
import mycontroller.RoutingData;
import tiles.LavaTrap;
import utilities.Coordinate;

public class RetrieveKeyStrategy implements Strategy {

    private static final int[][] DIRECTIONS = new int[][]{{1,0},{-1,0},{0,1},{0,-1}};

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
            getKeyRouting(output, myAIController.mapRecorder, keyCount - 2);
        }

        return output;
    }

    private void getKeyRouting(RoutingData routingData, MapRecorder mapRecorder, int keyID) {

            Coordinate keyCoord = mapRecorder.keysCoord[keyID];
            Coordinate[] rushCoord = new Coordinate[DIRECTIONS.length];

            for(int i=0; i<DIRECTIONS.length; i++) {
                int x = keyCoord.x;
                int y = keyCoord.y;
                int selectedX = -1;
                int selectedY = -1;
                int awayFromLava = 0;
                // make sure it is in the map
                while (mapRecorder.inRange(x,y)) {
                    x += DIRECTIONS[i][0];
                    y += DIRECTIONS[i][1];

                    if (mapRecorder.mapStatus[x][y] == MapRecorder.TileStatus.UNREACHABLE) break;
                    if (!(mapRecorder.mapTiles[x][y] instanceof LavaTrap)) {
                        // find a road
                        selectedX = x;
                        selectedY = y;
                        awayFromLava += 1;
                        if (awayFromLava==2) break;

                    } else {
                        if (awayFromLava>0) break;
                    }
                }

                if (selectedX!=-1) {
                    // has a rush point
                    rushCoord[i] = new Coordinate(selectedX, selectedY);
                }
            }

        boolean hasRushPoint = false;
        for (int i=0; i+1<DIRECTIONS.length; i+=2) {
            if (rushCoord[i]!=null && rushCoord[i+1]!=null) {
                hasRushPoint = true;
                Coordinate first = rushCoord[i], second = rushCoord[i+1];
                routingData.targetPairs.put(first, second);
                routingData.targetPairs.put(second, first);
                routingData.targets.add(first);
                routingData.targets.add(second);

            }
        }

        if (!hasRushPoint) {
            // use the normal way
            routingData.targets.add(keyCoord);
        }



    }

    @Override
    public boolean isFinished(MyAIController myAIController) {
        return (myAIController.getKey() == 1 && myAIController.getRoutingData().path.size() == 0);
    }
}
