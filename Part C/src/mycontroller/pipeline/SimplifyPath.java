package mycontroller.pipeline;

import mycontroller.MyAIController;
import mycontroller.Position;
import mycontroller.RoutingData;
import tiles.LavaTrap;
import tiles.MapTile;

import java.util.Objects;

/**
 * Pipeline for simplifying the path by allowing the car go diagonal
 */
public class SimplifyPath implements Pipeline.Step<RoutingData, MyAIController> {


    /**
     * Execute this pipeline step, simplify the path by allowing the car go diagonal
     *
     * @param routingData    routingData
     * @param myAIController the main controller
     * @return updated routingData
     */
    @Override
    public RoutingData execute(RoutingData routingData, MyAIController myAIController) {

        for (int i = 0; i < routingData.path.size() - 2; i++) {
            if (routingData.path.get(i) == null) continue;

            for (int j = i + 2; j < routingData.path.size(); j++) {

                // select 3 points
                Position pos1 = routingData.path.get(i);
                Position pos2 = routingData.path.get(j - 1);
                Position pos3 = routingData.path.get(j);

                if (pos2 == null || pos3 == null) continue;

                // based on the pos1 and pos3, make a rectangle, if the condition met then the pos2 can be removed
                int minX = Math.round(Math.min(pos1.x, pos3.x));
                int maxX = Math.round(Math.max(pos1.x, pos3.x));
                int minY = Math.round(Math.min(pos1.y, pos3.y));
                int maxY = Math.round(Math.max(pos1.y, pos3.y));

                // skip if pos2 is not in the rectangle of pos1 and pos3
                int pos2X = Math.round(pos2.x), pos2Y = Math.round(pos2.y);
                if (!(pos2X <= maxX && pos2X >= minX && pos2Y <= maxY && pos2Y >= minY)) continue;


                boolean flag = true;

                // if pos1 and pos3 is LavaTrap, then allow to go through LavaTrap
                boolean noLavaTrap = true;
                if (myAIController.mapRecorder.mapTiles[Math.round(pos1.x)][Math.round(pos1.y)] instanceof LavaTrap &&
                        myAIController.mapRecorder.mapTiles[Math.round(pos3.x)][Math.round(pos3.y)] instanceof LavaTrap)
                    noLavaTrap = false;

                for (int x = minX; x <= maxX; x++) {
                    for (int y = minY; y <= maxY; y++) {
                        MapTile current = myAIController.mapRecorder.mapTiles[x][y];

                        // break the loop if there is Wall or LavaTrap in the rectangle
                        if (current.isType(MapTile.Type.WALL) || (current instanceof LavaTrap && noLavaTrap)) {
                            flag = false;
                            break;
                        }

                    }
                    if (!flag) break;
                }

                if (flag) {
                    // clean, remove pos2
                    routingData.path.set(j - 1, null);
                }


            }
        }

        // clean up
        routingData.path.removeIf(Objects::isNull);

        return routingData;
    }
}
