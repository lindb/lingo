package io.lindb.lingo.common.model;

public enum SpanKind {
	Unspecified(0),
	Internal(1),
	Server(2),
	Client(3),
	Producer(4),
	Consumer(5);

	private final int kind;

	private SpanKind(int kind) {
		this.kind = kind;
	}

	public static SpanKind valueOf(int kind) {
		switch (kind) {
			case 1:
				return Internal;
			case 2:
				return Server;
			case 3:
				return Client;
			case 4:
				return Producer;
			case 5:
				return Consumer;
			default:
				return Unspecified;
		}
	}
}
