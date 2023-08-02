package io.lindb.lingo.common.model;

import lombok.Data;

@Data
public class Histogram {
	private double[] upperBounds;// buckets;
	private long[] cumulativeCounts; // length == upperBounds's length
	private long sum;
	private long total;
	private long min;
	private long max;
}
