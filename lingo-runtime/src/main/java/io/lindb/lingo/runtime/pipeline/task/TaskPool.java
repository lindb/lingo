package io.lindb.lingo.runtime.pipeline.task;

/**
 * TaskPool
 */
public class TaskPool {
	private final int taskSize;
	private final ShardingStrategy shardingStrategy;
	private Task[] tasks;

	public TaskPool(int taskSize, ShardingStrategy shardingStrategy) {
		this.taskSize = taskSize;
		this.shardingStrategy = shardingStrategy;
		this.tasks = new Task[this.taskSize];
	}

	public void addTask(int index, Task task) {
		this.tasks[index] = task;
	}

	public void handleEvent(Object key, Object event) {
		if (this.taskSize == 0) {
			return;
		}
		int target = this.shardingStrategy.chooseTasks(key);
		this.tasks[target].handleEvent(key, event);
	}

}
