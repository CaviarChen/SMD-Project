package mycontroller;

import java.util.ArrayList;
import java.util.Stack;

public class StrategyManager {

    private Stack<Strategy> strategyStack;

    private RepairStrategy repairStrategy;

    public StrategyManager() {
        strategyStack = new Stack<>();
        strategyStack.push(new EscapeStrategy());
        strategyStack.push(new RetriveKeyStrategy());

        repairStrategy = new RepairStrategy();
    }

    public ArrayList<Position> getTargets(MyAIController myAIController) {
        return strategyStack.peek().getTargets(myAIController);
    }

    public boolean update(MyAIController myAIController) {

        if (strategyStack.peek()!=repairStrategy && repairStrategy.needTakeover(myAIController)) {
            strategyStack.push(repairStrategy);
            return true;
        }

        if (strategyStack.peek().isFinished(myAIController)) {
            strategyStack.pop();
            return true;
        }

        return false;
    }


}
