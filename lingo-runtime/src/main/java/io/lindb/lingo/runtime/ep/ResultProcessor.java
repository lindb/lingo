package io.lindb.lingo.runtime.ep;

import com.espertech.esper.common.internal.event.map.MapEventBean;

/**
 * ResultProcessor
 */
public interface ResultProcessor<T> {
	T process(MapEventBean event);
}
