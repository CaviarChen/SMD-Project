package mycontroller.stratergies;

import mycontroller.MyAIController;
import mycontroller.RoutingData;

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
