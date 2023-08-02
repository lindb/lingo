package io.lindb.lingo.runtime.ep.plugin.udf;

import io.lindb.lingo.common.model.AggregateType;
import io.lindb.lingo.common.model.Field;
import io.lindb.lingo.runtime.ep.annotation.UserDefineFunction;

public final class Functions {
	private final static long now = System.currentTimeMillis() * 1000;

	private Functions() {
	}

	@UserDefineFunction(name = "trunc_sec")
	public static long truncateBySecond(long timestamp, int second) {
		if (timestamp > now) {
			// maybe ns
			timestamp = timestamp / 1_000_000;
		}
		return timestamp / (second * 1_000) * (second * 1_000);
	}

	@UserDefineFunction(name = "f_sum")
	public static Field sumField(double value) {
		return new Field(AggregateType.Sum, value);
	}

}
