package io.lindb.lingo.runtime.pipeline.transform;

import java.util.List;

import io.lindb.lingo.common.model.Span;
import io.lindb.lingo.common.model.Trace;
import io.lindb.lingo.common.translator.OTLPToLingo;
import io.lindb.lingo.runtime.pipeline.ComponentType;
import io.lindb.lingo.runtime.pipeline.ParallelComponent;
import io.lindb.lingo.runtime.pipeline.task.Handle;
import io.opentelemetry.proto.trace.v1.TracesData;
import lombok.extern.log4j.Log4j2;

/**
 * TraceDecode
 */
@ComponentType(type = "trace_decode")
@Log4j2
public class TraceDecode extends ParallelComponent {

	@Override
	public Handle newHandler() {
		return (key, event) -> {
			if (!(event instanceof byte[])) {
				return;
			}
			byte[] data = (byte[]) event;
			TracesData traces;
			try {
				traces = TracesData.parseFrom(data);
				List<Trace> traceList = OTLPToLingo.toTrace(traces);
				if (traceList == null) {
					return;
				}
				for (Trace trace : traceList) {
					List<Span> spans = trace.getSpans();
					for (Span span : spans) {
						Object traceEvent = span.toEvent();
						next(null, traceEvent);
					}
				}
			} catch (Exception e) {
				log.error("transform trace data failure", e);
			}
		};
	}

}
