package io.lindb.lingo.runtime.ep.annotation;

/**
 * Metric
 */
public @interface Metric {
	String namespace() default "";

	String name() default "";

	String timestamp() default "timestamp";

	String[] tags() default {};

	String[] fields() default {};

	String sampling() default "sampling";
}
