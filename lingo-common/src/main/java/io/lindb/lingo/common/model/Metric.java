package io.lindb.lingo.common.model;

import java.util.Map;

import lombok.Data;

/**
 * Metric
 */
@Data
public class Metric {
	private String namespace;
	private String name;
	private long timestamp;
	private Map<String, String> tags;
	private Map<String, Field> fields;
	private String sampling;
}
