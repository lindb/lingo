package io.lindb.lingo.common.queue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * QueueManager
 */
public class QueueManager {
	private String dir;
	private Map<String, FanOutQueue> queues;
	private Lock lock = new ReentrantLock();

	public QueueManager(String dir) {
		this.dir = dir;
		this.queues = new HashMap<>();
	}

	public FanOutQueue getOrCreate(String queueName) throws IOException {
		lock.lock();
		try {
			FanOutQueue queue = this.queues.get(queueName);
			if (queue == null) {
				queue = new FanOutQueueImpl(queueName, this.dir);
				this.queues.put(queueName, queue);
			}
			return queue;
		} finally {
			lock.unlock();
		}
	}
}
