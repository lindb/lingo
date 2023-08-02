package io.lindb.lingo.runtime.pipeline;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;

import lombok.extern.log4j.Log4j2;

/**
 * PipelineRunner
 */
@Log4j2
@SpringBootApplication
public class PipelineRunner {

	public static Pipeline run(String cfgFile) throws Exception {
		Properties props = new Properties();
		props.setProperty("spring.autoconfigure.exclude",
				"net.devh.boot.grpc.server.autoconfigure.GrpcServerFactoryAutoConfiguration");
		ConfigurableApplicationContext ctx = new SpringApplicationBuilder(PipelineRunner.class)
				.properties(props)
				.web(WebApplicationType.NONE)
				.initializers(applicationContext -> {
					try {
						Resource resource = applicationContext.getResource("classpath:" + cfgFile);
						YamlPropertySourceLoader sourceLoader = new YamlPropertySourceLoader();
						List<PropertySource<?>> yamlTestProperties = sourceLoader.load(cfgFile, resource);
						for (PropertySource<?> propertySource : yamlTestProperties) {
							applicationContext.getEnvironment().getPropertySources().addFirst(propertySource);
						}
						log.info("initialize pipeline application context successfully, config file {}", cfgFile);
					} catch (IOException e) {
						log.error("initialize pipeline application context failure", e);
					}
				})
				.run();
		Pipeline pipeline = ctx.getBean(Pipeline.class);
		pipeline.run(ctx);

		Runtime.getRuntime().addShutdownHook(new Thread(() -> pipeline.shutdown()));
		return pipeline;
	}
}
