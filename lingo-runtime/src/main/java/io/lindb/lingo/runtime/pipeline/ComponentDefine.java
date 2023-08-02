package io.lindb.lingo.runtime.pipeline;

import java.beans.BeanInfo;

import lombok.Data;

/**
 * ComponentDefine
 */
@Data
public class ComponentDefine {
	private Class<?> clazz;
	private BeanInfo beanInfo;
}
