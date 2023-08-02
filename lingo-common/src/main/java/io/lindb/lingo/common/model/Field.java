package io.lindb.lingo.common.model;

import lombok.Data;

/**
 * Field
 */
@Data
public class Field {
	private AggregateType aggregateType;
	private double value;

	public Field(AggregateType aggregateType, double value) {
		this.aggregateType = aggregateType;
		this.value = value;
	}

}
