package io.lindb.lingo.common.queue;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.log4j.Log4j2;

import static java.nio.channels.FileChannel.MapMode.READ_WRITE;

/**
 * MappedPageFactory
 */
@Log4j2
public class MappedPageFactory {
	public static final String PAGE_FILE_NAME = "page";
	public static final String PAGE_FILE_SUFFIX = ".dat";

	private final Object mapLock = new Object();
	private final Map<Long, Object> pageCreationLockMap = new HashMap<Long, Object>();
	private Map<Long, MappedPage> cache;
	private int pageSize;
	private String pageFile;
	private String pageDir;
	private File pageDirFile;

	public MappedPageFactory(int pageSize, String pageDir) {
		this.pageSize = pageSize;
		this.pageDir = pageDir;
		this.pageDirFile = new File(this.pageDir);
		if (!pageDirFile.exists()) {
			pageDirFile.mkdirs();
		}
		if (!this.pageDir.endsWith(File.separator)) {
			this.pageDir += File.separator;
		}
		this.pageFile = this.pageDir + PAGE_FILE_NAME + "-";
		this.cache = new HashMap<>();
	}

	public MappedPage acquirePage(long index) throws IOException {
		MappedPage mpi = this.cache.get(index);
		if (mpi == null) { // not in cache, need to create one
			try {
				Object lock = null;
				synchronized (mapLock) {
					if (!pageCreationLockMap.containsKey(index)) {
						pageCreationLockMap.put(index, new Object());
					}
					lock = pageCreationLockMap.get(index);
				}
				synchronized (lock) { // only lock the creation of page index
					mpi = cache.get(index); // double check
					if (mpi == null) {
						RandomAccessFile raf = null;
						FileChannel channel = null;
						try {
							String fileName = this.getFileNameByIndex(index);
							raf = new RandomAccessFile(fileName, "rw");
							channel = raf.getChannel();
							MappedByteBuffer mbb = channel.map(READ_WRITE, 0, this.pageSize);
							mpi = new MappedPage(mbb, fileName, index);
							cache.put(index, mpi);
							if (log.isDebugEnabled()) {
								log.debug("Mapped page for " + fileName + " was just created and cached.");
							}
						} finally {
							if (channel != null) {
								channel.close();
							}
							if (raf != null) {
								raf.close();
							}
						}
					}
				}
			} finally {
				synchronized (mapLock) {
					pageCreationLockMap.remove(index);
				}
			}
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Hit mapped page " + mpi.getPageFile() + " in cache.");
			}
		}

		return mpi;
	}

	public void releasePage(long index) {
		// FIXME:
		// cache.release(index);
	}

	public void releaseCachedPages() throws IOException {
		// FIXME:
		// cache.removeAll();
	}

	private String getFileNameByIndex(long index) {
		return this.pageFile + index + PAGE_FILE_SUFFIX;
	}

	public void flush() {
		for (MappedPage mappedPage : this.cache.values()) {
			mappedPage.flush();
		}
	}
}
