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
package io.lindb.lingo.runtime.ep;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.common.client.module.Module;
import com.espertech.esper.common.client.module.ModuleItem;
import com.espertech.esper.common.client.soda.AnnotationPart;
import com.espertech.esper.common.client.soda.EPStatementObjectModel;
import com.espertech.esper.common.client.soda.OutputLimitClause;
import com.espertech.esper.common.client.soda.OutputLimitSelector;
import com.espertech.esper.common.client.soda.OutputLimitUnit;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.EPCompiler;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.runtime.client.*;
import com.espertech.esper.runtime.client.option.StatementUserObjectRuntimeContext;
import com.espertech.esper.runtime.client.option.StatementUserObjectRuntimeOption;

import lombok.extern.log4j.Log4j2;
import io.lindb.lingo.runtime.ep.annotation.AnnotationProcessor;
import io.lindb.lingo.runtime.ep.annotation.UserDefineFunction;
import io.lindb.lingo.runtime.utils.ReflectionUtil;
import io.lindb.lingo.common.model.Analysable;

@Log4j2
public class Engine {
	public static final String RESULT_PROCESSORS = "result_processors";

	private final static String defaultAggregateContext = "aggregate_context_10sec";

	private final Output ouput;
	private Configuration config;
	private UpdateListener listener;
	private EPCompiler compiler;
	private EPRuntime runtime;
	private EPEventService eventService;
	private CompilerArguments args;
	private OutputLimitClause outputLimitClause;
	private ProcessorManager processorManager;

	public Engine(Output ouput) {
		this.ouput = ouput;
	}

	public void prepare() throws Exception {
		this.processorManager = new ProcessorManager();

		this.config = new Configuration();

		Set<Class<?>> events = ReflectionUtil.getTypesAnnotatedWith(Analysable.class);
		for (Class<?> event : events) {
			String name = event.getSimpleName();
			log.info("add event to ep engine, name:{}, {}", name, event);
			this.config.getCommon().addEventType(name, event);
		}

		// disable avro
		this.config.getCommon().getEventMeta().getAvroSettings().setEnableAvro(false);

		this.config.getCommon().addImport("io.lindb.lingo.runtime.ep.plugin.udf.*");
		this.config.getCommon().addAnnotationImport("io.lindb.lingo.runtime.ep.annotation.*");
		this.config.getCompiler().addPlugInAggregationFunctionForge("sampling",
				"io.lindb.lingo.runtime.ep.plugin.aggregator.SamplingAggregator");
		this.config.getCompiler().addPlugInAggregationFunctionForge("histogram",
				"io.lindb.lingo.runtime.ep.plugin.aggregator.HistogramAggregator");

		// add user define functions
		Set<Method> userDefineFunctions = ReflectionUtil.getMethodsAnnotatedWith(UserDefineFunction.class);
		for (Method method : userDefineFunctions) {
			UserDefineFunction userDefineFunction = method.getAnnotation(UserDefineFunction.class);
			this.config.getCompiler().addPlugInSingleRowFunction(userDefineFunction.name(),
					method.getDeclaringClass().getName(),
					method.getName());
		}

		// FIXME:xxxx
		this.runtime = EPRuntimeProvider.getRuntime(this.ouput.toString(), this.config);
		this.listener = new AggregatorEventListener(this.ouput);

		this.args = new CompilerArguments(this.config);
		this.compiler = EPCompilerProvider.getCompiler();

		this.outputLimitClause = new OutputLimitClause();
		this.outputLimitClause.setSelector(OutputLimitSelector.SNAPSHOT);
		this.outputLimitClause.setUnit(OutputLimitUnit.CONTEXT_PARTITION_TERM);

		this.createContext(
				String.format("@public create context %s start @now end after 10 seconds", defaultAggregateContext));

		this.eventService = this.runtime.getEventService();
	}

	public void createContext(String epl) throws Exception {
		EPCompiled compiled = compiler.compile(epl, this.args);
		this.runtime.getDeploymentService().deploy(compiled);
		this.args.getPath().add(this.runtime.getRuntimePath());
		log.info("create context epl: {}", epl);
	}

	public void deploy(URL file) throws Exception {
		log.info("load module from file: {}", file);
		Module module = this.compiler.readModule(file);
		log.debug("EPEngine deploy module [{}]", module.getName());

		for (int i = 0; i < module.getItems().size(); i++) {
			ModuleItem item = module.getItems().get(i);
			if (!item.isCommentOnly()) {
				this.deploy(item.getExpression());
			}
		}
	}

	public void deploy(String epl) throws Exception {
		log.info("ep engine start deploy epl: {}", epl);
		EPStatementObjectModel epModel = compiler.eplToModel(epl, this.config);

		// add default aggregate context
		if (epModel.getContextName() == null) {
			epModel.setOutputLimitClause(outputLimitClause);
			epModel.setContextName(defaultAggregateContext);
		}

		Map<String, Object> userObject = new HashMap<>();
		// select items
		Map<String, SelectItem> selectItems = ClauseUtil.buildSelectCluase(epModel);
		List<AnnotationPart> annotations = epModel.getAnnotations();
		if (annotations != null && !annotations.isEmpty()) {
			List<ResultProcessor<?>> processors = new ArrayList<>();
			for (AnnotationPart annotation : annotations) {
				AnnotationProcessor processor = this.processorManager.createProcessor(annotation, epModel, selectItems);
				if (processor instanceof ResultProcessor) {
					processors.add((ResultProcessor<?>) processor);
				}
			}
			userObject.put(RESULT_PROCESSORS, processors);
		}

		String finalEPL = epModel.toEPL();
		EPCompiled compiled = compiler.compile(finalEPL, this.args);

		DeploymentOptions options = new DeploymentOptions();
		options.setStatementUserObjectRuntime(new StatementUserObjectRuntimeOption() {
			public Object getUserObject(StatementUserObjectRuntimeContext env) {
				return userObject;
			}
		});
		EPDeployment deployment = this.runtime.getDeploymentService().deploy(compiled, options);

		args.getPath().add(this.runtime.getRuntimePath());
		String name = deployment.getStatements()[0].getName();
		EPStatement stmt = this.runtime.getDeploymentService().getStatement(deployment.getDeploymentId(), name);
		stmt.addListener(this.listener);

		log.info("create query epl [{}] raw: {}, final: {}", name, epl, finalEPL);
	}

	public void emit(Object event) {
		if (event == null) {
			return;
		}
		String eventName = event.getClass().getSimpleName();
		this.eventService.sendEventBean(event, eventName);
	}

	public void shutdown() throws Exception {
		if (this.runtime != null) {
			// TODO: before undeploy all statement, need flush event
			this.runtime.getDeploymentService().undeployAll();
		}
	}

}
