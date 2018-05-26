package mycontroller.strategies;

import mycontroller.MyAIController;
import mycontroller.RoutingData;

import java.util.HashSet;

/**
 * Strategy used to navigate the car to the "finish area".
 * Activated when all keys are collected.
 */
public class EscapeStrategy implements Strategy {

    /**
     * get the targets decided by the strategy
     * @param myAIController the main controller
     * @return relative routing data
     */
    @Override
    public RoutingData getTargets(MyAIController myAIController) {

        RoutingData output = new RoutingData();
        output.targets = new HashSet<>(myAIController.mapRecorder.finishCoords);

        return output;
    }

    /**
     * check if the current strategy is finished (and will be pop out
     * from the strategy stack)
     * @param myAIController the main controller
     * @return false, as the game will end itself when the car hits
     * the finish area (when all keys are collected), this strategy
     * will never end as a fallback.
     */
    @Override
    public boolean isFinished(MyAIController myAIController) {
        return false;
    }


}
