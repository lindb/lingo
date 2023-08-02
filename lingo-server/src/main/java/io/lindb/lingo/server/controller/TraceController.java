package io.lindb.lingo.server.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.lindb.lingo.common.model.Trace;
import io.lindb.lingo.common.translator.OTLPToLingo;
import io.lindb.lingo.runtime.pipeline.Pipeline;
import io.lindb.lingo.runtime.pipeline.PipelineManager;
import io.opentelemetry.proto.trace.v1.TracesData;
import lombok.extern.log4j.Log4j2;

/**
 * TraceController
 */
@RestController
@RequestMapping("/trace")
@Log4j2
public class TraceController {

	@Autowired
	private PipelineManager pipelineMgr;

	@GetMapping()
	public List<Trace> getTraceById(@RequestParam("pipeline") String pipelineName,
			@RequestParam("traceId") String traceId)
			throws Exception {
		Pipeline pipeline = this.pipelineMgr.getPipelineByName(pipelineName);
		if (pipeline == null) {
			log.warn("pipeline not found, name [{}]", pipelineName);
			return null;
		}
		List<byte[]> data = pipeline.getTrace(traceId);
		if (data == null) {
			log.warn("trace data not found, pipeline [{}], trace [{}]", pipelineName, traceId);
			return null;
		}
		List<Trace> traces = new ArrayList<>();
		for (byte[] traceData : data) {
			TracesData trace = TracesData.parseFrom(traceData);
			List<Trace> traceList = OTLPToLingo.toTrace(trace);
			if (traceList != null) {
				traces.addAll(traceList);
			}
		}
		return traces;
	}
}
