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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * FanOutQueueImpl
 */
public class FanOutQueueImpl implements FanOutQueue {
	private BigArray array;
	private final Lock lock = new ReentrantLock();
	private String dir;
	private final Map<String, ConsumerGroup> consumerGroupMap;

	public FanOutQueueImpl(String name, String dir) throws IOException {
		if (!dir.endsWith(File.separator)) {
			dir += File.separator;
		}
		// append array name as part of the directory
		dir = dir + name + File.separator;

		// validate directory
		if (!Util.isFilenameValid(dir)) {
			throw new IllegalArgumentException("invalid array directory : " + dir);
		}

		this.consumerGroupMap = new HashMap<>();
		this.dir = dir;
		this.array = new BigArray(this.dir, 128 * 1024 * 1024);
	}

	public ConsumerGroup getOrCreateConsumerGroup(String consumerGroup) throws IOException {
		try {
			lock.lock();
			ConsumerGroup cg = this.consumerGroupMap.get(consumerGroup);
			if (cg == null) { // not in cache, need to create one
				cg = new ConsumerGroup(consumerGroup, this.dir, this.array);
				this.consumerGroupMap.put(consumerGroup, cg);
			}

			return cg;
		} finally {
			lock.unlock();
		}
	}

	public long nextAppendIndex() throws IOException {
		return this.array.nextAppendIndex();
	}

	public void put(long appendIndex, byte[] data) throws IOException {
		this.array.append(appendIndex, data);
	}

	public byte[] get(long index) throws IOException {
		return this.array.get(index);
	}

	@Override
	public void flush() {
		try {
			this.array.arrayReadLock.lock();

			for (ConsumerGroup cg : this.consumerGroupMap.values()) {
				try {
					cg.writeLock.lock();
					cg.indexPageFactory.flush();
				} finally {
					cg.writeLock.unlock();
				}
			}
			this.array.flush();
		} finally {
			this.array.arrayReadLock.unlock();
		}
	}

	@Override
	public void close() throws IOException {
		try {
			this.array.arrayWriteLock.lock();

			for (ConsumerGroup cg : this.consumerGroupMap.values()) {
				cg.indexPageFactory.releaseCachedPages();
			}

			this.array.close();
		} finally {
			this.array.arrayWriteLock.unlock();
		}
	}

}
