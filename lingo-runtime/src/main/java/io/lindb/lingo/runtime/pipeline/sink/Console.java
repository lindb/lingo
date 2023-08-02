package io.lindb.lingo.runtime.pipeline.sink;

import lombok.extern.log4j.Log4j2;
import io.lindb.lingo.runtime.pipeline.AbstractComponent;
import io.lindb.lingo.runtime.pipeline.ComponentType;

/**
 * Console
 */
@ComponentType(type = "console")
@Log4j2
public class Console extends AbstractComponent {

	@Override
	public void handle(Object key, Object event) {
		// FIXME:
		log.info("received key[{}], event[{}]", key, event);
	}

}
