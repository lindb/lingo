package io.lindb.lingo.runtime.pipeline;

import lombok.Data;

/**
 * Timer
 */
@Data
public class Timer {
	private boolean enable = true;
	private long interval = 1000;
}
