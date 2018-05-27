package mycontroller.pipeline;

import mycontroller.MyAIController;
import mycontroller.Position;
import mycontroller.RoutingData;
import utilities.Coordinate;

/**
 * Pipeline step for add the destination pair
 */
public class AddDestinationPair implements Pipeline.Step<RoutingData, MyAIController> {

    /**
     * Execute this pipeline step, if the destination is only partial, then add the corresponding one
     *
     * @param routingData    routingData
     * @param myAIController the main controller
     * @return updated routingData
     */
    @Override
    public RoutingData execute(RoutingData routingData, MyAIController myAIController) {

        Coordinate actualTarget = routingData.path.get(routingData.path.size() - 1).toCoordinate();

        // current target is only partial, add the another one
        if (routingData.targetPairs.containsKey(actualTarget)) {
            routingData.path.add(new Position(routingData.targetPairs.get(actualTarget)));
        }

        return routingData;
    }

}
