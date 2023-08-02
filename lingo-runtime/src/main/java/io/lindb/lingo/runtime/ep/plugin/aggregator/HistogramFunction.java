package io.lindb.lingo.runtime.ep.plugin.aggregator;

import com.espertech.esper.common.client.hook.aggfunc.AggregationFunction;

import io.lindb.lingo.common.model.Histogram;
import io.lindb.lingo.runtime.utils.ObjectUtil;

// Ref prometheus histogram: https://github.com/prometheus/client_java/blob/a76a65ddb62b50eb2c33a46e98cde4f4479a54a6/simpleclient/src/main/java/io/prometheus/client/Histogram.java#L72
public class HistogramFunction implements AggregationFunction {
	private final static double[] defaultBuckets = new double[] { .005, .01, .025, .05, .075, .1, .25, .5, .75, 1, 2.5,
			5, 7.5,
			10 };
	private double[] upperBounds;// buckets;
	private long[] cumulativeCounts; // length == upperBounds's length
	private long sum;
	private long total;
	private long min = Long.MAX_VALUE;
	private long max = Long.MIN_VALUE;

	@Override
	public void enter(Object value) {
		if (!(value instanceof Object[])) {
			return;
		}

		Object[] values = (Object[]) value;
		if (!(values[0] instanceof Long)) {
			return;
		}

		if (this.upperBounds == null) {
			if (values.length > 1 && values[1] instanceof double[]) {
				this.upperBounds = (double[]) values[1];
			} else {
				// default upper bounds
				this.upperBounds = defaultBuckets;
			}
			// init bucket cumulative counts
			this.cumulativeCounts = new long[this.upperBounds.length];
		}

		// do histogram agg
		long amt = ObjectUtil.toLong(values[0]);
		for (int i = 0; i < this.upperBounds.length; ++i) {
			// The last bucket is +Inf, so we always increment.
			if (amt <= this.upperBounds[i]) {
				cumulativeCounts[i] += 1;
				break;
			}
		}
		this.sum += amt;
		this.total++;
		if (amt < this.min) {
			this.min = amt;
		} else if (amt > this.max) {
			this.max = amt;
		}
	}

	@Override
	public void leave(Object value) {
		this.clear();
	}

	@Override
	public Object getValue() {
		Histogram histogram = new Histogram();
		histogram.setUpperBounds(this.upperBounds);
		histogram.setCumulativeCounts(this.cumulativeCounts);
		histogram.setSum(this.sum);
		histogram.setTotal(this.total);
		histogram.setMin(this.min);
		histogram.setMax(this.max);
		return histogram;
	}

	@Override
	public void clear() {
		this.sum = 0;
		this.total = 0;
		this.min = Long.MAX_VALUE;
		this.max = Long.MIN_VALUE;
	}

}
