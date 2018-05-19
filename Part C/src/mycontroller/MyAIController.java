package mycontroller;

import controller.CarController;
import utilities.Coordinate;
import world.Car;


import java.util.ArrayList;
import java.util.Objects;


public class MyAIController extends CarController{

    private static final float BRAKING_FORCE = 2f;
    private static final float ACCELERATION = 2f;
    private static final float FRICTION_FORCE = 0.5f;
    private static final int[][] DIRECTS = new int[][]{{1,0},{-1,0},{0,1},{0,-1}};

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

        AStar aStar = new AStar(mapRecorder, 2, 2, 21, 12);
	    ArrayList<Node> path = aStar.start(mapRecorder.mapStatus);
	    for (Node n:path) {
            targetPositions.add(avoidWall(n.coord.x, n.coord.y));
        }


        System.out.println('a');
	}


	private Position avoidWall(int x, int y) {

	    float posX = x;
	    float posY = y;

	    MapRecorder.TileStatus[][] mapStatus = mapRecorder.getTileStatus();

	    for(int[] dir: DIRECTS) {
	        int newX = x + dir[0];
            int newY = y + dir[1];

            if(mapRecorder.inRange(newX, newY)) {
                if (mapStatus[newX][newY] == MapRecorder.TileStatus.UNREACHABLE) {
                    posX -= 0.4 * dir[0];
                    posY -= 0.4 * dir[1];
                }
            }
        }

        return new Position(posX, posY);

    }

	@Override
	public void update(float delta) {

        mapRecorder.addCarView(Math.round(getX()), Math.round(getY()), getView());

        System.out.print("X: ");
        System.out.println(getX());
        System.out.print("Y: ");
        System.out.println(getY());

        if (targetPositions.size()!=0) {


            int currentX = Math.round(getX());
            int currentY = Math.round(getY());

            float targetX = targetPositions.get(0).x;
            float targetY = targetPositions.get(0).y;

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
                if(cmp<0) {
                    turnRight(delta);
                } else {
                    turnLeft(delta);
                }
            }

            float endingSpeed = 0f;

            System.out.print("dist: ");
            System.out.println(dist);


            float allowedSpeed = 0f;
            if (Math.abs(cmp) < 100) {
                allowedSpeed = computeAllowedVelocity(dist, endingSpeed);
            } else {
                // big turn
                allowedSpeed = 0.4f;
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
