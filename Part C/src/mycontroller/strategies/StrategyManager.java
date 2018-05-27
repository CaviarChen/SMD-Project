package mycontroller.strategies;

import mycontroller.MyAIController;
import mycontroller.RoutingData;

import java.util.Stack;

/**
 * Strategy manager.
 * Manages strategies used while retrieving targets, and
 * switch between strategies at appropriate time.
 */
public class StrategyManager {

    /**
     * Stack of strategies currently activated.
     */
    private Stack<Strategy> strategyStack;

    /**
     * Repair strategy object.
     * Repair strategy is separately dealt with as it can
     * overtake other strategy at any point of time.
     */
    private RepairStrategy repairStrategy;

    /**
     * Create an strategy manager
     */
    public StrategyManager() {
        // Initialize the strategy stack with currently activated
        // strategies in order. First strategy to use is added
        // lastly.
        strategyStack = new Stack<>();
        strategyStack.push(new EscapeStrategy());
        strategyStack.push(new RetrieveKeyStrategy());

        repairStrategy = new RepairStrategy();
    }

    /**
     * Get possible destinations from the current activated
     * strategy.
     *
     * @param myAIController the running "My AI Controller"
     * @return Routing data with the list of destinations
     */
    public RoutingData getTargets(MyAIController myAIController) {
        return strategyStack.peek().getTargets(myAIController);
    }

    /**
     * Switch strategy if necessary.
     *
     * @param myAIController the running "My AI Controller"
     * @return true if current strategy is changed.
     */
    public boolean update(MyAIController myAIController) {

        // Let Repair Strategy take over if necessary.
        if (strategyStack.peek() != repairStrategy &&
                repairStrategy.needTakeOver(myAIController)) {
            strategyStack.push(repairStrategy);
            return true;
        }

        // Pop up the current strategy if finished
        if (strategyStack.peek().isFinished(myAIController)) {
            strategyStack.pop();
            return true;
        }

        return false;
    }

    /**
     * Inform car strategy if car is moved one cell.
     */
    public void carMoved() {
        repairStrategy.carMoved();
    }


}
