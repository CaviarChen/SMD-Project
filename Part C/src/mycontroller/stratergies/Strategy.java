package mycontroller.stratergies;

import mycontroller.MyAIController;
import mycontroller.RoutingData;

public interface Strategy {

    RoutingData getTargets(MyAIController myAIController);

    boolean isFinished(MyAIController myAIController);
}
