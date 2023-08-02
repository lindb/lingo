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
