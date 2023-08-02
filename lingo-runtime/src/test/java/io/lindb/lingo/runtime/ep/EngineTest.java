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
