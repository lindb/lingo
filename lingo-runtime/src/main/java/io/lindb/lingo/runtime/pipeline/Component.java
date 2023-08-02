package io.lindb.lingo.runtime.pipeline;

import io.lindb.lingo.runtime.event.TimeTick;

/**
 * Component
 */
public interface Component {
	void ref();

	void next(Object key, Object event);

	void handle(Object key, Object event);

	void addOutput(Component output);

	void onTick(TimeTick tick);

	void shutdown();
}
