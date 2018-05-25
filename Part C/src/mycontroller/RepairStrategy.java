package mycontroller;

import tiles.HealthTrap;
import utilities.Coordinate;

import java.util.*;

public class RepairStrategy implements Strategy {

    private static final int FINISH_THRESHOLD = 96;
    private static final double DISTANCE_FACTOR = 0.5;
    private static final double TAKE_OVER_THRESHOLD = 100;
    private static final int LEAVE_HEALTH_TRAP = 7;

    private boolean carMoved = false;

    private static final TreeMap<Integer, Double> SPEED_LIMIT = new TreeMap<>(Comparator.reverseOrder());

    static {
        SPEED_LIMIT.put(95, Double.NEGATIVE_INFINITY);
        SPEED_LIMIT.put(85, 100.0);
        SPEED_LIMIT.put(55, 500.0);
        SPEED_LIMIT.put(30, 700.0);
        SPEED_LIMIT.put(0, Double.POSITIVE_INFINITY);
    }

    private int resetCount = 0;

    @Override
    public RoutingData getTargets(MyAIController myAIController) {

        int currentX = Math.round(myAIController.getX());
        int currentY = Math.round(myAIController.getY());

        RoutingData output = new RoutingData();

        // already on HealthTrap
        if (myAIController.mapRecorder.mapTiles[currentX][currentY] instanceof HealthTrap) {
            output.targets = null;
            return output;
        }

        output.targets = new HashSet<>(myAIController.mapRecorder.healthCoords);

        return output;
    }

    @Override
    public boolean isFinished(MyAIController myAIController) {
        if (myAIController.getHealth() >= FINISH_THRESHOLD) {
            resetCount = LEAVE_HEALTH_TRAP;
            return true;
        }
        return false;

    }

    public boolean needTakeover(MyAIController myAIController) {

        int currentX = Math.round(myAIController.getX()),
                currentY = Math.round(myAIController.getY());
        // Stay at health trap if pass by and need to recover
        if (myAIController.getHealth() < 90 &&
                myAIController.mapRecorder.mapTiles[currentX][currentY] instanceof HealthTrap) {
            System.out.println("PASS BY RECOVERY ----");
            return true;
        }

        if (!carMoved) return false;
        carMoved = false;

        // Never take over if no health trap is found
        if (myAIController.mapRecorder.healthCoords.isEmpty()) return false;

        // Never take over if just got out from the health trap
        if (resetCount > 0) {
            return false;
        }

        if (myAIController.getHealth() > 80) return false;


        AStar.Node node = new AStar().start(myAIController.mapRecorder,
                new Coordinate(currentX, currentY),
                myAIController.mapRecorder.healthCoords
        );

        int pathSize = myAIController.getRoutingData().path.size();

        if (pathSize > 0) {
            HashSet<Coordinate> carDestination = new HashSet<>();
            carDestination.add(myAIController.getRoutingData().path.get(pathSize - 1).toCoordinate());

            AStar.Node nodeDest = new AStar().start(myAIController.mapRecorder,
                    new Coordinate(currentX, currentY),
                    carDestination
                    );

            System.out.print("ORIGINAL DEST " + nodeDest.G);
            System.out.print(" v. " + node.G + " = TAKE OVER");
            System.out.println(" @ = " + myAIController.getHealth());

            return node.G < nodeDest.G;
        }
//        double value =  ((myAIController.getHealth()/100 + 1) * node.G * DISTANCE_FACTOR);


        System.out.print(node.G + " = TAKE OVER");
        System.out.println("@ = " + myAIController.getHealth());
        return false;


//        for (Map.Entry<Integer, Double> i: SPEED_LIMIT.entrySet()) {
//            if (myAIController.getHealth() > i.getKey())
//                return node.G < i.getValue();
//        }
//        return true;
//        return value <= TAKE_OVER_THRESHOLD;
//        return myAIController.getHealth() + node.G * DISTANCE_FACTOR <= TAKE_OVER_THRESHOLD;
    }

    public void carMoved() {
        if (resetCount>0) {
            resetCount--;
        }
        carMoved = true;
    }

}
