package io.lindb.lingo.runtime.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

/**
 * ReflectionUtil
 */
public class ReflectionUtil {
	private ReflectionUtil() {
	}

	private static Reflections reflections;

	static {
		reflections = new Reflections(
				new ConfigurationBuilder().addUrls(ClasspathHelper.forPackage("io.lindb.lingo"))
						.addScanners(Scanners.TypesAnnotated, Scanners.SubTypes,
								Scanners.MethodsAnnotated));
	}

	public static <T> Set<Class<? extends T>> scannerSubType(Class<T> subType) {
		return reflections.getSubTypesOf(subType);
	}

	public static Set<Class<?>> getTypesAnnotatedWith(Class<? extends Annotation> annotation) {
		return reflections.getTypesAnnotatedWith(annotation);
	}

	public static Set<Method> getMethodsAnnotatedWith(Class<? extends Annotation> annotation) {
		return reflections.getMethodsAnnotatedWith(annotation);
	}

	public static Set<Field> getAllFields(Class<?> clazz) {
		return ReflectionUtils.getAllFields(clazz);
	}
}
