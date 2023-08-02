package io.lindb.lingo.runtime.ep.plugin.aggregator;

import com.espertech.esper.common.client.hook.aggfunc.AggregationFunctionForge;
import com.espertech.esper.common.client.hook.aggfunc.AggregationFunctionMode;
import com.espertech.esper.common.client.hook.aggfunc.AggregationFunctionModeMultiParam;
import com.espertech.esper.common.client.hook.aggfunc.AggregationFunctionValidationContext;
import com.espertech.esper.common.client.hook.forgeinject.InjectionStrategy;
import com.espertech.esper.common.client.hook.forgeinject.InjectionStrategyClassNewInstance;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.epl.expression.core.ExprValidationException;
import com.espertech.esper.common.internal.util.JavaClassHelper;

import io.lindb.lingo.common.model.Histogram;

public class HistogramAggregator implements AggregationFunctionForge {

	@Override
	public void validate(AggregationFunctionValidationContext ctx) throws ExprValidationException {
		if (ctx.getParameterTypes().length <= 1
				|| !JavaClassHelper.isTypeLong(ctx.getParameterTypes()[0])) {
			throw new IllegalArgumentException("Histogram aggregation requires first parameter need long type");
		}
	}

	@Override
	public EPTypeClass getValueType() {
		return new EPTypeClass(Histogram.class);
	}

	@Override
	public AggregationFunctionMode getAggregationFunctionMode() {
		InjectionStrategy injectionStrategy = new InjectionStrategyClassNewInstance(
				HistogramAggregatorFactory.class);
		AggregationFunctionModeMultiParam mode = new AggregationFunctionModeMultiParam();
		mode.setInjectionStrategyAggregationFunctionFactory(injectionStrategy);
		return mode;
	}

}
