package org.diagnoseit.traceservices.aggregated;

public class NumericStatistics<T extends Number> {
	private double mean = 0.0;
	private double min = Double.MAX_VALUE;
	private double max = Double.MIN_VALUE;
	private int count = 0;
	private Class<T> type;

	/**
	 * Should not be used!
	 */
	public NumericStatistics() {

	}

	public NumericStatistics(Class<T> type) {
		this.type = type;
	}

	public NumericStatistics(T newValue) {
		addValue(newValue);
	}

	public void mergeStatistics(NumericStatistics<T> other) {
		double sumCount = ((double) count) + ((double) other.count);
		double thisProportion = ((double) count) / sumCount;
		double otherProportion = ((double) other.count) / sumCount;
		mean = mean * thisProportion + other.mean * otherProportion;
		if (other.min < min) {
			min = other.min;
		}
		if (other.max > max) {
			max = other.max;
		}
		count = (int) sumCount;
	}

	public void addValue(T newValue) {
		double nValue = newValue.doubleValue();
		double dCount = count;
		double oldSum = dCount * mean;
		dCount += 1.0;
		mean = (oldSum + nValue) / dCount;
		if (nValue < min) {
			min = nValue;
		}
		if (nValue > max) {
			max = nValue;
		}
		count = (int) dCount;
	}

	/**
	 * @return the mean
	 */
	public T getMean() {
		return getValue(mean);
	}

	/**
	 * @return the min
	 */
	public T getMin() {
		return getValue(min);
	}

	/**
	 * @return the max
	 */
	public T getMax() {
		return getValue(max);
	}

	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * @return the max
	 */
	public T getSum() {
		return getValue(mean * count);
	}

	@SuppressWarnings("unchecked")
	private T getValue(double value) {
		if (type.isAssignableFrom(Short.class)) {
			return (T) new Short((short) value);
		} else if (type.isAssignableFrom(Integer.class)) {
			return (T) new Integer((int) value);
		} else if (type.isAssignableFrom(Long.class)) {
			return (T) new Long((long) value);
		} else if (type.isAssignableFrom(Float.class)) {
			return (T) new Float((float) value);
		} else if (type.isAssignableFrom(Double.class)) {
			return (T) new Double(value);
		} else if (type.isAssignableFrom(Byte.class)) {
			return (T) new Byte((byte) value);
		} else {
			throw new IllegalArgumentException("Unsupported Type!");
		}
	}

	public boolean isEmpty() {
		return count < 0.5;
	}

}
