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

import com.espertech.esper.common.client.hook.aggfunc.AggregationFunction;

import io.lindb.lingo.common.model.AggregateType;
import io.lindb.lingo.common.model.Pair;
import io.lindb.lingo.runtime.utils.ObjectUtil;

public class SamplingFuncation implements AggregationFunction {
	private String traceId;
	private long timestamp;
	private long value;
	private AggregateType aggType;

	@Override
	public void enter(Object value) {
		if (!(value instanceof Object[])) {
			return;
		}

		Object[] values = (Object[]) value;
		if (!(values[0] instanceof String)) {
			return;
		}
		if (this.aggType == null) {
			String typeStr = (String) values[0];
			this.aggType = AggregateType.parseType(typeStr);
			// reset aggregate current value
			if (this.aggType == AggregateType.Min) {
				this.value = Long.MAX_VALUE;
			} else if (this.aggType == AggregateType.Max) {
				this.value = Long.MIN_VALUE;
			}
		}

		this.sampling(values);
	}

	@Override
	public void leave(Object value) {
		this.clear();
	}

	@Override
	public Object getValue() {
		return new Pair<>(this.aggType, this.traceId);
	}

	@Override
	public void clear() {
		this.traceId = null;
		this.aggType = null;
		this.timestamp = 0;
		this.value = 0;
	}

	private void sampling(Object[] values) {
		switch (this.aggType) {
			case Sum:
				// smapling("sum",traceId)
				if (this.traceId == null && values[1] instanceof String) {
					this.traceId = (String) values[1];
				}
				break;
			case Min:
				// smapling("min",val,traceId)
				if (values[2] instanceof String) {
					long val = ObjectUtil.toLong(values[1]);
					if (val < this.value) {
						this.value = val;
						this.traceId = (String) values[2];
					}
				}
				break;
			case Max:
				// smapling("max",val,traceId)
				if (values[2] instanceof String) {
					long val = ObjectUtil.toLong(values[1]);
					if (val > this.value) {
						this.value = val;
						this.traceId = (String) values[2];
					}
				}
				break;
			case Last:
				// smapling("last",timestamp,traceId)
				if (values.length == 3 && values[2] instanceof String) {
					long newTimestamp = ObjectUtil.toLong(values[1]);
					if (newTimestamp >= timestamp) {
						this.timestamp = newTimestamp;
						this.traceId = (String) values[2];
					}
				}
				break;
			case First:
				// smapling("first",timestamp,traceId)
				if (values.length == 3 && values[2] instanceof String) {
					long newTimestamp = ObjectUtil.toLong(values[1]);
					if (newTimestamp <= timestamp) {
						this.timestamp = newTimestamp;
						this.traceId = (String) values[2];
					}
				}
				break;
			default:
				throw new IllegalStateException("Unexpected aggregate type: " + this.aggType);
		}
	}
}
