package io.lindb.lingo.runtime.pipeline.source.otel;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.protobuf.ByteString;

import io.grpc.stub.StreamObserver;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceGrpc;
import io.opentelemetry.proto.resource.v1.Resource;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.ScopeSpans;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.proto.trace.v1.TracesData;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;
import io.lindb.lingo.common.queue.FanOutQueue;

@Log4j2
@GrpcService
public class TraceService extends TraceServiceGrpc.TraceServiceImplBase {
	@Setter
	private FanOutQueue queue;

	@Override
	public void export(ExportTraceServiceRequest request,
			StreamObserver<ExportTraceServiceResponse> responseObserver) {
		Map<ByteString, TracesData.Builder> traces = new HashMap<>();
		Map<ByteString, ScopeSpans.Builder> spans = new HashMap<>();
		for (ResourceSpans resourceSpans : request.getResourceSpansList()) {
			Resource resource = resourceSpans.getResource();
			List<ScopeSpans> scopeSpanList = resourceSpans.getScopeSpansList();
			for (ScopeSpans scopeSpans : scopeSpanList) {
				List<Span> spanList = scopeSpans.getSpansList();
				for (Span span : spanList) {
					ByteString traceId = span.getTraceId();
					ScopeSpans.Builder traceSpans = spans.get(traceId);
					if (traceSpans == null) {
						traceSpans = ScopeSpans.newBuilder();
						spans.put(traceId, traceSpans);
					}
					traceSpans.addSpans(span);
				}
			}

			for (Map.Entry<ByteString, ScopeSpans.Builder> entry : spans.entrySet()) {
				ByteString traceId = entry.getKey();
				TracesData.Builder traceSpans = traces.get(traceId);
				if (traceSpans == null) {
					traceSpans = TracesData.newBuilder();
					traces.put(traceId, traceSpans);
				}
				ResourceSpans.Builder rsb = ResourceSpans.newBuilder();
				rsb.setResource(resource);
				rsb.addScopeSpans(entry.getValue());
				traceSpans.addResourceSpans(rsb);
			}
			spans.clear(); // clear old spans
		}

		for (Map.Entry<ByteString, TracesData.Builder> entry : traces.entrySet()) {
			// TODO: need modify change trace id format
			// FIXME: batch trace data????
			log.info("receive trace [{}]", TraceId.fromBytes(entry.getKey().toByteArray()));
			try {
				long appendIndex = this.queue.nextAppendIndex();
				this.queue.put(appendIndex, entry.getValue().build().toByteArray());
			} catch (IOException e) {
				log.error("write trace data into queue failure:", e);
			}
		}

		// write otpl grpc response
		ExportTraceServiceResponse resp = ExportTraceServiceResponse.newBuilder().build();
		responseObserver.onNext(resp);
		responseObserver.onCompleted();
	}
}
