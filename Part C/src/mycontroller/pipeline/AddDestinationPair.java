package mycontroller.pipeline;


import mycontroller.MyAIController;
import mycontroller.Position;
import mycontroller.RoutingData;
import utilities.Coordinate;

public class AddDestinationPair implements Pipeline.Step<RoutingData, MyAIController> {

    @Override
    public RoutingData execute(RoutingData routingData, MyAIController myAIController) {

        Coordinate actualTarget = routingData.path.get(routingData.path.size()-1).toCoordinate();

        if (routingData.targetPairs.containsKey(actualTarget)) {
            routingData.path.add(new Position(routingData.targetPairs.get(actualTarget)));
        }

        return routingData;
    }

}
