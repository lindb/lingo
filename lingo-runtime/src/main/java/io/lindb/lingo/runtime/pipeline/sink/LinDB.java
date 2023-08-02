package io.lindb.lingo.runtime.pipeline.sink;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import io.lindb.client.Client;
import io.lindb.client.ClientFactory;
import io.lindb.client.Options;
import io.lindb.client.api.Point;
import io.lindb.client.api.Point.Builder;
import io.lindb.client.api.Write;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import io.lindb.lingo.common.model.Field;
import io.lindb.lingo.common.model.Metric;
import io.lindb.lingo.runtime.pipeline.AbstractComponent;
import io.lindb.lingo.runtime.pipeline.ComponentType;

/**
 * LinDB
 */
@ComponentType(type = "lindb")
@Log4j2
public class LinDB extends AbstractComponent {
	@Setter
	private String endpoints;
	@Setter
	private String database;
	@Setter
	private boolean gzip;
	// TODO: add common tags config

	private Write write;
	private Client client;

	@PostConstruct
	public void initialize() throws IOException {
		Options options = Options.builder()
				.useGZip(this.gzip).batchSize(5).flushInterval(1000)
				.build();
		log.info("init lindb client, endpoints:{}, database:{}", this.endpoints, this.database);
		// create LinDB Client with broker endpoint
		this.client = ClientFactory.create(this.endpoints, options);
		// get write for database
		this.write = client.write(this.database);
		log.info("lindb client initialized, endpoints:{}, database:{}", this.endpoints, this.database);
	}

	@PreDestroy
	public void destory() {
		try {
			this.write.close();
		} catch (Exception e) {
			log.error("close lindb write error", e);
		}
		try {
			this.client.close();
		} catch (Exception e) {
			log.error("close lindb client error", e);
		}
	}

	@Override
	public void handle(Object key, Object event) {
		if (event instanceof List) {
			List events = (List) event;
			for (Object e : events) {
				if (e instanceof Metric) {
					Metric metric = (Metric) e;
					Builder builder = Point.builder(metric.getName(), metric.getTimestamp())
							.namespace(metric.getNamespace())
							.addTags(metric.getTags());
					Map<String, Field> fields = metric.getFields();
					for (Map.Entry<String, Field> entry : fields.entrySet()) {
						Field field = entry.getValue();
						String fieldName = entry.getKey();
						switch (field.getAggregateType()) {
							case Sum:
								builder.addSum(fieldName, field.getValue());
								break;
							case Min:
								builder.addMin(fieldName, field.getValue());
								break;
							case Max:
								builder.addMax(fieldName, field.getValue());
								break;
							case First:
								builder.addFirst(fieldName, field.getValue());
								break;
							case Last:
								builder.addLast(fieldName, field.getValue());
								break;
							default:
								break;
						}
					}
					Point point = builder.build();
					write.put(point);
				}
			}
		}
	}
}
