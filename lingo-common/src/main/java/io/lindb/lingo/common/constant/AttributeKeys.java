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
package io.lindb.lingo.common.constant;

// ref:
// https://github.com/open-telemetry/opentelemetry-collector/tree/main/semconv
// https://github.com/open-telemetry/opentelemetry-specification/tree/main/specification
public interface AttributeKeys {
	String Unknown = "Unknown";

	String ServiceName = "service.name";
	String InstanceId = "service.instance.id";
	String ServiceVersion = "service.version";
	String SDKName = "telemetry.sdk.name";
	String SDKLanguage = "telemetry.sdk.language";
	String SDKVersion = "telemetry.sdk.version";

	String RPCSystem = "rpc.system";
	String RPCService = "rpc.service";
	String RPCMethod = "rpc.method";

	String DBSystem = "db.system";
	String DBName = "db.name";

	String MessagingSystem = "messaging.system";
	String MessagingOperation = "messaging.operation";

	String MessagingOperationPublish = "publish";
	String MessagingOperationReceive = "receive";
	String MessagingOperationProcess = "process";

	String ServerAddress = "server.address";
}
