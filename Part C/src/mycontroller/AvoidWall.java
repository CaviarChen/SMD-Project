package mycontroller;

import sun.tools.jstat.RawOutputFormatter;

import java.util.ArrayList;

public class AvoidWall implements Pipeline.Step<RoutingData, MyAIController> {

    private static final int[][] DIRECTS = new int[][]{{1,0},{-1,0},{0,1},{0,-1}, {1,1},{-1,1},{1,-1},{-1,-1}};


    @Override
    public RoutingData execute(RoutingData routingData, MyAIController myAIController) {

        ArrayList<Position> outputPath = new ArrayList<>();

        for (Position pos: routingData.path) {
            outputPath.add(positionAvoidWall(pos.x, pos.y, myAIController.mapRecorder));
        }

        routingData.path = outputPath;

        return routingData;
    }


    private Position positionAvoidWall(float x, float y, MapRecorder mapRecorder) {

        int offsetX = 0;
        int offsetY = 0;

        MapRecorder.TileStatus[][] mapStatus = mapRecorder.getTileStatus();

        for(int[] dir: DIRECTS) {
            int newX = Math.round(x) + dir[0];
            int newY = Math.round(y) + dir[1];

            if(mapRecorder.inRange(newX, newY)) {
                if (mapStatus[newX][newY] == MapRecorder.TileStatus.UNREACHABLE) {
                    offsetX += dir[0];
                    offsetY += dir[1];
                }
            }
        }
        if (offsetX<0) offsetX = -1;
        if (offsetX>0) offsetX = 1;
        if (offsetY<0) offsetY = -1;
        if (offsetY>0) offsetY = 1;

        return new Position(x - 0.3f*offsetX, y - 0.3f*offsetY);
    }

}
