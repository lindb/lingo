package io.lindb.lingo.runtime.event;

import lombok.Getter;

/**
 * MutableEvent
 */
@Getter
public class MutableEvent {
	private long timestamp;
	private Object key;
	private Object event;

	public void setEvent(Object key, Object event) {
		this.key = key;
		this.event = event;
		this.timestamp = System.currentTimeMillis();
	}

	public void clear() {
		this.event = null;
		this.key = null;
	}

}
