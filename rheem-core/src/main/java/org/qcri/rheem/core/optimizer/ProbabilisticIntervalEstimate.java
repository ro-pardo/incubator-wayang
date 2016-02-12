package org.qcri.rheem.core.optimizer;

import org.apache.commons.lang3.Validate;

import java.util.Objects;

/**
 * An estimate that is capable of expressing uncertainty.
 * <p>The estimate addresses uncertainty in the estimation process by
 * <ol><li>expressing estimates as intervals</li>
 * <li>and assigning a probability of correctness (in [0, 1]).</li></ol></p>
 */
public class ProbabilisticIntervalEstimate {

    /**
     * Probability of correctness between in the interval [0, 1]. This helps
     * Rheem in situations with many estimates to pick the best one.
     */
    private final double correctnessProb;

    /**
     * Lower and upper estimate. Not that this is not a bounding box, i.e., there is no guarantee that the finally
     * observed value will be within the estimated interval.
     */
    private final long lowerEstimate, upperEstimate;

    public ProbabilisticIntervalEstimate(long lowerEstimate, long upperEstimate, double correctnessProb) {
        Validate.isTrue(lowerEstimate <= upperEstimate);
        Validate.inclusiveBetween(0, 1, correctnessProb);

        this.correctnessProb = correctnessProb;
        this.lowerEstimate = lowerEstimate;
        this.upperEstimate = upperEstimate;
    }

    public long getLowerEstimate() {
        return this.lowerEstimate;
    }

    public long getUpperEstimate() {
        return this.upperEstimate;
    }

    public long getAverageEstimate() {
        return (this.getUpperEstimate() - this.getLowerEstimate()) / 2;
    }

    public double getCorrectnessProbability() {
        return this.correctnessProb;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        ProbabilisticIntervalEstimate estimate = (ProbabilisticIntervalEstimate) o;
        return Double.compare(estimate.correctnessProb, this.correctnessProb) == 0 &&
                this.lowerEstimate == estimate.lowerEstimate &&
                this.upperEstimate == estimate.upperEstimate;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.correctnessProb, this.lowerEstimate, this.upperEstimate);
    }

    @Override
    public String toString() {
        return String.format("%s[%d..%d, %.1f%%]", this.getClass().getSimpleName(),
                this.lowerEstimate, this.upperEstimate, this.correctnessProb * 100d);
    }
}
