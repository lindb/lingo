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
