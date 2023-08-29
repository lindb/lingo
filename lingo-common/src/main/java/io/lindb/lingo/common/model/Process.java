/**
 * Licensed to LinDB under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. LinDB licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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
