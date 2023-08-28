package io.lindb.lingo.runtime.pipeline;

import org.junit.jupiter.api.Test;

/**
 * PipelineRunnerTest
 */
public class PipelineRunnerTest {

	@Test
	public void run() throws Exception {
		PipelineRunner.run("a.yml");
		// PipelineRunner.run("c.yml");
		Thread.sleep(10000);
	}
}
