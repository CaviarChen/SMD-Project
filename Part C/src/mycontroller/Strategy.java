package mycontroller;

public interface Strategy {

    RoutingData getTargets(MyAIController myAIController);

    boolean isFinished(MyAIController myAIController);
}
