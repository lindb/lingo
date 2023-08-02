package io.lindb.lingo.common.model;

import lombok.Data;

@Data
@Analysable
// https://opentelemetry.io/docs/specs/otel/trace/semantic_conventions/exceptions/
public class ExceptionEvent {
	private Span span;
	private String exceptionType;
}
