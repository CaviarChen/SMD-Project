package mycontroller;


public class AddDestinationPair implements Pipeline.Step<RoutingData, MyAIController> {

    @Override
    public RoutingData execute(RoutingData routingData, MyAIController myAIController) {

        Position actualTarget = routingData.path.get(routingData.path.size()-1);

        if (routingData.targetPairs.containsKey(actualTarget)) {
            routingData.path.add(new Position(routingData.targetPairs.get(actualTarget)));
        }

        return routingData;
    }

}
