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

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.lindb.lingo.common.constant.AttributeKeys;

@Data
@Analysable
public class Span {
	@JsonIgnore
	private Process process;
	private String traceId;
	private String parentSpanId;
	private String spanId;
	private String traceState;
	private String name;
	private SpanKind kind;
	private long startTime; // ns
	private long endTime; // ns
	private long duration; // ns
	private Map<String, String> tags;

	private List<Event> events;
	private List<Link> links;

	public void addLink(Link link) {
		if (this.links == null) {
			this.links = new ArrayList<>();
		}
		this.links.add(link);
	}

	public void addEvent(Event event) {
		if (this.events == null) {
			this.events = new ArrayList<>();
		}
		this.events.add(event);
	}

	public void addTag(String key, String value) {
		if (this.tags == null) {
			this.tags = new HashMap<>();
		}
		this.tags.put(key, value);
	}

	public Object toEvent() {
		if (this.tags == null || this.tags.isEmpty()) {
			return this;
		}
		if (this.tags.containsKey(AttributeKeys.RPCSystem)) {
			String type = this.getTagValue(AttributeKeys.RPCSystem);
			if (this.kind == SpanKind.Server) {
				RPCServer server = new RPCServer();
				server.setSpan(this);
				server.setType(type);
				server.setService(this.getTagValue(AttributeKeys.RPCService));
				server.setMethod(this.getTagValue(AttributeKeys.RPCMethod));
				return server;
			} else {
				RPCClient client = new RPCClient();
				client.setSpan(this);
				client.setType(type);
				client.setService(this.getTagValue(AttributeKeys.RPCService));
				client.setMethod(this.getTagValue(AttributeKeys.RPCMethod));
				return client;
			}
		} else if (this.tags.containsKey(AttributeKeys.DBSystem)) {
			String type = this.getTagValue(AttributeKeys.DBSystem);
			DatabaseCall dbCall = new DatabaseCall();
			dbCall.setSpan(this);
			dbCall.setType(type);
			dbCall.setDatabase(this.getTagValue(AttributeKeys.DBName));
			dbCall.setAddress(this.getTagValue(AttributeKeys.ServerAddress));
			return dbCall;
		} else if (this.tags.containsKey(AttributeKeys.MessagingSystem)) {
			String type = this.getTagValue(AttributeKeys.MessagingSystem);
			String op = this.getTagValue(AttributeKeys.MessagingOperation);

			switch (op) {
				case AttributeKeys.MessagingOperationPublish:
					MessagePublish publish = new MessagePublish();
					publish.setSpan(this);
					publish.setType(type);
					return publish;
				case AttributeKeys.MessagingOperationReceive:
					MessageReceive receive = new MessageReceive();
					receive.setSpan(this);
					receive.setType(type);
					return receive;
				case AttributeKeys.MessagingOperationProcess:
					MessageProcess process = new MessageProcess();
					process.setSpan(this);
					process.setType(type);
					return process;
			}
		}
		return this;
	}

	private String getTagValue(String key) {
		String value = this.tags.get(key);
		if (value == null || value.length() == 0) {
			return AttributeKeys.Unknown;
		}
		return value;
	}
}
