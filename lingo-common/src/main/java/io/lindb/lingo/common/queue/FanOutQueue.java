package io.lindb.lingo.common.queue;

import java.io.IOException;

public interface FanOutQueue {
	ConsumerGroup getOrCreateConsumerGroup(String consumerGroup) throws IOException;

	long nextAppendIndex() throws IOException;

	void put(long appendIndex, byte[] data) throws IOException;

	byte[] get(long index) throws IOException;

	void flush();

	void close() throws IOException;
}
