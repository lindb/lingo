package io.lindb.lingo.common.model;

import lombok.Data;

@Data
@Analysable
// https://opentelemetry.io/docs/specs/otel/trace/semantic_conventions/rpc/
public class RPCServer {
	private Span span;
	private String type;
	private String service;
	private String method;
}
