package io.lindb.lingo.common.model;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class Event {
	private String name;
	private long timestamp; // ns
	private Map<String, String> tags;

	public void addTag(String key, String value) {
		if (this.tags == null) {
			this.tags = new HashMap<>();
		}
		this.tags.put(key, value);
	}
}
