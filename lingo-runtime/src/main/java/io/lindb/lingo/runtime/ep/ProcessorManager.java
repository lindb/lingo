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
