package io.lindb.lingo.server.configure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.lindb.lingo.runtime.pipeline.PipelineManager;

@Configuration
public class ServerConfigure {

	@Bean
	public PipelineManager pipelineManager() {
		return new PipelineManager();
	}
}
