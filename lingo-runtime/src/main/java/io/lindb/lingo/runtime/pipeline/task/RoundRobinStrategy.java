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
