package io.lindb.lingo.common.translator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.lindb.lingo.common.constant.AttributeKeys;
import io.lindb.lingo.common.model.Event;
import io.lindb.lingo.common.model.Link;
import io.lindb.lingo.common.model.Process;
import io.lindb.lingo.common.model.Span;
import io.lindb.lingo.common.model.SpanKind;
import io.lindb.lingo.common.model.Trace;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.resource.v1.Resource;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.ScopeSpans;
import io.opentelemetry.proto.trace.v1.TracesData;

/**
 * OTLPToLingo
 */
public class OTLPToLingo {
	private static final String ResourceNoServiceName = "OTLPResourceNoServiceName";

	private OTLPToLingo() {

	}

	public static List<Trace> toTrace(TracesData traceData) {
		if (traceData.getResourceSpansCount() == 0) {
			return null;
		}
		List<ResourceSpans> resourceSpanList = traceData.getResourceSpansList();
		List<Trace> traces = new ArrayList<>();
		for (ResourceSpans resourceSpans : resourceSpanList) {
			Trace trace = new Trace();
			traces.add(trace);
			Process process = convertResource(resourceSpans.getResource());
			trace.setProcess(process);

			if (resourceSpans.getScopeSpansCount() == 0) {
				continue;
			}
			List<ScopeSpans> spanList = resourceSpans.getScopeSpansList();
			for (ScopeSpans scopeSpans : spanList) {
				if (scopeSpans.getSpansCount() == 0) {
					continue;
				}
				List<io.opentelemetry.proto.trace.v1.Span> spans = scopeSpans.getSpansList();
				for (io.opentelemetry.proto.trace.v1.Span otSpan : spans) {
					Span span = convertSpan(otSpan);
					span.setProcess(process);
					trace.addSpan(span);
				}
			}
			// sort span by start time
			trace.sortSpans();
		}
		return traces;
	}

	private static Span convertSpan(io.opentelemetry.proto.trace.v1.Span otSpan) {
		Span span = new Span();
		// FIXME: convert id
		span.setTraceId(TraceId.fromBytes(otSpan.getTraceId().toByteArray()));
		span.setParentSpanId(SpanId.fromBytes(otSpan.getParentSpanId().toByteArray()));
		span.setSpanId(SpanId.fromBytes(otSpan.getSpanId().toByteArray()));
		span.setName(otSpan.getName());
		span.setStartTime(otSpan.getStartTimeUnixNano());
		span.setEndTime(otSpan.getEndTimeUnixNano());
		span.setDuration(span.getEndTime() - span.getStartTime());
		span.setKind(SpanKind.valueOf(otSpan.getKindValue()));
		span.setTags(convertTags(otSpan.getAttributesList()));

		if (otSpan.getEventsCount() > 0) {
			// process span events
			List<io.opentelemetry.proto.trace.v1.Span.Event> events = otSpan.getEventsList();
			for (io.opentelemetry.proto.trace.v1.Span.Event otEvent : events) {
				span.addEvent(convertEvent(otEvent));
			}
		}

		if (otSpan.getLinksCount() > 0) {
			// process span links
			List<io.opentelemetry.proto.trace.v1.Span.Link> links = otSpan.getLinksList();
			for (io.opentelemetry.proto.trace.v1.Span.Link otLink : links) {
				span.addLink(convertLink(otLink));
			}
		}

		return span;
	}

	private static Link convertLink(io.opentelemetry.proto.trace.v1.Span.Link otLink) {
		Link link = new Link();
		link.setTraceId(TraceId.fromBytes(otLink.getTraceId().toByteArray()));
		link.setSpanId(SpanId.fromBytes(otLink.getSpanId().toByteArray()));
		link.setTraceState(otLink.getTraceState());
		link.setTags(convertTags(otLink.getAttributesList()));
		return link;
	}

	private static Event convertEvent(io.opentelemetry.proto.trace.v1.Span.Event otEvent) {
		Event event = new Event();
		event.setName(otEvent.getName());
		event.setTimestamp(otEvent.getTimeUnixNano());
		event.setTags(convertTags(otEvent.getAttributesList()));
		return event;
	}

	private static Map<String, String> convertTags(List<KeyValue> attrs) {
		if (attrs == null || attrs.isEmpty()) {
			return null;
		}
		Map<String, String> tags = new HashMap<>();
		for (KeyValue attr : attrs) {
			String key = attr.getKey();
			String value = toString(attr.getValue());
			tags.put(key, value);
		}
		return tags;
	}

	private static Process convertResource(Resource resource) {
		Process process = new Process();
		if (resource.getAttributesCount() == 0) {
			process.setServiceName(ResourceNoServiceName);
			return process;
		}
		List<KeyValue> attrs = resource.getAttributesList();
		for (KeyValue attr : attrs) {
			String key = attr.getKey();
			String value = toString(attr.getValue());
			switch (key) {
				case AttributeKeys.ServiceName:
					process.setServiceName(value);
					break;
				case AttributeKeys.InstanceId:
					process.setInstanceId(value);
					break;
				case AttributeKeys.ServiceVersion:
					process.setServiceVersion(value);
					break;
				case "library.name":
				case AttributeKeys.SDKName:
					process.setSdk(value);
					break;
				case "library.language":
				case AttributeKeys.SDKLanguage:
					process.setSdkLanguage(value);
					break;
				case "library.version":
				case AttributeKeys.SDKVersion:
					process.setSdkVersion(value);
					break;
				default:
					// other attrs add to process's tags
					process.addTag(key, value);
			}
		}
		return process;
	}

	private static String toString(AnyValue value) {
		switch (value.getValueCase()) {
			case STRING_VALUE:
				return value.getStringValue();
			case BOOL_VALUE:
				return Boolean.toString(value.getBoolValue());
			case INT_VALUE:
				return Long.toString(value.getIntValue());
			case DOUBLE_VALUE:
				return Double.toString(value.getDoubleValue());
			case ARRAY_VALUE:
				return value.getArrayValue().getValuesList().toString();
			case KVLIST_VALUE:
				return value.getKvlistValue().getValuesList().toString();
			case VALUE_NOT_SET:
			default:
				return "unknown";
		}
	}
}
