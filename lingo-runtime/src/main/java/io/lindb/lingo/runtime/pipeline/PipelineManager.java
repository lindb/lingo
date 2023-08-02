package io.lindb.lingo.runtime.pipeline;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PipelineManager {
	private final Map<String, Pipeline> pipelines;

	public PipelineManager() {
		this.pipelines = new ConcurrentHashMap<>();
	}

	public void addPipeline(Pipeline pipeline) throws Exception {
		String name = pipeline.getName();
		if (this.pipelines.containsKey(name)) {
			throw new RuntimeException("pipeline exist");
		}
		this.pipelines.put(name, pipeline);
	}

	public Pipeline getPipelineByName(String name) {
		return this.pipelines.get(name);
	}
}
