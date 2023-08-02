package io.lindb.lingo.common.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Data;

/**
 * TraceData
 */
@Data
public class Trace {
	private Process process;
	private List<Span> spans;

	public void addSpan(Span span) {
		if (this.spans == null) {
			this.spans = new ArrayList<>();
		}
		this.spans.add(span);
	}

	public void sortSpans() {
		if (this.spans == null) {
			return;
		}
		Collections.sort(this.spans, (o1, o2) -> (int) (o1.getStartTime() - o2.getStartTime()));
	}
}
