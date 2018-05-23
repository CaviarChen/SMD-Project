package mycontroller;

import java.util.ArrayList;

public class Pipeline<T> {

    ArrayList<Step<T>> steps;

    public Pipeline() {
        steps = new ArrayList<>();
    }

    public void appendStep(Step<T> step) {
        steps.add(step);
    }

    public T execute(T input) {
        for(Step<T> step: steps) {
            input = step.execute(input);
        }

        return input;
    }


    public interface Step<T> {
        T execute(T input);
    }
}
