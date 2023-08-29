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
package io.lindb.lingo.runtime;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;
import io.lindb.lingo.runtime.utils.YamlUtils;

import java.util.Map;

public class YamlTest {
	@Test
	public void loadYaml() throws Exception {
		Representer representer = new Representer(new DumperOptions());
		representer.getPropertyUtils().setSkipMissingProperties(true);

		Yaml yaml = new Yaml(representer);
		String file = this.getClass().getClassLoader().getResource("a.yml").getFile();
		Map<String, Object> param = YamlUtils.loadYamlFile(file);
		// Object obj = yaml.loadAs(input, PipelineConfig.class);
		System.out.println("test......");
		System.out.println(param);
	}
}
