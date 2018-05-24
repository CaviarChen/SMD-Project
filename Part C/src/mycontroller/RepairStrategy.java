package mycontroller;

import tiles.HealthTrap;
import utilities.Coordinate;

import java.util.ArrayList;

public class RepairStrategy implements Strategy {
    @Override
    public ArrayList<Position> getTargets(MyAIController myAIController) {

        int currentX = Math.round(myAIController.getX());
        int currentY = Math.round(myAIController.getY());


        // already on HealthTrap
        if (myAIController.mapRecorder.mapTiles[currentX][currentY] instanceof HealthTrap) {
            return null;
        }


        ArrayList<Position> output = new ArrayList<>();

        for(Coordinate coord: myAIController.mapRecorder.healthCoords) {
            output.add(new Position(coord));
        }

        return output;
    }

    @Override
    public boolean isFinished(MyAIController myAIController) {
        return (myAIController.getHealth() >= 100);
    }

    public boolean needTakeover(MyAIController myAIController) {
        return ((myAIController.getHealth() < 50) && myAIController.mapRecorder.healthCoords.size()>0);
    }
}
