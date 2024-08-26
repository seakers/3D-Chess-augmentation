/* Copyright 2009-2016 David Hadka
 *
 * This file is part of the MOEA Framework.
 *
 * The MOEA Framework is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * The MOEA Framework is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the MOEA Framework.  If not, see <http://www.gnu.org/licenses/>.
 */
package tatc.architecture.variable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Variable;

import java.text.MessageFormat;

/**
 * Decision variable for real values.
 */
public class RealVariable implements Variable {
    private static final long serialVersionUID = 3141851312155686224L;
    private static final String VALUE_OUT_OF_BOUNDS = "value out of bounds (value: {0}, min: {1}, max: {2})";
    /**
     * The current value of this decision variable.
     */
    private double value;
    /**
     * The lower bound of this decision variable.
     */
    private final double lowerBound;

    /**
     * The upper bound of this decision variable.
     */
    private final double upperBound;

    public RealVariable(double lowerBound, double upperBound) {
        this(0.0D / 0.0, lowerBound, upperBound);
    }

    /**
     * Constructs a real variable in the range {@code lowerBound <= x <=
     * upperBound} with the specified initial value.
     *
     * @param value the initial value of this decision variable
     * @param lowerBound the lower bound of this decision variable, inclusive
     * @param upperBound the upper bound of this decision variable, inclusive
     * @throws IllegalArgumentException if the value is out of bounds
     *         {@code (value < lowerBound) || (value > upperBound)}
     */
    public RealVariable(double value, double lowerBound, double upperBound) {
        this.value = value;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        if (value < lowerBound || value > upperBound) {
            throw new IllegalArgumentException(MessageFormat.format("value out of bounds (value: {0}, min: {1}, max: {2})", value, lowerBound, upperBound));
        }
    }

    /**
     * Returns the current value of this decision variable.
     *
     * @return the current value of this decision variable
     */
    public double getValue() {
        return this.value;
    }

    /**
     * Sets the value of this decision variable.
     *
     * @param value the new value for this decision variable
     * @throws IllegalArgumentException if the value is out of bounds
     *         {@code (value < getLowerBound()) || (value > getUpperBound())}
     */
    public void setValue(double value) {
        if (value >= this.lowerBound && value <= this.upperBound) {
            this.value = value;
        } else {
            throw new IllegalArgumentException(MessageFormat.format("value out of bounds (value: {0}, min: {1}, max: {2})", value, this.lowerBound, this.upperBound));
        }
    }

    /**
     * Returns the lower bound of this decision variable.
     *
     * @return the lower bound of this decision variable, inclusive
     */
    public double getLowerBound() {
        return this.lowerBound;
    }

    /**
     * Returns the upper bound of this decision variable.
     *
     * @return the upper bound of this decision variable, inclusive
     */
    public double getUpperBound() {
        return this.upperBound;
    }

    public RealVariable copy() {
        return new RealVariable(this.value, this.lowerBound, this.upperBound);
    }

    public String toString() {
        return Double.toString(this.value);
    }

    public int hashCode() {
        return (new HashCodeBuilder()).append(this.lowerBound).append(this.upperBound).append(this.value).toHashCode();
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj != null && obj.getClass() == this.getClass()) {
            RealVariable rhs = (RealVariable)obj;
            return (new EqualsBuilder()).append(this.lowerBound, rhs.lowerBound).append(this.upperBound, rhs.upperBound).append(this.value, rhs.value).isEquals();
        } else {
            return false;
        }
    }

    public void randomize() {
        this.setValue(PRNG.nextDouble(this.lowerBound, this.upperBound));
    }
}
