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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import io.lindb.lingo.runtime.event.TimeTick;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * AbstractComponent
 */
@Log4j2
public abstract class AbstractComponent implements Component, ApplicationContextAware {
	private final List<Component> outputs = new ArrayList<>();
	private final AtomicInteger ref = new AtomicInteger(); // source->target(how many sources use it)
	@Setter
	protected String name;
	@Setter
	protected ApplicationContext context;
	@Setter
	protected Map<String, String> inputs;

	@Override
	public void ref() {
		this.ref.incrementAndGet();
	}

	@Override
	public void addOutput(Component output) {
		output.ref();
		this.outputs.add(output);
	}

	@Override
	public void next(Object key, Object event) {
		for (Component output : this.outputs) {
			output.handle(key, event);
		}
	}

	@Override
	public void shutdown() {
		this.ref.decrementAndGet();

		if (this.ref.get() <= 0) {
			// TODO: shutdown current

			for (Component output : this.outputs) {
				output.shutdown();
			}
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = applicationContext;
	}

	public void initInputStreams() {
		if (this.inputs != null) {
			for (String input : this.inputs.values()) {
				Component component = this.context.getBean(input, Component.class);
				component.addOutput(this);
				log.info("add ouput component[{}] to input component[{}]", this.name, input);
			}
		}
	}

	@Override
	public void handle(Object key, Object event) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onTick(TimeTick tick) {
		// TODO Auto-generated method stub
	}

}
