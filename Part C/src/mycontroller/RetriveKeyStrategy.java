package mycontroller;

import utilities.Coordinate;

import java.util.ArrayList;

public class RetriveKeyStrategy implements Strategy {

    @Override
    public ArrayList<Position> getTargets(MyAIController myAIController) {

        ArrayList<Position> output = new ArrayList<>();

        int keyCount = myAIController.getKey();

        boolean allKeyFound = true;
        for (int i = keyCount - 2; i >= 0; i--)
            allKeyFound &= myAIController.mapRecorder.keysCoord[i] != null;

        if (!allKeyFound) {
            for(Coordinate coord: myAIController.mapRecorder.coordinatesToExplore()) {
                output.add(new Position(coord));
            }
        }

        // If next key pos is known
        if (keyCount > 1 && myAIController.mapRecorder.keysCoord[keyCount - 2] != null) {
            Coordinate coord = myAIController.mapRecorder.keysCoord[keyCount - 2];
            output.add(new Position(coord));
        }

        return output;
    }

    @Override
    public boolean isFinished(MyAIController myAIController) {
        return (myAIController.getKey() == 1);
    }
}
