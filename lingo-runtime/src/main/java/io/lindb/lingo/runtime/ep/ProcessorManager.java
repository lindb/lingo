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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.espertech.esper.common.client.soda.AnnotationPart;
import com.espertech.esper.common.client.soda.EPStatementObjectModel;

import io.lindb.lingo.runtime.ep.annotation.AnnotationProcessor;
import io.lindb.lingo.runtime.ep.annotation.ProcessorFor;
import io.lindb.lingo.runtime.utils.ReflectionUtil;

/**
 * ProcessorManager
 */
public class ProcessorManager {
	private static Map<String, Class<? extends AnnotationProcessor>> processors = new HashMap<>();

	static {
		// scan all annotation processor
		Set<Class<? extends AnnotationProcessor>> annotationProcessor = ReflectionUtil.scannerSubType(
				AnnotationProcessor.class);
		for (Class<? extends AnnotationProcessor> clazz : annotationProcessor) {
			ProcessorFor annotation = clazz.getAnnotation(ProcessorFor.class);
			if (annotation != null) {
				processors.put(annotation.name().getSimpleName(), clazz);
			}
		}
	}

	public AnnotationProcessor createProcessor(AnnotationPart annotation, EPStatementObjectModel model,
			Map<String, SelectItem> selectItems) throws Exception {
		AnnotationProcessor processor = null;
		String name = annotation.getName();
		if (processors.containsKey(name)) {
			processor = processors.get(annotation.getName()).getDeclaredConstructor(new Class[] { Map.class })
					.newInstance(selectItems);
			processor.initialize(annotation, model);
			processor.validation();
		}
		return processor;
	}

}
