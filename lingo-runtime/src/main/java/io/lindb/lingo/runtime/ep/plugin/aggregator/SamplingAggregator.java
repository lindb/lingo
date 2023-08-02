package io.lindb.lingo.runtime.ep.plugin.aggregator;

import com.espertech.esper.common.client.hook.aggfunc.AggregationFunctionForge;
import com.espertech.esper.common.client.hook.aggfunc.AggregationFunctionMode;
import com.espertech.esper.common.client.hook.aggfunc.AggregationFunctionModeMultiParam;
import com.espertech.esper.common.client.hook.aggfunc.AggregationFunctionValidationContext;
import com.espertech.esper.common.client.hook.aggfunc.ExtensionAggregationFunction;
import com.espertech.esper.common.client.hook.forgeinject.InjectionStrategy;
import com.espertech.esper.common.client.hook.forgeinject.InjectionStrategyClassNewInstance;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.epl.expression.core.ExprValidationException;
import com.espertech.esper.common.internal.util.JavaClassHelper;

import io.lindb.lingo.common.model.Pair;

@ExtensionAggregationFunction(name = "sampling")
public class SamplingAggregator implements AggregationFunctionForge {

	@Override
	public void validate(AggregationFunctionValidationContext ctx) throws ExprValidationException {
		if (ctx.getParameterTypes().length <= 1
				|| !JavaClassHelper.isTypeString(ctx.getParameterTypes()[0])) {
			throw new IllegalArgumentException("Sampling aggregation requires first parameter need string type");
		}
	}

	@Override
	public EPTypeClass getValueType() {
		return new EPTypeClass(Pair.class);
	}

	@Override
	public AggregationFunctionMode getAggregationFunctionMode() {
		// Inject a factory by using "new"
		InjectionStrategy injectionStrategy = new InjectionStrategyClassNewInstance(
				SamplingAggregatorFactory.class);
		// The managed mode means there is no need to write code that generates code
		AggregationFunctionModeMultiParam mode = new AggregationFunctionModeMultiParam();
		mode.setInjectionStrategyAggregationFunctionFactory(injectionStrategy);
		return mode;
	}

}
