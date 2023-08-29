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
package io.lindb.lingo.runtime.pipeline;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import lombok.Setter;
import io.lindb.lingo.runtime.pipeline.task.AsyncTask;
import io.lindb.lingo.runtime.pipeline.task.Handle;
import io.lindb.lingo.runtime.pipeline.task.RoundRobinStrategy;
import io.lindb.lingo.runtime.pipeline.task.ShardingStrategy;
import io.lindb.lingo.runtime.pipeline.task.TaskPool;
import io.lindb.lingo.runtime.utils.ReflectionUtil;

/**
 * ParallelComponent
 */
public abstract class ParallelComponent extends AbstractComponent {
	private static Map<String, ComponentDefine> shardingStrategies = new HashMap<>();
	private static ComponentDefine defaultShardingStrategy;
	static {
		Set<Class<?>> clazz = ReflectionUtil.getTypesAnnotatedWith(ComponentType.class);
		try {
			for (Class<?> aClass : clazz) {
				if (!ShardingStrategy.class.isAssignableFrom(aClass)) {
					continue;
				}
				ComponentType componentType = aClass.getAnnotation(ComponentType.class);
				ComponentDefine define = new ComponentDefine();
				define.setClazz(aClass);
				define.setBeanInfo(Introspector.getBeanInfo(aClass));
				shardingStrategies.put(componentType.type(), define);
			}
			defaultShardingStrategy = new ComponentDefine();
			defaultShardingStrategy.setClazz(RoundRobinStrategy.class);
			defaultShardingStrategy.setBeanInfo(Introspector.getBeanInfo(RoundRobinStrategy.class));
		} catch (IntrospectionException e) {
			throw new RuntimeException(e);
		}
	}
	@Setter
	protected int parallelism;
	@Setter
	protected int bufferSize;
	@Setter
	private String sharding;
	private TaskPool pool;

	public void startup() throws Exception {
		ComponentDefine shardingStrategy = shardingStrategies.get(this.sharding);
		if (shardingStrategy == null) {
			// use default sharding strategry
			shardingStrategy = defaultShardingStrategy;
		}
		ShardingStrategy strategy = (ShardingStrategy) this.context.getBean(shardingStrategy.getClazz(),
				this.parallelism);
		this.pool = new TaskPool(this.parallelism, strategy);

		for (int i = 0; i < this.parallelism; i++) {
			AsyncTask task = new AsyncTask(this.name, this.bufferSize, this.newHandler());
			task.initialize();
			this.pool.addTask(i, task);
		}
	}

	@Override
	public void handle(Object key, Object event) {
		this.pool.handleEvent(key, event);
	}

	public abstract Handle newHandler();
}
