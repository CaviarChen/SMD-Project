package mycontroller;

import java.util.ArrayList;

public class Pipeline<T, D> {

    ArrayList<Step<T, D>> steps;

    public Pipeline() {
        steps = new ArrayList<>();
    }

    public void appendStep(Step<T, D> step) {
        steps.add(step);
    }

    public T execute(T input, D data) {
        for(Step<T, D> step: steps) {
            input = step.execute(input, data);
        }

        return input;
    }


    public interface Step<T, D> {
        T execute(T input, D data);
    }
}
