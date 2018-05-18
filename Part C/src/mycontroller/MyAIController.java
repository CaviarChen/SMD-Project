package mycontroller;

import controller.CarController;
import org.lwjgl.Sys;
import utilities.Coordinate;
import world.Car;

import java.util.ArrayList;



public class MyAIController extends CarController{

    private static final float BRAKING_FORCE = 2f;
    private static final float ACCELERATION = 2f;
    private static final float FRICTION_FORCE = 0.5f;

//    String[] test_pos = new String[]{"3,3", "7,3", "7,11"};
    String[] test_pos = new String[]{"26,2", "21,12", "2,12", "2,2", "26,2", "21,12", "2,12", "2,2"};

    ArrayList<Coordinate> target_coordinates = new ArrayList<>();

	public MyAIController(Car car) {

        super(car);

	    for(String s: test_pos) {
	        target_coordinates.add(new Coordinate(s));
        }

        System.out.println('a');
	}

	@Override
	public void update(float delta) {
        System.out.print("X: ");
        System.out.println(getX());
        System.out.print("Y: ");
        System.out.println(getY());

        if (target_coordinates.size()!=0) {


            int currentX = Math.round(getX());
            int currentY = Math.round(getY());

            if (target_coordinates.get(0).x==currentX && target_coordinates.get(0).y==currentY) {
                target_coordinates.remove(0);
                return;
            }


            float targetAngle = getTargetAngle(target_coordinates.get(0));
            float dist = getTargetDistance(target_coordinates.get(0));
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

            float endingSpeed = 0.5f;

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



    private float getTargetDistance(Coordinate target) {
        float target_x = target.x;// + 0.5f;
        float target_y = target.y;// + 0.5f;

        return (float) Math.sqrt(Math.pow(target_x - getX(),2)+Math.pow(target_y - getY(),2));
    }


    private float getTargetAngle(Coordinate target) {
	    float target_x = target.x;// + 0.5f;
        float target_y = target.y;// + 0.5f;
        float angle = (float) Math.toDegrees(Math.atan2(target_y - getY(), target_x - getX()));

        if(angle < 0){
            angle += 360;
        }
        return angle;
    }


}
