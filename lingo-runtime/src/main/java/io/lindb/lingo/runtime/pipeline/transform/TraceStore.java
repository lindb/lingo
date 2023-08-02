package io.lindb.lingo.runtime.pipeline.transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.protobuf.ByteString;

import io.lindb.lingo.common.queue.FanOutQueue;
import io.lindb.lingo.common.queue.QueueManager;
import io.lindb.lingo.runtime.pipeline.ComponentType;
import io.lindb.lingo.runtime.pipeline.ParallelComponent;
import io.lindb.lingo.runtime.pipeline.task.Handle;
import io.lindb.lingo.storage.trace.TraceStorage;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.ScopeSpans;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.proto.trace.v1.TracesData;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * TraceStore
 */
@ComponentType(type = "trace_store")
@Log4j2
public class TraceStore extends ParallelComponent {

	@Autowired
	@Setter
	private TraceStorage traceStorage;
	@Autowired
	@Setter
	private QueueManager queueManager;

	@Override
	public Handle newHandler() {
		return (key, event) -> {
			if (!(event instanceof byte[])) {
				return;
			}
			if (!(key instanceof Long)) {
				return;
			}
			byte[] data = (byte[]) event;
			Long msgId = (Long) key;
			Map<String, Long> traceIds = new HashMap<>();
			TracesData traces;
			try {
				traces = TracesData.parseFrom(data);
				for (ResourceSpans resourceSpan : traces.getResourceSpansList()) {
					for (ScopeSpans scopeSpans : resourceSpan.getScopeSpansList()) {
						for (Span span : scopeSpans.getSpansList()) {
							ByteString traceId = span.getTraceId();
							traceIds.put(TraceId.fromBytes(traceId.toByteArray()), msgId);
							break;
						}
					}
				}
				this.traceStorage.save(traceIds);
			} catch (Exception e) {
				// TODO: Auto-generated catch block
				e.printStackTrace();
			}
		};
	}

	public List<byte[]> getTrace(String traceId) throws Exception {
		List<Long> msgIds = this.traceStorage.getTrace(traceId);
		if (msgIds == null) {
			log.warn("trace message id not found, trace [{}]", traceId);
			return null;
		}
		FanOutQueue queue = this.queueManager.getOrCreate("trace_q");
		List<byte[]> traces = new ArrayList<>();
		for (Long msgId : msgIds) {
			byte[] traceData = queue.get(msgId);
			if (traceData != null) {
				traces.add(traceData);
			}
		}
		return traces;
	}
}
