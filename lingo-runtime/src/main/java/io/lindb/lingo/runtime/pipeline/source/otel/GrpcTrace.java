package io.lindb.lingo.runtime.pipeline.source.otel;

import java.util.Collections;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.config.GrpcServerProperties;
import net.devh.boot.grpc.server.serverfactory.NettyGrpcServerFactory;
import net.devh.boot.grpc.server.service.AnnotationGrpcServiceDiscoverer;
import net.devh.boot.grpc.server.service.GrpcServiceDefinition;
import io.lindb.lingo.common.queue.FanOutQueue;
import io.lindb.lingo.common.queue.QueueManager;
import io.lindb.lingo.runtime.pipeline.AbstractComponent;
import io.lindb.lingo.runtime.pipeline.ComponentType;
import io.lindb.lingo.runtime.pipeline.source.Source;

/**
 * OTELGrpcSource
 */
@ComponentType(type = "otel")
@Log4j2
public class GrpcTrace extends AbstractComponent implements Source {
	@Setter
	private int port;
	@Setter
	private String diskQueue;
	@Autowired
	@Setter
	private QueueManager queueManager;
	@Autowired
	private TraceService traceService;

	private FanOutQueue queue;

	@PostConstruct
	public void initialize() throws Exception {
		GrpcServerProperties props = new GrpcServerProperties();
		props.setPort(this.port);
		AnnotationGrpcServiceDiscoverer serviceDiscoverer = new AnnotationGrpcServiceDiscoverer();
		serviceDiscoverer.setApplicationContext(this.context);
		final NettyGrpcServerFactory factory = new NettyGrpcServerFactory(props, Collections.emptyList());
		for (final GrpcServiceDefinition service : serviceDiscoverer.findGrpcServices()) {
			factory.addService(service);
		}
		factory.createServer().start();
		log.info("otel grpc server started, port:{}", this.port);

		this.queue = this.queueManager.getOrCreate(diskQueue);
		this.traceService.setQueue(queue);
	}

	@Override
	public void startup() throws Exception {
		// TODO Auto-generated method stub
	}
}
