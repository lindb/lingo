package io.lindb.lingo.runtime.event;

import com.lmax.disruptor.EventFactory;

/**
 * MutableEventFactory
 */
public class MutableEventFactory implements EventFactory<MutableEvent> {

	@Override
	public MutableEvent newInstance() {
		return new MutableEvent();
	}

}
