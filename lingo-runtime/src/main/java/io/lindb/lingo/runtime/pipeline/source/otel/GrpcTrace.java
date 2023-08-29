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
