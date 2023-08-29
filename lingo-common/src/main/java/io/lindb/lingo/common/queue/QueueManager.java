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
