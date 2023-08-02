package io.lindb.lingo.common.model;

import lombok.Data;

@Data
@Analysable
// https://opentelemetry.io/docs/specs/semconv/messaging/messaging-spans/
public class MessageProcess {
	private Span span;
	private String type;
}
