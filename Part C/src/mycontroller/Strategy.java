package mycontroller;

import java.util.ArrayList;

public interface Strategy {
    ArrayList<Position> getTargets(MyAIController myAIController);

    boolean isFinished(MyAIController myAIController);
}
