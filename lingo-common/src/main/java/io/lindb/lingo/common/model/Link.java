package io.lindb.lingo.common.model;

import java.util.Map;

import lombok.Data;

@Data
public class Link {
	private String traceId;
	private String spanId;
	String traceState;
	Map<String, String> tags;
}
