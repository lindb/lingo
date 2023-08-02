package io.lindb.lingo.runtime.ep.annotation;

import com.espertech.esper.common.client.soda.AnnotationPart;
import com.espertech.esper.common.client.soda.EPStatementObjectModel;

/**
 * AnnotationProcessor
 */
public interface AnnotationProcessor {
	void initialize(AnnotationPart annotation, EPStatementObjectModel model);

	void validation();
}
