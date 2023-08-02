package io.lindb.lingo.common.queue;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;

import lombok.extern.log4j.Log4j2;

/**
 * MappedPage
 */
@Log4j2
public class MappedPage {
	private final String pageFile;
	private ThreadLocalByteBuffer threadLocalBuffer;
	private volatile boolean dirty;
	private volatile boolean closed = false;
	private long index;

	public MappedPage(MappedByteBuffer mbb, String fileName, long index) {
		this.threadLocalBuffer = new ThreadLocalByteBuffer(mbb);
		this.pageFile = fileName;
		this.index = index;
	}

	public String getPageFile() {
		return this.pageFile;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public byte[] getLocal(int position, int length) {
		ByteBuffer buf = this.getLocal(position);
		// TODO: return buf direct???
		byte[] data = new byte[length];
		buf.get(data);
		return data;
	}

	public ByteBuffer getLocal(int position) {
		ByteBuffer buf = this.threadLocalBuffer.get();
		buf.position(position);
		return buf;
	}

	public void flush() {
		synchronized (this) {
			if (closed) {
				return;
			}
			if (dirty) {
				MappedByteBuffer srcBuf = (MappedByteBuffer) threadLocalBuffer.getSourceBuffer();
				srcBuf.force(); // flush the changes
				dirty = false;
				if (log.isDebugEnabled()) {
					log.debug("Mapped page for " + this.pageFile + " was just flushed.");
				}
			}
		}
	}

	public void close() throws IOException {
		synchronized (this) {
			if (closed) {
				return;
			}

			flush();

			MappedByteBuffer srcBuf = (MappedByteBuffer) threadLocalBuffer.getSourceBuffer();
			unmap(srcBuf);

			this.threadLocalBuffer = null; // hint GC

			closed = true;
			if (log.isDebugEnabled()) {
				log.debug("Mapped page for " + this.pageFile + " was just unmapped and closed.");
			}
		}
	}

	private static void unmap(MappedByteBuffer buffer) {
		Cleaner.clean(buffer);
	}

	/**
	 * Helper class allowing to clean direct buffers.
	 */
	// TODO: test jdk11???
	private static class Cleaner {
		public static final boolean CLEAN_SUPPORTED;
		private static final Method directBufferCleaner;
		private static final Method directBufferCleanerClean;

		static {
			Method directBufferCleanerX = null;
			Method directBufferCleanerCleanX = null;
			boolean v;
			try {
				directBufferCleanerX = Class.forName("java.nio.DirectByteBuffer").getMethod("cleaner");
				directBufferCleanerX.setAccessible(true);
				directBufferCleanerCleanX = Class.forName("sun.misc.Cleaner").getMethod("clean");
				directBufferCleanerCleanX.setAccessible(true);
				v = true;
			} catch (Exception e) {
				v = false;
			}
			CLEAN_SUPPORTED = v;
			directBufferCleaner = directBufferCleanerX;
			directBufferCleanerClean = directBufferCleanerCleanX;
		}

		public static void clean(ByteBuffer buffer) {
			if (buffer == null)
				return;
			if (CLEAN_SUPPORTED && buffer.isDirect()) {
				try {
					Object cleaner = directBufferCleaner.invoke(buffer);
					directBufferCleanerClean.invoke(cleaner);
				} catch (Exception e) {
					// silently ignore exception
					log.error("clearn mapped byte buffer error: ", e);
				}
			}
		}
	}

	private static class ThreadLocalByteBuffer extends ThreadLocal<ByteBuffer> {
		private ByteBuffer src;

		public ThreadLocalByteBuffer(ByteBuffer src) {
			this.src = src;
		}

		public ByteBuffer getSourceBuffer() {
			return this.src;
		}

		@Override
		protected synchronized ByteBuffer initialValue() {
			ByteBuffer dup = this.src.duplicate();
			return dup;
		}
	}
}
