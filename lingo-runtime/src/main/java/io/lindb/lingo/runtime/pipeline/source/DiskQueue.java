package io.lindb.lingo.runtime.pipeline.source;

import io.lindb.lingo.runtime.pipeline.ComponentType;
import io.lindb.lingo.runtime.pipeline.ParallelComponent;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.Setter;
import io.lindb.lingo.common.queue.ConsumerGroup;
import io.lindb.lingo.common.queue.FanOutQueue;
import io.lindb.lingo.common.queue.QueueManager;
import io.lindb.lingo.runtime.pipeline.task.Handle;

/**
 * DiskQueueSource
 */
@ComponentType(type = "disk_queue")
public class DiskQueue extends ParallelComponent implements Source {
	@Autowired
	@Setter
	private QueueManager queueManager;
	@Setter
	private String diskQueue;
	private FanOutQueue queue;
	private ConsumerGroup consumerGroup;
	private volatile boolean running;

	private Thread[] workers;

	@Override
	public void startup() throws Exception {
		if (this.parallelism <= 0) {
			this.parallelism = 1;
		}

		this.running = true;
		this.queue = this.queueManager.getOrCreate(this.diskQueue);
		this.consumerGroup = this.queue.getOrCreateConsumerGroup(this.name);

		this.workers = new Thread[this.parallelism];
		for (int i = 0; i < workers.length; i++) {
			workers[i] = new Thread(() -> {
				while (this.running) {
					try {
						long msgId = consumerGroup.consume();
						byte[] data = this.queue.get(msgId);
						if (data == null) {
							continue;
						}
						this.next(msgId, data);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			workers[i].start();
		}
	}

	@Override
	public Handle newHandler() {
		throw new RuntimeException("Not support");
	}

}
