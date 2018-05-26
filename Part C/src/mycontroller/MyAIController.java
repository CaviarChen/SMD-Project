package mycontroller;

import controller.CarController;
import mycontroller.pipeline.*;
import mycontroller.strategies.StrategyManager;
import swen30006.driving.Simulation;
import utilities.Coordinate;
import world.Car;
import world.WorldSpatial;



public class MyAIController extends CarController {

    /** Physics constants form Car */
    private static final float BRAKING_FORCE = 2f;
    private static final float FRICTION_FORCE = 0.5f;

    /** Angle threshold to detect U-turns */
    private static final float U_TURN_THRESHOLD = 120.0f;


    private Pipeline<RoutingData, MyAIController> pathPlanner;
    private StrategyManager strategyManager;

    /** record the last position of the car*/
    private int lastX = -1;
    private int lastY = -1;


    public MapRecorder mapRecorder;

    private RoutingData routingData;

    /**
     * Constructor
     * @param car car object
     */
	public MyAIController(Car car) {

        super(car);

        // init MapRecorder
        mapRecorder = new MapRecorder(getMap(), getKey());
        mapRecorder.addCarView(getView(), getKey());

        // init pathPlanner Pipeline
        pathPlanner = new Pipeline<>();
        pathPlanner.appendStep(new AStar());
        pathPlanner.appendStep(new AddDestinationPair());
        pathPlanner.appendStep(new AvoidWall());
        pathPlanner.appendStep(new RemoveRedundantPath());
        pathPlanner.appendStep(new SimplifyPath());

        // init strategy Manager
        strategyManager = new StrategyManager();

	}

    /**
     * @return the current routing data
     */
    public RoutingData getRoutingData() {
        return routingData;
    }

    /**
     * calculate the next targets
     */
    private void calculateTargets() {
        routingData = strategyManager.getTargets(this);

        // targets changed, recalculate the route
        calculateRoute();
    }

    /**
     * plan the route for the car
     */
	private void calculateRoute() {
	    if (routingData.targets == null) return;
	    // the last one should be the current position

        routingData = pathPlanner.execute(routingData, this);
    }


    /**
     * update is triggered by the simulation every delta time
     * @param delta the time delta
     */
	@Override
	public void update(float delta) {

        boolean reCalculateTargetFlag = false;
        boolean reCalculateRouteFlag = false;

        if (strategyManager.update(this)) {
            reCalculateTargetFlag = true;
        }

        int currentX = Math.round(getX());
        int currentY = Math.round(getY());

        if (currentX!=lastX || currentY!=lastY) {
            // if position moved, add car view
            int foundFlags = mapRecorder.addCarView(getView(), getKey());
            lastX = currentX;
            lastY = currentY;

            if ((foundFlags & MapRecorder.NEXT_KEY_FOUND) != 0) {
                // found the next key
                reCalculateTargetFlag = true;
            }
            if ((foundFlags & MapRecorder.LAVA_FOUND) != 0 && routingData.path.size() != 0) {
                // found a lava
                reCalculateRouteFlag = true;
            }

            // notify StrategyManager
            strategyManager.carMoved();
        }

        // no more route
        if (routingData==null || routingData.path.isEmpty()) {
           reCalculateTargetFlag = true;
        }

        if (reCalculateRouteFlag || reCalculateTargetFlag) {
            if (reCalculateTargetFlag) {
                calculateTargets();
            } else if(reCalculateRouteFlag) {
                calculateRoute();
            }
        }

        // recheck the route
        if (routingData==null || routingData.path.isEmpty()) {
            // need to wait
            if (getSpeed()>0) {
                applyBrake();
            }
            return;
        }


        driveCar(delta, currentX, currentY);
	}

    /**
     * drive the car
     * @param delta delta from update
     * @param currentX current X position
     * @param currentY current Y position
     */
    private void driveCar(float delta, int currentX, int currentY) {
        int targetX = Math.round(routingData.path.get(0).x);
        int targetY = Math.round(routingData.path.get(0).y);

        // Debug only
         Simulation.flagList = routingData.path;

        if (currentX==targetX && currentY==targetY) {
            // car reaches a position in route
            routingData.path.remove(0);

            // clean partial destination in RoutingData
            Coordinate currentCoord = new Coordinate(currentX, currentY);
            routingData.targets.remove(currentCoord);
            if (routingData.targetPairs.containsKey(currentCoord)) {
                routingData.targetPairs.clear();
            }
            if (routingData.path.size()==0) return;
        }

        Position currentPos = new Position(getX(), getY());
        float targetAngle = getAngleBetweenPos(currentPos, routingData.path.get(0));
        float dist = getTargetDistance(routingData.path.get(0));

        // direction control
        float cmp = compareAngles(getAngle(), targetAngle);
        if (cmp!=0) {
            if (Math.abs(cmp) > U_TURN_THRESHOLD) {
                // Trying to u-turn, turn to the side further to the wall.
                cmp = doUTurn(cmp);
            }
            if (cmp<0) {
                turnRight(delta);
            } else {
                turnLeft(delta);
            }
        }


        // speed control
        float endingSpeed = getAllowedEndingSpeed(currentPos);


        float allowedSpeed;
        if (Math.abs(cmp) < 40) {
            allowedSpeed = computeAllowedVelocity(dist, endingSpeed);
        } else {
            // big turn
            allowedSpeed = 0.2f;
        }

        if (getSpeed()<allowedSpeed) {
            applyForwardAcceleration();
        } else if (getSpeed()>allowedSpeed) {
            applyBrake();
        }
    }

    /**
     * decide which way the car should turn in order to avoid hitting the wall
     * @param cmp
     * @return new cmp value
     */
    private float doUTurn(float cmp) {
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
        return cmp;
    }

    /**
     * based on the ending speed and the distance, calculate the current max speed
     * Formula: v^2 - u^2 = 2as
     * @param s the distance
     * @param u the ending speed
     * @return
     */
	private float computeAllowedVelocity(float s, float u) {
	    // calculate the breaking acceleration
        // note that in this simulation, Physics is unlike the real world
        // the direction of the friction is opposite to the acceleration instead of velocity
	    float a = BRAKING_FORCE-FRICTION_FORCE;

	    return (float) Math.sqrt(2*a*s + u*u);
    }

    /**
     * @param sourceAngle
     * @param otherAngle
     * @return the angle ([-180,+180]) difference between two given angle
     */
    private float compareAngles(float sourceAngle, float otherAngle) {
        float difference = otherAngle - sourceAngle;

        if(difference < -180.0f)
            difference += 360.0f;
        if(difference > 180.0f)
            difference -= 360.0f;

        return difference;
    }

    /**
     * @param currentPos the current position
     * @return the speed that the car allow to have when reaches the next target
     */
    private float getAllowedEndingSpeed(Position currentPos) {
	    if (routingData.path.size()<2) {
	        return 0.3f;
        }

        float angle1 = getAngleBetweenPos(currentPos, routingData.path.get(0));
        float angle2 = getAngleBetweenPos(routingData.path.get(0), routingData.path.get(1));
        if (Math.abs(angle1-angle2) < 45) {
            return 1.4f;
        }
        return 0.4f;
    }

    /**
     * @param target the given target
     * @return the distance between the current position and the target
     */
    private float getTargetDistance(Position target) {
        return (float) Math.sqrt(Math.pow(target.x - getX(),2)+Math.pow(target.y - getY(),2));
    }

    /**
     * @param pos1 position 1
     * @param pos2 position 2
     * @return the angle [0,360) of pos1 aiming at pos2
     */
    private float getAngleBetweenPos(Position pos1, Position pos2) {
        float angle = (float) Math.toDegrees(Math.atan2(pos2.y - pos1.y, pos2.x - pos1.x));

        if(angle < 0){
            angle += 360;
        }
        return angle;
    }


}
