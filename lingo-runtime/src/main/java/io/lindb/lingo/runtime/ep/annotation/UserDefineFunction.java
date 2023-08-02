package io.lindb.lingo.runtime.ep.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface UserDefineFunction {
	String name();
}
