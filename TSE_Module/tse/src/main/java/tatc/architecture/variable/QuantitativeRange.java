package tatc.architecture.variable;

import java.util.ArrayList;
import java.util.List;

public class QuantitativeRange {
    private double min;
    private double max;
    private double step;

    public QuantitativeRange(double min, double max, double step) {
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public List<Double> discretize() {
        List<Double> values = new ArrayList<>();
        for (double val = min; val <= max; val += step) {
            values.add(val);
        }
        return values;
    }
}
