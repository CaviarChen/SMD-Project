package mycontroller.pipeline;

import java.util.ArrayList;

/**
 * Class for Pipeline pattern
 *
 * @param <T> input and output type
 * @param <D> data type
 */
public class Pipeline<T, D> {

    private ArrayList<Step<T, D>> steps;

    /**
     * Constructor
     */
    public Pipeline() {
        steps = new ArrayList<>();
    }

    /**
     * Add a step into the pipeline
     *
     * @param step the step object
     */
    public void appendStep(Step<T, D> step) {
        steps.add(step);
    }

    /**
     * Execute the pipeline
     *
     * @param input the input data
     * @param data  the extra data
     * @return the output data from the pipeline
     */
    public T execute(T input, D data) {
        for (Step<T, D> step : steps) {
            input = step.execute(input, data);
        }

        return input;
    }

    /**
     * Interface for a step in the pipeline
     *
     * @param <T> input and output type
     * @param <D> data type
     */
    public interface Step<T, D> {
        /**
         * Execute the step
         *
         * @param input the input data
         * @param data  the extra data
         * @return the output data
         */
        T execute(T input, D data);
    }
}
