package io.lindb.lingo.storage.trace;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface TraceStorage {

	void close() throws IOException;

	void save(Map<String, Long> traces) throws Exception;

	List<Long> getTrace(String traceId) throws Exception;
}
