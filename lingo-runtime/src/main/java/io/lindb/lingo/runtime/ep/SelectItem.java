package io.lindb.lingo.runtime.ep;

import lombok.Data;
import io.lindb.lingo.common.model.AggregateType;

/**
 * SelectItem
 */
@Data
public class SelectItem {
	public enum Type {
		Normal,
		Sum,
		Min,
		Max,
	}

	private String name;
	private Type type;

	public SelectItem() {
	}

	public SelectItem(String name, Type type) {
		this.name = name;
		this.type = type;
	}

	public AggregateType getAggregateType() {
		switch (this.type) {
			case Sum:
				return AggregateType.Sum;
			case Min:
				return AggregateType.Min;
			case Max:
				return AggregateType.Max;
			default:
				return AggregateType.Last;
		}
	}
}
