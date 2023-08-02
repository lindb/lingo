package io.lindb.lingo.common.queue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * BigArray
 */
public class BigArray {
	// minimum size in bytes of a data page
	private final static int MINIMUM_DATA_PAGE_SIZE = 32 * 1024 * 1024;
	// folder name for index page
	private final static String INDEX_PAGE_FOLDER = "index";
	// folder name for data page
	private final static String DATA_PAGE_FOLDER = "data";
	// folder name for meta data page
	private final static String META_DATA_PAGE_FOLDER = "meta_data";

	// 2 ^ 4 = 16
	final static int META_DATA_ITEM_LENGTH_BITS = 4;
	// size in bytes of a meta data page
	final static int META_DATA_PAGE_SIZE = 1 << META_DATA_ITEM_LENGTH_BITS;
	// 2 ^ 17 = 1024 * 128
	final static int INDEX_ITEMS_PER_PAGE_BITS = 17; // 1024 * 128
	// number of items per page
	final static int INDEX_ITEMS_PER_PAGE = 1 << INDEX_ITEMS_PER_PAGE_BITS;
	// 2 ^ 5 = 32
	final static int INDEX_ITEM_LENGTH_BITS = 5;
	// only use the first page
	static final long META_DATA_PAGE_INDEX = 0;
	// length in bytes of an index item
	final static int INDEX_ITEM_LENGTH = 1 << INDEX_ITEM_LENGTH_BITS;
	// size in bytes of an index page
	final static int INDEX_PAGE_SIZE = INDEX_ITEM_LENGTH * INDEX_ITEMS_PER_PAGE;

	// lock for appending state management
	private final Lock appendLock = new ReentrantLock();

	// global lock for array read and write management
	private final ReadWriteLock arrayReadWritelock = new ReentrantReadWriteLock();
	final Lock arrayReadLock = arrayReadWritelock.readLock();
	final Lock arrayWriteLock = arrayReadWritelock.writeLock();
	protected final Condition notEmpty = appendLock.newCondition();
	private final String dir;

	// head index of the big array, this is the read write barrier.
	// readers can only read items before this index, and writes can write this
	// index or after
	final AtomicLong arrayHeadIndex = new AtomicLong();
	// tail index of the big array,
	// readers can't read items before this tail
	final AtomicLong arrayTailIndex = new AtomicLong();
	// size in bytes of a data page
	private final int dataPageSize;

	// head index of the data page, this is the to be appended data page index
	private long headDataPageIndex;
	// head offset of the data page, this is the to be appended data offset
	private int headDataItemOffset;
	// factory for index page management(acquire, release, cache)
	MappedPageFactory indexPageFactory;
	// factory for data page management(acquire, release, cache)
	MappedPageFactory dataPageFactory;
	// factory for meta data page management(acquire, release, cache)
	MappedPageFactory metaPageFactory;

	public BigArray(String dir, int pageSize) throws IOException {
		if (pageSize < MINIMUM_DATA_PAGE_SIZE) {
			throw new IllegalArgumentException(
					"invalid page size, allowed minimum is : " + MINIMUM_DATA_PAGE_SIZE + " bytes.");
		}
		this.dir = dir;
		this.dataPageSize = pageSize;

		this.initialize();
	}

	public long nextAppendIndex() throws IOException {
		try {
			this.appendLock.lock();

			long nextAppendIndex = this.arrayHeadIndex.get();
			this.arrayHeadIndex.incrementAndGet();

			// update meta data
			MappedPage metaDataPage = this.metaPageFactory.acquirePage(META_DATA_PAGE_INDEX);
			ByteBuffer metaDataBuf = metaDataPage.getLocal(0);
			metaDataBuf.putLong(this.arrayHeadIndex.get());
			metaDataBuf.putLong(this.arrayTailIndex.get());
			metaDataPage.setDirty(true);

			return nextAppendIndex;
		} finally {
			this.appendLock.unlock();
		}
	}

	public void append(long appendIndex, byte[] data) throws IOException {
		try {
			arrayReadLock.lock();
			MappedPage toAppendDataPage = null;
			MappedPage toAppendIndexPage = null;
			long toAppendIndexPageIndex = -1L;
			long toAppendDataPageIndex = -1L;

			try {
				appendLock.lock(); // only one thread can append

				// prepare the data pointer
				if (this.headDataItemOffset + data.length > this.dataPageSize) { // not enough space
					this.headDataPageIndex++;
					this.headDataItemOffset = 0;
				}

				toAppendDataPageIndex = this.headDataPageIndex;
				int toAppendDataItemOffset = this.headDataItemOffset;

				// append data
				toAppendDataPage = this.dataPageFactory.acquirePage(toAppendDataPageIndex);
				ByteBuffer toAppendDataPageBuffer = toAppendDataPage.getLocal(toAppendDataItemOffset);
				toAppendDataPageBuffer.put(data);
				toAppendDataPage.setDirty(true);
				// update to next
				this.headDataItemOffset += data.length;

				toAppendIndexPageIndex = Util.div(appendIndex, INDEX_ITEMS_PER_PAGE_BITS); // shift
																							// optimization
				toAppendIndexPage = this.indexPageFactory.acquirePage(toAppendIndexPageIndex);
				int toAppendIndexItemOffset = (int) (Util.mul(Util.mod(appendIndex, INDEX_ITEMS_PER_PAGE_BITS),
						INDEX_ITEM_LENGTH_BITS));

				// update index
				ByteBuffer toAppendIndexPageBuffer = toAppendIndexPage.getLocal(toAppendIndexItemOffset);
				toAppendIndexPageBuffer.putLong(toAppendDataPageIndex);
				toAppendIndexPageBuffer.putInt(toAppendDataItemOffset);
				toAppendIndexPageBuffer.putInt(data.length);
				// TODO: remove create timestamp???
				long currentTime = System.currentTimeMillis();
				toAppendIndexPageBuffer.putLong(currentTime);
				toAppendIndexPage.setDirty(true);
				this.notEmpty.signalAll();
			} finally {
				appendLock.unlock();
				if (toAppendDataPage != null) {
					this.dataPageFactory.releasePage(toAppendDataPageIndex);
				}
				if (toAppendIndexPage != null) {
					this.indexPageFactory.releasePage(toAppendIndexPageIndex);
				}
			}
		} finally {
			arrayReadLock.unlock();
		}
	}

	public void checkIfEmpty(long index) throws Exception {
		try {
			appendLock.lock();
			while (index == this.arrayHeadIndex.get()) {
				notEmpty.await();
			}
		} finally {
			appendLock.unlock();
		}
	}

	public byte[] get(long index) throws IOException {
		try {
			arrayReadLock.lock();
			validateIndex(index);

			MappedPage dataPage = null;
			long dataPageIndex = -1L;
			try {
				ByteBuffer indexItemBuffer = this.getIndexItemBuffer(index);
				dataPageIndex = indexItemBuffer.getLong();
				int dataItemOffset = indexItemBuffer.getInt();
				int dataItemLength = indexItemBuffer.getInt();
				dataPage = this.dataPageFactory.acquirePage(dataPageIndex);
				byte[] data = dataPage.getLocal(dataItemOffset, dataItemLength);
				return data;
			} finally {
				if (dataPage != null) {
					this.dataPageFactory.releasePage(dataPageIndex);
				}
			}
		} finally {
			arrayReadLock.unlock();
		}
	}

	public boolean isEmpty() {
		try {
			arrayReadLock.lock();
			return this.arrayHeadIndex.get() == this.arrayTailIndex.get();
		} finally {
			arrayReadLock.unlock();
		}
	}

	ByteBuffer getIndexItemBuffer(long index) throws IOException {
		MappedPage indexPage = null;
		long indexPageIndex = -1L;
		try {
			indexPageIndex = Util.div(index, INDEX_ITEMS_PER_PAGE_BITS); // shift optimization
			indexPage = this.indexPageFactory.acquirePage(indexPageIndex);
			int indexItemOffset = (int) (Util.mul(Util.mod(index, INDEX_ITEMS_PER_PAGE_BITS),
					INDEX_ITEM_LENGTH_BITS));

			ByteBuffer indexItemBuffer = indexPage.getLocal(indexItemOffset);
			return indexItemBuffer;
		} finally {
			if (indexPage != null) {
				this.indexPageFactory.releasePage(indexPageIndex);
			}
		}
	}

	void validateIndex(long index) {
		if (this.arrayTailIndex.get() <= this.arrayHeadIndex.get()) {
			if (index < this.arrayTailIndex.get() || index >= this.arrayHeadIndex.get()) {
				throw new IndexOutOfBoundsException();
			}
		} else {
			if (index < this.arrayTailIndex.get() && index >= this.arrayHeadIndex.get()) {
				throw new IndexOutOfBoundsException();
			}
		}
	}

	public void close() throws IOException {
		try {
			this.arrayWriteLock.lock();
			if (this.metaPageFactory != null) {
				this.metaPageFactory.releaseCachedPages();
			}
			if (this.indexPageFactory != null) {
				this.indexPageFactory.releaseCachedPages();
			}
			if (this.dataPageFactory != null) {
				this.dataPageFactory.releaseCachedPages();
			}
		} finally {
			this.arrayWriteLock.unlock();
		}
	}

	public void flush() {
		try {
			arrayReadLock.lock();
			this.metaPageFactory.flush();
			this.indexPageFactory.flush();
			this.dataPageFactory.flush();
		} finally {
			arrayReadLock.unlock();
		}

	}

	private void initialize() throws IOException {
		// initialize page factories
		this.indexPageFactory = new MappedPageFactory(INDEX_PAGE_SIZE,
				this.dir + INDEX_PAGE_FOLDER);
		this.dataPageFactory = new MappedPageFactory(this.dataPageSize,
				this.dir + DATA_PAGE_FOLDER);
		// the ttl does not matter here since meta data page is always cached
		this.metaPageFactory = new MappedPageFactory(META_DATA_PAGE_SIZE,
				this.dir + META_DATA_PAGE_FOLDER);

		// initialize array indexes
		this.initArrayIndex();

		// initialize data page indexes
		this.initDataPageIndex();
	}

	// find out array head/tail from the meta data
	private void initArrayIndex() throws IOException {
		MappedPage metaDataPage = this.metaPageFactory.acquirePage(META_DATA_PAGE_INDEX);
		ByteBuffer metaBuf = metaDataPage.getLocal(0);
		long head = metaBuf.getLong();
		long tail = metaBuf.getLong();

		arrayHeadIndex.set(head);
		arrayTailIndex.set(tail);
	}

	// find out data page head index and offset
	void initDataPageIndex() throws IOException {

		if (this.isEmpty()) {
			headDataPageIndex = 0L;
			headDataItemOffset = 0;
		} else {
			MappedPage previousIndexPage = null;
			long previousIndexPageIndex = -1;
			try {
				long previousIndex = this.arrayHeadIndex.get() - 1;
				previousIndexPageIndex = Util.div(previousIndex, INDEX_ITEMS_PER_PAGE_BITS); // shift optimization
				previousIndexPage = this.indexPageFactory.acquirePage(previousIndexPageIndex);
				int previousIndexPageOffset = (int) (Util
						.mul(Util.mod(previousIndex, INDEX_ITEMS_PER_PAGE_BITS), INDEX_ITEM_LENGTH_BITS));
				ByteBuffer previousIndexItemBuffer = previousIndexPage.getLocal(previousIndexPageOffset);
				long previousDataPageIndex = previousIndexItemBuffer.getLong();
				int previousDataItemOffset = previousIndexItemBuffer.getInt();
				int perviousDataItemLength = previousIndexItemBuffer.getInt();

				headDataPageIndex = previousDataPageIndex;
				headDataItemOffset = previousDataItemOffset + perviousDataItemLength;
			} finally {
				if (previousIndexPage != null) {
					this.indexPageFactory.releasePage(previousIndexPageIndex);
				}
			}
		}
	}
}
