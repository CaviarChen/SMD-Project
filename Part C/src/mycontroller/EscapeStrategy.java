package mycontroller;

import utilities.Coordinate;

import java.util.ArrayList;
import java.util.HashSet;

public class EscapeStrategy implements Strategy {

    @Override
    public RoutingData getTargets(MyAIController myAIController) {

        RoutingData output = new RoutingData();
        output.targets = new HashSet<>(myAIController.mapRecorder.finishCoords);

        return output;
    }

    @Override
    public boolean isFinished(MyAIController myAIController) {
        return false;
    }


}
