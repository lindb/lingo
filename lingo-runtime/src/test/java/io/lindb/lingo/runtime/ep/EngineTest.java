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
package io.lindb.lingo.runtime.ep;

import org.junit.jupiter.api.Test;

import lombok.extern.log4j.Log4j2;
import io.lindb.lingo.common.model.Span;

@Log4j2
public class EngineTest {

	@Test
	public void deploy() throws Exception {
		log.info("test....");
		Engine engine = new Engine(event -> System.out.println(event));
		engine.prepare();
		engine.deploy(
				"@Metric(name='aa',fields={'count'}) select name,trunc_sec(startTime, 10) as timestamp,"
						+ "sampling('sum',traceId),count(1) as count from Span group by trunc_sec(startTime, 10)");
		for (int i = 0; i < 3; i++) {
			Span span = new Span();
			span.setTraceId("traceId-" + i);
			span.setName("span-" + i);
			span.setStartTime(i);
			engine.emit(span);
		}
	}
}
