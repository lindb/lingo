package io.lindb.lingo.runtime.ep;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.espertech.esper.common.client.soda.AnnotationAttribute;
import com.espertech.esper.common.client.soda.AnnotationPart;
import com.espertech.esper.common.client.soda.EPStatementObjectModel;
import com.espertech.esper.common.internal.event.map.MapEventBean;

import io.lindb.lingo.common.model.Field;
import io.lindb.lingo.common.model.Metric;
import io.lindb.lingo.runtime.ep.annotation.AnnotationProcessor;
import io.lindb.lingo.runtime.ep.annotation.ProcessorFor;
import io.lindb.lingo.runtime.utils.ObjectUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@ProcessorFor(name = io.lindb.lingo.runtime.ep.annotation.Metric.class)
public class MetricConvertProcessor implements ResultProcessor<Metric>, AnnotationProcessor {
	private static final Pattern PARAM_PATTERN = Pattern.compile("\\$\\{(.*?)}");
	private Map<String, SelectItem> selectItems;
	private String namespace = "namespace";
	private String name = "name";
	private String timestamp = "timestamp";
	private String[] tags;
	private String[] fields;
	private Set<String> nameParams;
	private Set<String> namespaceParams;

	public MetricConvertProcessor(Map<String, SelectItem> selectItems) {
		this.selectItems = selectItems;
	}

	@Override
	public void initialize(AnnotationPart annotation, EPStatementObjectModel model) {
		List<AnnotationAttribute> attributes = annotation.getAttributes();
		if (attributes == null) {
			return;
		}
		for (AnnotationAttribute attribute : attributes) {
			String attrName = attribute.getName();
			Object attrVal = attribute.getValue();
			if (attrVal == null) {
				continue;
			}
			switch (attrName) {
				case "namespace":
					this.namespace = (String) attrVal;
					this.namespaceParams = getParams(this.namespace);
					break;
				case "name":
					this.name = (String) attrVal;
					this.nameParams = getParams(this.name);
					break;
				case "timestamp":
					this.timestamp = (String) attrVal;
					break;
				case "tags":
					this.tags = ObjectUtil.toStrings((Object[]) attrVal);
					break;
				case "fields":
					this.fields = ObjectUtil.toStrings((Object[]) attrVal);
					break;
				// TODO: need add sampling
			}
		}
	}

	@Override
	public Metric process(MapEventBean event) {
		Map<String, Object> props = event.getProperties();
		if (props == null || props.isEmpty()) {
			return null;
		}
		Map<String, Field> fields = this.getFields(props);
		if (fields == null || fields.isEmpty()) {
			return null;
		}

		Metric metric = new Metric();
		metric.setNamespace(this.getParamValue(this.namespace, props, namespaceParams));
		metric.setName(this.getParamValue(this.name, props, nameParams));
		metric.setTags(this.getTags(props));
		metric.setTimestamp(this.getTimestamp(props));
		metric.setFields(fields);

		return metric;
	}

	@Override
	public void validation() {
		if (this.fields == null || this.fields.length == 0) {
			throw new RuntimeException("metric set empty field config");
		}
		for (String field : this.fields) {
			checkSelectItem(field);
		}
		checkSelectItem(this.timestamp);
		if (this.tags != null) {
			for (String tagKey : this.tags) {
				checkSelectItem(tagKey);
			}
		}
	}

	private void checkSelectItem(String item) {
		if (!this.selectItems.containsKey(item)) {
			log.error("metric config item[{}] not in select items, current select items:{}", item, this.selectItems);
			throw new RuntimeException(String.format("metric config item<%s> not in select clause item", item));
		}
	}

	private long getTimestamp(Map<String, Object> props) {
		// get timestamp from result event
		long timestamp = ObjectUtil.toLong(props.get(this.timestamp));
		if (timestamp == 0) {
			timestamp = System.currentTimeMillis();
		}
		return timestamp;
	}

	private String getParamValue(String key, Map<String, Object> props, Set<String> params) {
		String result = key;
		if (params == null || params.isEmpty()) {
			return result;
		}
		for (String param : params) {
			String paramStr = ObjectUtil.toString(props.get(param));
			result = result.replace("${" + param + "}", paramStr);
		}
		return result;
	}

	private Map<String, String> getTags(Map<String, Object> props) {
		if (this.tags == null || this.tags.length == 0) {
			// TODO: return null????
			return Collections.emptyMap();
		}
		Map<String, String> tags = new HashMap<>();
		for (String tagName : this.tags) {
			Object paramObj = props.get(tagName);
			// TODO: handle tag value(map)
			tags.put(tagName, ObjectUtil.toString(paramObj));
		}
		return tags;
	}

	private Map<String, Field> getFields(Map<String, Object> props) {
		Map<String, Field> result = null;
		for (int i = 0; i < this.fields.length; i++) {
			String fieldName = this.fields[i];
			Object obj = props.get(fieldName);
			if (obj == null) {
				continue;
			}
			if (result == null) {
				result = new HashMap<>();
			}
			// TODO: handle map fiels/Field
			SelectItem selectItem = this.selectItems.get(fieldName);
			result.put(fieldName, new Field(selectItem.getAggregateType(), ObjectUtil.toDouble(obj)));
		}
		return result;
	}

	private Set<String> getParams(String value) {
		Matcher namespaceMatcher = PARAM_PATTERN.matcher(value);
		Set<String> result = null;

		while (namespaceMatcher.find()) {
			if (result == null) {
				result = new HashSet<>();
			}
			result.add(namespaceMatcher.group(1));
		}
		return result;
	}
}
