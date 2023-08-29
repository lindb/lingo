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
