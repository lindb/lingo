package io.lindb.lingo.runtime.pipeline.transform;

import java.io.File;
import java.util.List;
import java.util.Map;

import io.lindb.lingo.runtime.ep.Engine;
import lombok.Setter;
import io.lindb.lingo.runtime.pipeline.ComponentType;
import io.lindb.lingo.runtime.pipeline.ParallelComponent;
import io.lindb.lingo.runtime.pipeline.task.Handle;

/**
 * EPAggregateTransform
 */
@ComponentType(type = "ep")
public class EPAggregate extends ParallelComponent {
	@Setter
	private Map<String, String> sql;
	@Setter
	private Map<String, String> files;

	@Override
	public Handle newHandler() {
		try {
			return new EPHandle();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	class EPHandle implements Handle {

		private Engine engine;

		EPHandle() throws Exception {
			this.engine = new Engine(event -> next(null, event)); // output aggregate result
			this.engine.prepare();

			if (sql != null) {
				for (String epl : sql.values()) {
					this.engine.deploy(epl);
				}
			}
			if (files != null) {
				for (String file : files.values()) {
					this.engine.deploy(this.getClass().getClassLoader().getResource(file));
				}
			}
		}

		@Override
		public void handleEvent(Object key, Object event) {
			this.engine.emit(event);
		}
	}

}
