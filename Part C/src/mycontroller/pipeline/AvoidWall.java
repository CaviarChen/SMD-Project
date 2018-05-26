package mycontroller.pipeline;

import mycontroller.MapRecorder;
import mycontroller.MyAIController;
import mycontroller.Position;
import mycontroller.RoutingData;

import java.util.ArrayList;

/**
 * Pipeline for adjusting the path to avoid hitting the wall
 */
public class AvoidWall implements Pipeline.Step<RoutingData, MyAIController> {

    // 8 directions for checking the wall
    private static final int[][] DIRECTS = new int[][]{{1,0},{-1,0},{0,1},{0,-1}, {1,1},{-1,1},{1,-1},{-1,-1}};


    /**
     * Execute this pipeline step, adjust the path to avoid hitting the wall
     * @param routingData routingData
     * @param myAIController the main controller
     * @return updated routingData
     */
    @Override
    public RoutingData execute(RoutingData routingData, MyAIController myAIController) {

        ArrayList<Position> outputPath = new ArrayList<>();

        // for every position on the path, adjust the position
        for (Position pos: routingData.path) {
            outputPath.add(positionAvoidWall(pos.x, pos.y, myAIController.mapRecorder));
        }

        routingData.path = outputPath;

        return routingData;
    }


    /**
     * adjust the position to avoid hitting the wall
     * @param x x position
     * @param y y posiiton
     * @param mapRecorder the mapRecorder proving map information
     * @return a new position
     */
    private Position positionAvoidWall(float x, float y, MapRecorder mapRecorder) {

        int offsetX = 0;
        int offsetY = 0;

        MapRecorder.TileStatus[][] mapStatus = mapRecorder.getTileStatus();

        for(int[] dir: DIRECTS) {
            int newX = Math.round(x) + dir[0];
            int newY = Math.round(y) + dir[1];

            // for every directions, if a wall is found, adjust the offset
            if(mapRecorder.inRange(newX, newY)) {
                if (mapStatus[newX][newY] == MapRecorder.TileStatus.UNREACHABLE) {
                    offsetX += dir[0];
                    offsetY += dir[1];
                }
            }
        }
        // limit the offset
        if (offsetX<0) offsetX = -1;
        if (offsetX>0) offsetX = 1;
        if (offsetY<0) offsetY = -1;
        if (offsetY>0) offsetY = 1;

        // return the new position
        return new Position(x - 0.3f*offsetX, y - 0.3f*offsetY);
    }

}
