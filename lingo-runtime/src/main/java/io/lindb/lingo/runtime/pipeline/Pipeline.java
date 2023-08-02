package io.lindb.lingo.runtime.pipeline;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import io.lindb.lingo.common.queue.QueueManager;
import io.lindb.lingo.runtime.pipeline.source.Source;
import io.lindb.lingo.runtime.pipeline.transform.TraceStore;
import io.lindb.lingo.runtime.utils.ReflectionUtil;
import io.lindb.lingo.storage.trace.TraceStorage;
import io.lindb.lingo.storage.trace.local.LocalTraceStorage;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Lazy
@Service
@ConfigurationProperties
@Log4j2
public class Pipeline {
	private static Map<String, ComponentDefine> components = new HashMap<>();
	static {
		Set<Class<?>> clazz = ReflectionUtil.getTypesAnnotatedWith(ComponentType.class);
		for (Class<?> aClass : clazz) {
			if (!Component.class.isAssignableFrom(aClass)) {
				continue;
			}
			ComponentType componentType = aClass.getAnnotation(ComponentType.class);
			try {
				BeanInfo beanInfo = Introspector.getBeanInfo(aClass);
				ComponentDefine define = new ComponentDefine();
				define.setClazz(aClass);
				define.setBeanInfo(beanInfo);
				components.put(componentType.type(), define);
			} catch (IntrospectionException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Setter
	@Getter
	private String name;
	@Setter
	private String dataDir;
	@Setter
	private Timer timer;
	@Setter
	private Map<String, Map<String, Object>> sources;
	@Setter
	private Map<String, Map<String, Object>> transforms;
	@Setter
	private Map<String, Map<String, Object>> sinks;

	private Set<String> componentNames = new HashSet<>();
	private Map<String, TraceStore> traceStores = new HashMap<>();

	private ApplicationContext ctx;

	@Bean
	public QueueManager queueManager() {
		return new QueueManager(this.dataDir + File.separator + this.name + File.separator + "queue");
	}

	@Bean
	public TraceStorage traceStorage() throws Exception {
		return new LocalTraceStorage(this.dataDir + File.separator + "trace-test");
	}

	public List<byte[]> getTrace(String traceId) throws Exception {
		// FIXME: fixme
		TraceStore traceStore = this.traceStores.get("trace_storage");
		return traceStore.getTrace(traceId);
	}

	public void run(ApplicationContext ctx) throws Exception {
		this.ctx = ctx;
		final BeanDefinitionRegistry registry = (BeanDefinitionRegistry) ctx.getAutowireCapableBeanFactory();
		registerComponent(sources, registry);
		registerComponent(transforms, registry);
		registerComponent(sinks, registry);

		// init all components
		for (String cmpName : this.componentNames) {
			Object cmp = ctx.getBean(cmpName);
			if (cmp instanceof ParallelComponent && !this.sources.containsKey(cmpName)) {
				ParallelComponent parallelCmp = (ParallelComponent) cmp;
				parallelCmp.startup();
			}

			// cache all trace stores
			if (cmp instanceof TraceStore) {
				TraceStore traceStore = (TraceStore) cmp;
				this.traceStores.put(cmpName, traceStore);
			}
		}

		if (this.timer != null && this.timer.isEnable()) {
			long interval = this.timer.getInterval();
			log.info("start timer for pipeline[{}], interval:{}ms", this.name, interval);
			ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
			scheduler.scheduleAtFixedRate(() -> {
				// FIXME:
				System.out.println("tick...");
			}, interval, interval, TimeUnit.MILLISECONDS);
		}

		// start source components
		for (String sourceName : this.sources.keySet()) {
			Source source = ctx.getBean(sourceName, Source.class);
			source.startup();
		}
	}

	public void shutdown() {
		// shutdown pipeline from source components
		for (String sourceName : this.sources.keySet()) {
			Object source = ctx.getBean(sourceName, Source.class);
			if (source instanceof Component) {
				// shutdown source component
				((Component) source).shutdown();
			}
		}
		log.info("shutdown pipeline [{}]", this.name);
	}

	private void registerComponent(Map<String, Map<String, Object>> componentConfigs, BeanDefinitionRegistry registry) {
		for (Map.Entry<String, Map<String, Object>> entry : componentConfigs.entrySet()) {
			final String cmpName = entry.getKey();
			if (this.componentNames.contains(cmpName)) {
				throw new RuntimeException("duplicate component name, name:" + cmpName);
			}

			final GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
			Map<String, Object> props = entry.getValue();
			ComponentDefine cmpDefine = components.get(props.get("type"));
			if (cmpDefine == null) {
				log.warn("component type not define, type: {}", props.get("type"));
				continue;
			}
			beanDefinition.setBeanClass(cmpDefine.getClazz());
			PropertyDescriptor[] pds = cmpDefine.getBeanInfo().getPropertyDescriptors();
			for (PropertyDescriptor pd : pds) {
				String fieldName = pd.getName();
				if (pd.getWriteMethod() != null) {
					if ("name".equals(fieldName)) {
						beanDefinition.getPropertyValues().addPropertyValue(fieldName, cmpName);
					} else {
						Object val = props.get(fieldName);
						if (val != null) {
							beanDefinition.getPropertyValues().addPropertyValue(fieldName, val);
						}
					}
				}
			}

			// set init input streams dependency method
			beanDefinition.setInitMethodName("initInputStreams");

			beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
			registry.registerBeanDefinition(cmpName, beanDefinition);
			// add define component for init/start it
			this.componentNames.add(cmpName);
		}
	}
}
