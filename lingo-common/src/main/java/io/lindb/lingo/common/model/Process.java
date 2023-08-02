package io.lindb.lingo.common.model;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class Process {
	private String serviceName;
	private String instanceId;
	private String serviceVersion;
	private String sdk;
	private String sdkLanguage;
	private String sdkVersion;
	Map<String, String> tags;

	public void addTag(String key, String value) {
		if (this.tags == null) {
			this.tags = new HashMap<>();
		}
		this.tags.put(key, value);
	}
}
