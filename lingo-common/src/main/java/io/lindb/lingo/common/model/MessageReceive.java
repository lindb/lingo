package io.lindb.lingo.common.model;

import lombok.Data;

@Data
@Analysable
public class MessageReceive {
	private Span span;
	private String type;
}
