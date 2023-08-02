package io.lindb.lingo.common.model;

import lombok.Data;

@Data
@Analysable
// https://opentelemetry.io/docs/specs/otel/trace/semantic_conventions/database/
public class DatabaseCall {
	private Span span;
	private String type;
	private String database;
	private String address;
}
