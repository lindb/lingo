/**
 * Licensed to LinDB under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. LinDB licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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
