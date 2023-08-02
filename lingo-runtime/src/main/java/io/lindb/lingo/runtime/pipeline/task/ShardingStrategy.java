package io.lindb.lingo.runtime.pipeline.task;

/**
 * ShardingStrategy
 */
public interface ShardingStrategy {

	int chooseTasks(Object key);

}
