package io.lindb.lingo.runtime.ep.plugin.aggregator;

import com.espertech.esper.common.client.hook.aggfunc.AggregationFunction;
import com.espertech.esper.common.client.hook.aggfunc.AggregationFunctionFactory;
import com.espertech.esper.common.client.hook.aggfunc.AggregationFunctionFactoryContext;

public class HistogramAggregatorFactory implements AggregationFunctionFactory {

	@Override
	public AggregationFunction newAggregator(AggregationFunctionFactoryContext ctx) {
		return new HistogramFunction();
	}

}
