package mycontroller;

import java.util.ArrayList;
import java.util.Objects;

public class SimplifyPath implements Pipeline.Step<ArrayList<Position>, MapRecorder> {

    private static final float PRECISION_LEVEL = 0.001f;


    @Override
    public ArrayList<Position> execute(ArrayList<Position> input, MapRecorder mapRecorder) {

        for (int i=0; i<input.size()-2; i++) {
            if (input.get(i)==null) continue;;

            for (int j=i+2; j<input.size(); j++) {
                Position pos1 = input.get(i);
                Position pos2 = input.get(j);

                if (floatEquals(pos1.x, pos2.x) || floatEquals(pos1.y, pos2.y)) {
                    input.set(j-1, null);
                } else {
                    break;
                }
            }
        }

        // clean up
        input.removeIf(Objects::isNull);

        return input;
    }


    private boolean floatEquals(float a, float b) {
        return Math.abs(a-b) < PRECISION_LEVEL;
    }

}
