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
