/**
 * Licensed to LinDB under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. LinDB licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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
