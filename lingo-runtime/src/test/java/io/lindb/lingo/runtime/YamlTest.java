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
