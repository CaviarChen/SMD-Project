package mycontroller;

import tiles.HealthTrap;
import utilities.Coordinate;

import java.util.ArrayList;

public class RepairStrategy implements Strategy {

    private static final int FINISH_THRESHOLD = 96;
    private static final double DISTANCE_FACTOR = 0.5;
    private static final double TAKE_OVER_THRESHOLD = 50;

    @Override
    public ArrayList<Position> getTargets(MyAIController myAIController) {

        int currentX = Math.round(myAIController.getX());
        int currentY = Math.round(myAIController.getY());

        // already on HealthTrap
        if (myAIController.mapRecorder.mapTiles[currentX][currentY] instanceof HealthTrap) {
            return null;
        }

        ArrayList<Position> output = new ArrayList<>();

        for (Coordinate coord : myAIController.mapRecorder.healthCoords) {
            output.add(new Position(coord));
        }

        return output;
    }

    @Override
    public boolean isFinished(MyAIController myAIController) {
        return (myAIController.getHealth() >= FINISH_THRESHOLD);
    }

    public boolean needTakeover(MyAIController myAIController) {
        // Never take over if just got out from the health trap

        // Never take over if no health trap is found
        if (myAIController.mapRecorder.healthCoords.isEmpty()) return false;

        ArrayList<Position> destination = new ArrayList<>();
        for (Coordinate i: myAIController.mapRecorder.healthCoords) {
            destination.add(new Position(i));
        }


        AStar.Node node = new AStar().start(myAIController.mapRecorder,
                new Position(myAIController.getX(), myAIController.getY()),
                destination
        );

        System.out.println("TAKE OVER EVALUATION VALUE = " + (myAIController.getHealth() + node.G * DISTANCE_FACTOR));

        return myAIController.getHealth() + node.G * DISTANCE_FACTOR <= TAKE_OVER_THRESHOLD;
    }

}
