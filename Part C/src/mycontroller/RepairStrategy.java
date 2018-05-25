package mycontroller;

import tiles.HealthTrap;
import utilities.Coordinate;

import java.util.ArrayList;

public class RepairStrategy implements Strategy {

    private static final int FINISH_THRESHOLD = 96;
    private static final double DISTANCE_FACTOR = 0.5;
    private static final double TAKE_OVER_THRESHOLD = 100;

    public static double count = 1;

    private int resetCount = 0;

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
        if (myAIController.getHealth() >= FINISH_THRESHOLD) {
            resetCount = 8;
            return true;
        }
        return false;

    }

    public boolean needTakeover(MyAIController myAIController) {

        // Never take over if just got out from the health trap
        if (resetCount>0) {
            return false;
        }


        // Never take over if no health trap is found
        if (myAIController.mapRecorder.healthCoords.isEmpty()) return false;

        if (myAIController.getHealth()>90) return false;

        ArrayList<Position> destination = new ArrayList<>();
        for (Coordinate i: myAIController.mapRecorder.healthCoords) {
            destination.add(new Position(i));
        }


        AStar.Node node = new AStar().start(myAIController.mapRecorder,
                new Position(myAIController.getX(), myAIController.getY()),
                destination
        );



        double value =  ((myAIController.getHealth()/100 + 1) * node.G * DISTANCE_FACTOR);

        count += 1;
        if (count==10) {
            count = 0;
        }

        System.out.println("TAKE OVER EVALUATION VALUE = " + value);

        if (value <= TAKE_OVER_THRESHOLD) {
            return true;
        }
        return false;

//        return myAIController.getHealth() + node.G * DISTANCE_FACTOR <= TAKE_OVER_THRESHOLD;
    }

    public void carMoved() {
        if (resetCount>0) {
            resetCount--;
        }
    }

}
