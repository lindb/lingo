package io.lindb.lingo.runtime.pipeline;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ComponentType {
	String type();
}
