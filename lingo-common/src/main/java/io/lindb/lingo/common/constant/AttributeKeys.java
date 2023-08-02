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
