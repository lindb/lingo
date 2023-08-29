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
package io.lindb.lingo.runtime.ep;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.internal.event.map.MapEventBean;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPStatement;
import com.espertech.esper.runtime.client.UpdateListener;

/**
 * AggregatorEventListener
 */
public class AggregatorEventListener implements UpdateListener {
	private final Output output;

	public AggregatorEventListener(Output output) {
		this.output = output;
	}

	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents, EPStatement statement, EPRuntime runtime) {
		if (newEvents == null) {
			return;
		}
		List<ResultProcessor<?>> processors = getResultProcessors(statement);

		List<Object> result = new ArrayList<>();
		for (EventBean newEvent : newEvents) {
			if (newEvent == null) {
				continue;
			}
			if (newEvent instanceof MapEventBean) {
				MapEventBean event = (MapEventBean) newEvent;

				if (processors != null && processors.size() > 0) {
					for (ResultProcessor<?> processor : processors) {
						result.add(processor.process(event));
					}
				}
			}
		}
		this.output.update(result);
	}

	@SuppressWarnings("unchecked")
	private List<ResultProcessor<?>> getResultProcessors(EPStatement statement) {
		Object userObjectRuntime = statement.getUserObjectRuntime();
		if (userObjectRuntime == null) {
			return null;
		}
		if (!(userObjectRuntime instanceof Map)) {
			return null;
		}

		Map<String, Object> userObject = (Map<String, Object>) userObjectRuntime;
		List<ResultProcessor<?>> processors = (List<ResultProcessor<?>>) userObject.get(Engine.RESULT_PROCESSORS);
		return processors;
	}
}
