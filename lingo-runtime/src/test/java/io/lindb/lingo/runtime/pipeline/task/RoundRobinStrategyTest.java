package io.lindb.lingo.runtime.pipeline.task;

import org.junit.jupiter.api.Test;

/*
 * RoundRobinStrategyTest
 */
public class RoundRobinStrategyTest {

	@Test
	void choose() {
		RoundRobinStrategy strategy = new RoundRobinStrategy(10);
		for (int i = 0; i < 10; i++) {
			System.out.println(strategy.chooseTasks("test"));
		}
	}
}
