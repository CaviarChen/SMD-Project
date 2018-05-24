package mycontroller;

import utilities.Coordinate;

import java.util.ArrayList;

public class EscapeStrategy implements Strategy {

    @Override
    public ArrayList<Position> getTargets(MyAIController myAIController) {

        ArrayList<Position> output = new ArrayList<>();

        for(Coordinate coord: myAIController.mapRecorder.finishCoords) {
            output.add(new Position(coord));
        }

        return output;
    }

    @Override
    public boolean isFinished(MyAIController myAIController) {
        return false;
    }


}
