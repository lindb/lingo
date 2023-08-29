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
