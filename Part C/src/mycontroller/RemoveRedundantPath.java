package mycontroller;

import java.util.Objects;

public class RemoveRedundantPath implements Pipeline.Step<RoutingData, MyAIController> {

    private static final float PRECISION_LEVEL = 0.001f;


    @Override
    public RoutingData execute(RoutingData routingData, MyAIController myAIController) {

        for (int i=0; i<routingData.path.size()-2; i++) {
            if (routingData.path.get(i)==null) continue;

            for (int j=i+2; j<routingData.path.size(); j++) {
                Position pos1 = routingData.path.get(i);
                Position pos2 = routingData.path.get(j);

                if (floatEquals(pos1.x, pos2.x) || floatEquals(pos1.y, pos2.y)) {
                    routingData.path.set(j-1, null);
                } else {
                    break;
                }
            }
        }

        // clean up
        routingData.path.removeIf(Objects::isNull);

        return routingData;
    }


    private boolean floatEquals(float a, float b) {
        return Math.abs(a-b) < PRECISION_LEVEL;
    }

}
