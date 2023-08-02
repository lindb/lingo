package io.lindb.lingo.runtime.pipeline.task;

/**
 * Handle
 */
public interface Handle {
	void handleEvent(Object key, Object event);
}
