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
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ConsumerGroup
 */
public class ConsumerGroup {
	// only use the first page
	private static final long CONSUMER_GROUP_PAGE_INDEX = 0;
	// 2 ^ 3 = 8
	private final static int CONSUMER_GROUP_INDEX_ITEM_LENGTH_BITS = 3;
	// size in bytes of consumer group index page
	private final static int CONSUMER_GROUP_INDEX_PAGE_SIZE = 1 << CONSUMER_GROUP_INDEX_ITEM_LENGTH_BITS;
	// folder name prefix for consumer group index page
	private final static String CONSUMER_GROUP_INDEX_PAGE_FOLDER_PREFIX = "cg_index_";

	// consume group identifier
	final String consumerGroup;
	// front index of the fanout queue
	final AtomicLong index = new AtomicLong();
	// factory for queue front index page management(acquire, release, cache)
	final MappedPageFactory indexPageFactory;
	// lock for queue front write management
	final Lock writeLock = new ReentrantLock();
	final BigArray innerArray;

	ConsumerGroup(String consumerGroup, String dir, BigArray array) throws IOException {
		try {
			Util.validate(consumerGroup);
		} catch (IllegalArgumentException ex) {
			throw new IllegalArgumentException("invalid consume group identifier", ex);
		}
		this.consumerGroup = consumerGroup;
		this.innerArray = array;
		// the ttl does not matter here since queue front index page is always cached
		this.indexPageFactory = new MappedPageFactory(CONSUMER_GROUP_INDEX_PAGE_SIZE,
				dir + CONSUMER_GROUP_INDEX_PAGE_FOLDER_PREFIX + consumerGroup);

		MappedPage indexPage = this.indexPageFactory.acquirePage(CONSUMER_GROUP_PAGE_INDEX);

		ByteBuffer indexBuffer = indexPage.getLocal(0);
		index.set(indexBuffer.getLong());
		validateAndAdjustIndex();
	}

	public long consume() throws Exception {
		try {
			this.writeLock.lock();
			long msgId = this.index.get();
			this.innerArray.checkIfEmpty(msgId);
			this.incrementIndex();
			return msgId;
		} catch (IndexOutOfBoundsException ex) {
			// FIXME:
			ex.printStackTrace();
			this.resetIndex(); // maybe the back array has been truncated to limit size

			long msgId = this.index.get();
			this.incrementIndex();

			return msgId;

		} finally {
			this.writeLock.unlock();
		}

	}

	void validateAndAdjustIndex() throws IOException {
		if (index.get() != innerArray.arrayHeadIndex.get()) { // ok that index is equal to array head index
			try {
				innerArray.validateIndex(index.get());
			} catch (IndexOutOfBoundsException ex) { // maybe the back array has been truncated to limit size
				resetIndex();
			}
		}
	}

	// reset queue front index to the tail of array
	void resetIndex() throws IOException {
		index.set(innerArray.arrayTailIndex.get());

		this.persistIndex();
	}

	void incrementIndex() throws IOException {
		long nextIndex = index.get();
		if (nextIndex == Long.MAX_VALUE) {
			nextIndex = 0L; // wrap
		} else {
			nextIndex++;
		}
		index.set(nextIndex);

		this.persistIndex();
	}

	void persistIndex() throws IOException {
		// persist index
		MappedPage indexPage = this.indexPageFactory.acquirePage(CONSUMER_GROUP_PAGE_INDEX);
		ByteBuffer indexBuffer = indexPage.getLocal(0);
		indexBuffer.putLong(index.get());
		indexPage.setDirty(true);
	}
}
