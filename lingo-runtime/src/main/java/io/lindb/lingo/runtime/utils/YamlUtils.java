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

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

public final class YamlUtils {
	private YamlUtils() {
	}

	public static Map<String, Object> loadYamlFile(String file) throws Exception {
		Representer representer = new Representer(new DumperOptions());
		representer.getPropertyUtils().setSkipMissingProperties(true);

		Yaml yaml = new Yaml(representer);
		InputStream input = new FileInputStream(file);

		return yaml.load(input);
	}

	public static <T> T loadYamlFile(String file, Class<T> clazz) throws Exception {
		Representer representer = new Representer(new DumperOptions());
		representer.getPropertyUtils().setSkipMissingProperties(true);

		Yaml yaml = new Yaml(representer);
		InputStream input = new FileInputStream(file);

		return yaml.loadAs(input, clazz);
	}

	public static <T> T loadYamlString(String yamlContent, Class<T> clazz) {
		Representer representer = new Representer(new DumperOptions());
		representer.getPropertyUtils().setSkipMissingProperties(true);

		Yaml yaml = new Yaml(representer);

		return yaml.loadAs(yamlContent, clazz);
	}

	public static <T> T covertObject(Object obj, Class<T> clazz) {
		Yaml yaml = new Yaml();
		String str = yaml.dump(obj);

		return loadYamlString(str, clazz);
	}

}
