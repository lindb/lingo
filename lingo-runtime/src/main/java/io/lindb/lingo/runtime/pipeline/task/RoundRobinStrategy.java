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
package io.lindb.lingo.runtime.pipeline.task;

import java.util.concurrent.atomic.AtomicInteger;

import io.lindb.lingo.runtime.pipeline.ComponentType;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * RoundRobinStrategy
 */
@ComponentType(type = "round_robin")
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RoundRobinStrategy implements ShardingStrategy {
	private final int parallelism;
	private final AtomicInteger seq = new AtomicInteger(0);

	public RoundRobinStrategy(int parallelism) {
		this.parallelism = parallelism;
	}

	@Override
	public int chooseTasks(Object key) {
		if (this.parallelism <= 1) {
			return 0;
		}
		int cur = this.seq.getAndIncrement();
		if (cur < 0) {
			// reset seq
			cur = 0;
			this.seq.set(1);
		}
		return cur % this.parallelism;
	}

}
