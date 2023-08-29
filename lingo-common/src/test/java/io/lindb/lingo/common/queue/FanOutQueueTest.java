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

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class FanOutQueueTest {

	@Test
	public void consumeGroup() throws Exception {
		log.info("run test....");
		QueueManager mgr = new QueueManager("." + File.separator + "queue_test");
		FanOutQueue queue = mgr.getOrCreate("test");
		AtomicBoolean running = new AtomicBoolean(true);
		ConsumerGroup cg1 = queue.getOrCreateConsumerGroup("cg1");
		ConsumerGroup cg2 = queue.getOrCreateConsumerGroup("cg2");
		Thread consumer1 = new Thread(() -> {
			while (running.get()) {
				try {
					long msg = cg1.consume();
					log.info("consumer group1: {}", msg);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		consumer1.start();
		Thread consumer2 = new Thread(() -> {
			while (running.get()) {
				try {
					long msg = cg2.consume();
					log.info("consumer group2: {}", msg);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		consumer2.start();

		Thread producer = new Thread(() -> {
			while (running.get()) {
				try {
					Thread.sleep(1000);
					long id = queue.nextAppendIndex();
					queue.put(id, "data".getBytes());
					log.info("produce msg");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		producer.start();

		Thread.sleep(10000);
		running.set(false);
	}
}
