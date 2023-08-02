package io.lindb.lingo.common.model;

import lombok.Data;

@Data
@Analysable
public class MessagePublish {
	private Span span;
	private String type;
}
