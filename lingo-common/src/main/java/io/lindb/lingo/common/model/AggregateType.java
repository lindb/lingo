package io.lindb.lingo.common.model;

public enum AggregateType {
	Unspecified,
	Sum,
	Max,
	Min,
	Last,
	First;

	public static AggregateType parseType(String typeStr) {
		switch (typeStr.toLowerCase()) {
			case "sum":
				return Sum;
			case "max":
				return Max;
			case "min":
				return Min;
			case "last":
				return Last;
			case "first":
				return First;
			default:
				return Unspecified;
		}
	}
}
