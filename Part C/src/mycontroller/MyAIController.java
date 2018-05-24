package mycontroller;

import controller.CarController;
import swen30006.driving.Simulation;
import world.Car;
import world.WorldSpatial;


import java.util.ArrayList;


public class MyAIController extends CarController {

    private static final float BRAKING_FORCE = 2f;
    private static final float ACCELERATION = 2f;
    private static final float FRICTION_FORCE = 0.5f;

    /** Angle threshold to detect U-turns */
    private static final float U_TURN_THRESHOLD = 120.0f;

    /** Threshold for maximum addition of length for alternative path that includes the next key needed to collect **/
    private static final int MAX_EXTRA_LEN_ALT_PATH = 10;

    private Pipeline<ArrayList<Position>, MapRecorder> pathPlanner;
    private StrategyManager strategyManager;


    private int lastX = -1;
    private int lastY = -1;



    MapRecorder mapRecorder;

    ArrayList<Position> targets = new ArrayList<>();
    ArrayList<Position> route = new ArrayList<>();


	public MyAIController(Car car) {

        super(car);

        mapRecorder = new MapRecorder(getMap(), getKey());
        mapRecorder.addCarView(Math.round(getX()), Math.round(getY()), getView(), getKey());

        pathPlanner = new Pipeline<>();
        pathPlanner.appendStep(new AStar());
        pathPlanner.appendStep(new AvoidWall());
        pathPlanner.appendStep(new RemoveRedundantPath());
        pathPlanner.appendStep(new SimplifyPath());

        strategyManager = new StrategyManager();

	}

    private void calculateTargets() {
        targets = strategyManager.getTargets(this);

        // targets changed, recalculate the route
        calculateRoute();
    }

	private void calculateRoute() {
	    if (targets == null) return;
	    // the last one should be the current position
        System.out.println("targets = " + targets);
        ArrayList<Position> input = new ArrayList<>(targets);
        input.add(new Position(getX(), getY()));
        route = pathPlanner.execute(input, mapRecorder);
    }
    
    
	@Override
	public void update(float delta) {

        int foundFlags = 0;

        if (strategyManager.update(this)) {
            foundFlags = MapRecorder.NEXT_KEY_FOUND; // Strategy changed
        }

        int currentX = Math.round(getX());
        int currentY = Math.round(getY());
        if (currentX!=lastX || currentY!=lastY) {
            foundFlags = mapRecorder.addCarView(Math.round(getX()), Math.round(getY()), getView(), getKey());
        }
        if ((foundFlags & MapRecorder.NEXT_KEY_FOUND) != 0)
            calculateTargets(); // Recalculate targets
        else if ((foundFlags & MapRecorder.LAVA_FOUND) != 0 && route.size() != 0)
            calculateRoute(); // Reroute




        System.out.print("X: ");
        System.out.println(getX());
        System.out.print("Y: ");
        System.out.println(getY());

        if (route.isEmpty()) {
            calculateTargets();
        }

        if (!route.isEmpty()) {

            int targetX = Math.round(route.get(0).x);
            int targetY = Math.round(route.get(0).y);

            Simulation.flagList = route;


            if (currentX==targetX && currentY==targetY) {
                route.remove(0);
                return;
            }


            float targetAngle = getTargetAngle(route.get(0));
            float dist = getTargetDistance(route.get(0));
            System.out.println(dist);
            System.out.println(targetAngle);
            System.out.println(getAngle());

            // ------
            float cmp = compareAngles(getAngle(), targetAngle);
            if (cmp!=0) {
                if (Math.abs(cmp) > U_TURN_THRESHOLD) {
                    // Trying to u-turn, turn to the side further to the wall.
                    int orientationAngle = (int) getAngle();
                    switch (getOrientation()) {
                        case EAST: orientationAngle = WorldSpatial.EAST_DEGREE_MIN; break;
                        case WEST: orientationAngle = WorldSpatial.WEST_DEGREE; break;
                        case SOUTH: orientationAngle = WorldSpatial.SOUTH_DEGREE; break;
                        case NORTH: orientationAngle = WorldSpatial.NORTH_DEGREE; break;
                    }
                    double xOffsetL = Math.cos(orientationAngle + 90),
                           yOffsetL = Math.sin(orientationAngle + 90),
                           xOffsetR = Math.cos(orientationAngle - 90),
                           yOffsetR = Math.sin(orientationAngle - 90);

                    MapRecorder.TileStatus leftTile = mapRecorder.mapStatus
                            [(int) Math.round(getX() + xOffsetL)]
                            [(int) Math.round(getY() + yOffsetL)];
                    MapRecorder.TileStatus rightTile = mapRecorder.mapStatus
                            [(int) Math.round(getX() + xOffsetR)]
                            [(int) Math.round(getY() + yOffsetR)];

                    if (leftTile == MapRecorder.TileStatus.UNREACHABLE) {
                        cmp = -1.0f;
                    } else if (rightTile == MapRecorder.TileStatus.UNREACHABLE) {
                        cmp = 1.0f;
                    }
                }

                if (cmp<0) {
                    turnRight(delta);
                } else {
                    turnLeft(delta);
                }
            }

            float endingSpeed = 0.5f;

            System.out.print("dist: ");
            System.out.println(dist);


            float allowedSpeed = 0f;
            if (Math.abs(cmp) < 60) {
                allowedSpeed = computeAllowedVelocity(dist, endingSpeed);
            } else {
                // big turn
                allowedSpeed = 0.3f;
            }


            System.out.print("speed: ");
            System.out.println(allowedSpeed);

            if (getSpeed()<allowedSpeed) {
                applyForwardAcceleration();
            } else if (getSpeed()>allowedSpeed) {
                applyBrake();
            }



        } else {

            if (getSpeed()>0) {
                applyBrake();
            }

        }




	}
    // v^2 - u^2 = 2as
	float computeAllowedVelocity(float s, float u) {
	    // this is weird but it is what happened in the Car class
	    float a = BRAKING_FORCE-FRICTION_FORCE;
	    return (float) Math.sqrt(2*a*s + u*u);
//        return 0.6f;
    }

    // returns 1 if otherAngle is to the right of sourceAngle,
    //         0 if the angles are identical
    //         -1 if otherAngle is to the left of sourceAngle

    // now return difference
    float compareAngles(float sourceAngle, float otherAngle) {
        // sourceAngle and otherAngle should be in the range -180 to 180
        float difference = otherAngle - sourceAngle;

        if(difference < -180.0f)
            difference += 360.0f;
        if(difference > 180.0f)
            difference -= 360.0f;

        return difference;
    }



    private float getTargetDistance(Position target) {
        float target_x = target.x;// + 0.5f;
        float target_y = target.y;// + 0.5f;

        return (float) Math.sqrt(Math.pow(target_x - getX(),2)+Math.pow(target_y - getY(),2));
    }


    private float getTargetAngle(Position target) {
	    float target_x = target.x;// + 0.5f;
        float target_y = target.y;// + 0.5f;
        float angle = (float) Math.toDegrees(Math.atan2(target_y - getY(), target_x - getX()));

        if(angle < 0){
            angle += 360;
        }
        return angle;
    }


}
