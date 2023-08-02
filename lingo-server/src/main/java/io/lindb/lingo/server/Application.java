package io.lindb.lingo.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import io.lindb.lingo.runtime.pipeline.Pipeline;
import io.lindb.lingo.runtime.pipeline.PipelineManager;
import io.lindb.lingo.runtime.pipeline.PipelineRunner;

@SpringBootApplication
@EnableAutoConfiguration
public class Application {

	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);

		// TODO: need add init logic
		Pipeline pipeline = PipelineRunner.run("otel_trace_pipeline.yml");
		PipelineManager pipelineMgr = ctx.getBean(PipelineManager.class);
		pipelineMgr.addPipeline(pipeline);
	}
}
