package mycontroller;

import utilities.Coordinate;

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
            for(Coordinate coord: myAIController.mapRecorder.coordinatesToExplore()) {
                output.targets.add(coord);
            }
        }

        // If next key pos is known
        if (keyCount > 1 && myAIController.mapRecorder.keysCoord[keyCount - 2] != null) {
            Coordinate coord = myAIController.mapRecorder.keysCoord[keyCount - 2];
            output.targets.add(coord);
        }

        return output;
    }

    @Override
    public boolean isFinished(MyAIController myAIController) {
        return (myAIController.getKey() == 1);
    }
}
