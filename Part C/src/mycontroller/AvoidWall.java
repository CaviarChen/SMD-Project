package mycontroller;

import java.util.ArrayList;

public class AvoidWall implements Pipeline.Step<ArrayList<Position>, MapRecorder> {

    private static final int[][] DIRECTS = new int[][]{{1,0},{-1,0},{0,1},{0,-1}};
    //{1,1},{-1,1},{1,-1},{-1,-1}

    @Override
    public ArrayList<Position> execute(ArrayList<Position> input, MapRecorder mapRecorder) {

        ArrayList<Position> output = new ArrayList<>();

        for (Position pos: input) {
            output.add(positionAvoidWall(pos.x, pos.y, mapRecorder));
        }

        return output;
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
                    if (offsetX==0) offsetX = dir[0];
                    if (offsetY==0) offsetY = dir[1];
                }
            }
        }

        return new Position(x - 0.3f*offsetX, y - 0.3f*offsetY);
    }

}
