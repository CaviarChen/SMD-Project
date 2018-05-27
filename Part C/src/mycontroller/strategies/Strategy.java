/**
 * Group 44
 */

package mycontroller.strategies;

import mycontroller.MyAIController;
import mycontroller.RoutingData;

/**
 * Interface for a strategy
 */
public interface Strategy {

    /**
     * get the targets decided by the strategy
     *
     * @param myAIController the main controller
     * @return relative routing data
     */
    RoutingData getTargets(MyAIController myAIController);

    /**
     * check if the current strategy is finished (and will be pop out
     * from the strategy stack)
     *
     * @param myAIController the main controller
     * @return boolean value
     */
    boolean isFinished(MyAIController myAIController);
}
