package mycontroller;

import controller.CarController;
import tiles.MapTile;
import utilities.Coordinate;
import world.Car;
import world.WorldSpatial;


import java.util.ArrayList;
import java.util.Objects;


public class MyAIController extends CarController{

    private static final float BRAKING_FORCE = 2f;
    private static final float ACCELERATION = 2f;
    private static final float FRICTION_FORCE = 0.5f;
    private static final int[][] DIRECTS = new int[][]{{1,0},{-1,0},{0,1},{0,-1},{1,1},{-1,1},{1,-1},{-1,-1}};
    private static final float PRECISION_LEVEL = 0.001f;

    /** Angle threshold to detect U-turns */
    private static final float U_TURN_THRESHOLD = 120.0f;

//    String[] test_pos = new String[]{"3,3", "7,3", "7,11"};  // easy-map
//    String[] test_pos = new String[]{"26,2", "21,12", "2,12", "2,2", "26,2", "21,12", "2,12", "2,2"}; // test-key-map
//    String[] test_pos = new String[]{"23,3", "23,17"}; // narrow-road


    MapRecorder mapRecorder;
    ArrayList<Position> targetPositions = new ArrayList<>();


	public MyAIController(Car car) {

        super(car);

//	    for(String s: test_pos) {
//	        targetPositions.add(new Coordinate(s));
//        }

        mapRecorder = new MapRecorder(getMap());

//        AStar aStar = new AStar(mapRecorder, 2, 2, 21, 12);
//	    ArrayList<Node> path = aStar.start();
//	    for (Node n:path) {
//            targetPositions.add(avoidWall(n.coord.x, n.coord.y));
//        }

        // Use the simple random path to navigate the car to explore
        // the map
        loadNextExplorePoint();
	}

    /**
     * Load next explore point.
     * Return True if a point is found. False otherwise.
     */
	private boolean loadNextExplorePoint() {
        Coordinate firstPt = mapRecorder.getNearestExplorationPoint(getX(), getY());
        if (firstPt == null) return false;
        ArrayList<Node> path = new AStar(mapRecorder,
                Math.round(getX()), Math.round(getY()),
                firstPt.x, firstPt.y
        ).start();
        for (Node n: path) {
            targetPositions.add(avoidWall(n.coord.x, n.coord.y));
        }

        System.out.println(targetPositions.size());
        removeUselessPos(targetPositions);
        System.out.println(targetPositions.size());
        return true;
    }

	private boolean floatEquals(float a, float b) {
	    return Math.abs(a-b) < PRECISION_LEVEL;
    }


	private Position avoidWall(int x, int y) {

	    int offsetX = 0;
	    int offsetY = 0;

	    MapRecorder.TileStatus[][] mapStatus = mapRecorder.getTileStatus();

	    for(int[] dir: DIRECTS) {
	        int newX = x + dir[0];
            int newY = y + dir[1];

            if(mapRecorder.inRange(newX, newY)) {
                if (mapStatus[newX][newY] == MapRecorder.TileStatus.UNREACHABLE) {
                    if (offsetX==0) offsetX = dir[0];
                    if (offsetY==0) offsetY = dir[1];
                }
            }
        }

        return new Position(x - 0.2f*offsetX, y - 0.2f*offsetY);
    }

    private void removeUselessPos(ArrayList<Position> positions) {
	    for (int i=0; i<positions.size()-2; i++) {
	        if (positions.get(i)==null) continue;;

	        for (int j=i+2; j<positions.size(); j++) {
                Position pos1 = positions.get(i);
                Position pos2 = positions.get(j);

                if (floatEquals(pos1.x, pos2.x) || floatEquals(pos1.y, pos2.y)) {
                    positions.set(j-1, null);
                } else {
                    break;
                }
            }
        }

        // clean up
        positions.removeIf(Objects::isNull);

    }

	@Override
	public void update(float delta) {

        mapRecorder.addCarView(Math.round(getX()), Math.round(getY()), getView());

        System.out.print("X: ");
        System.out.println(getX());
        System.out.print("Y: ");
        System.out.println(getY());

        if (!targetPositions.isEmpty() || loadNextExplorePoint()) {


            int currentX = Math.round(getX());
            int currentY = Math.round(getY());

            int targetX = Math.round(targetPositions.get(0).x);
            int targetY = Math.round(targetPositions.get(0).y);

//            if (Math.abs(targetX - getX())<=0.1 && Math.abs(targetY - getY())<=0.1) {
//                targetPositions.remove(0);
//                return;
//            }

            if (currentX==targetX && currentY==targetY) {
                targetPositions.remove(0);
                return;
            }


            float targetAngle = getTargetAngle(targetPositions.get(0));
            float dist = getTargetDistance(targetPositions.get(0));
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

            float endingSpeed = 0f;

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
    float compareAngles(float sourceAngle, float otherAngle)
    {
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



    public class Position {
        public float x;
        public float y;


        public Position(float x, float y){
            this.x = x;
            this.y = y;
        }

        public String toString(){
            return x+","+y;
        }


        /**
         * Defined in order to use it as keys in a hashmap
         */
        public boolean equals(Object c){
            if(c == this){
                return true;
            }
            if(!(c instanceof Position)){
                return false;
            }
            Position pos = (Position) c;
            return (pos.x == this.x) && (pos.y == this.y);
        }

        public int hashCode(){
            return Objects.hash(x,y);
        }
    }


}
