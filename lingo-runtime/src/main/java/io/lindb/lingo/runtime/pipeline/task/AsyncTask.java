package io.lindb.lingo.runtime.pipeline.task;

import java.util.concurrent.TimeUnit;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.LiteBlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.TimeoutException;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import io.lindb.lingo.runtime.event.MutableEvent;
import io.lindb.lingo.runtime.event.MutableEventFactory;
import lombok.extern.log4j.Log4j2;

/**
 * AsyncTask
 */
@Log4j2
public class AsyncTask implements EventHandler<MutableEvent>, Task {
	private final String name;
	private final int bufferSize;
	private final Handle handle;
	private Disruptor<MutableEvent> disruptor;
	private RingBuffer<MutableEvent> buffer;

	public AsyncTask(String name, int bufferSize, Handle handle) {
		this.name = name;
		this.bufferSize = bufferSize;
		this.handle = handle;
	}

	public void initialize() {
		if (Integer.bitCount(bufferSize) != 1) {
			throw new IllegalArgumentException("Buffer size must be a power of 2.");
		}

		this.disruptor = new Disruptor<>(new MutableEventFactory(), bufferSize, r -> {
			return new Thread(r, this.name);
		}, ProducerType.MULTI, new LiteBlockingWaitStrategy());

		this.buffer = this.disruptor.getRingBuffer();
		this.disruptor.handleEventsWith(this);

		this.disruptor.setDefaultExceptionHandler(new ExceptionHandler<MutableEvent>() {
			@Override
			public void handleEventException(Throwable ex, long sequence, MutableEvent event) {
				log.error("async task [{}] handle event error:", name, ex);
			}

			@Override
			public void handleOnStartException(Throwable ex) {
				log.error("async task [{}] throw exception when start, error:", name, ex);
			}

			@Override
			public void handleOnShutdownException(Throwable ex) {
				log.error("async task [{}] throw exception when shutdown, error:", name, ex);
			}
		});
		this.disruptor.start();
		log.info("start async task queue for name [{}] class [{}].", this.name,
				this.getClass().getName());
	}

	@Override
	public void onEvent(MutableEvent event, long l, boolean b) throws Exception {
		Object e = event.getEvent();
		Object key = event.getKey();
		this.handle.handleEvent(key, e);
		// clear event data after get value
		event.clear();
	}

	@Override
	public void handleEvent(Object key, Object event) {
		final long sequence = this.buffer.next();

		final MutableEvent mutableEvent = this.buffer.get(sequence);
		mutableEvent.setEvent(key, event);
		buffer.publish(sequence);
	}

	public void shutdown() {
		try {
			// TODO: add config
			this.disruptor.shutdown(10, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
