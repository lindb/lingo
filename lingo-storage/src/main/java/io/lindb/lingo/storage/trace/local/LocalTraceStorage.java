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
package io.lindb.lingo.storage.trace.local;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.rocksdb.Options;
import org.rocksdb.RocksDB;

import io.lindb.lingo.storage.trace.TraceStorage;
import lombok.extern.log4j.Log4j2;

/**
 * LocalTraceStorage
 */
@Log4j2
public class LocalTraceStorage implements TraceStorage {
	static {
		RocksDB.loadLibrary();
	}

	private final RocksDB db;
	private final Options options;
	private final AtomicBoolean running;

	public LocalTraceStorage(String dir) throws Exception {
		this.options = new Options().setCreateIfMissing(true).setMergeOperatorName("stringappend");
		this.db = RocksDB.open(options, dir);
		this.running = new AtomicBoolean(true);
	}

	public void close() throws IOException {
		if (this.running.compareAndSet(true, false)) {
			if (this.db != null) {
				this.db.close();
			}
			this.options.close();
			log.info("local trace storage closed");
		}
	}

	public void save(Map<String, Long> traces) throws Exception {
		for (Map.Entry<String, Long> entry : traces.entrySet()) {
			log.info("store trace [{}]", entry.getKey());
			this.db.merge(entry.getKey().getBytes(), longToBytes(entry.getValue()));
		}
	}

	public List<Long> getTrace(String traceId) throws Exception {
		byte[] msgId = this.db.get(traceId.getBytes());
		if (msgId == null) {
			return null;
		}
		List<Long> ids = new ArrayList<>();
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		if (msgId.length > 8) {
			int pos = 0;
			while (pos < msgId.length) {
				buffer.put(msgId, pos, 8);
				buffer.flip();// read
				long d = buffer.getLong();
				buffer.flip(); // write
				ids.add(d);
				pos += 9;
			}
		} else {
			buffer.put(msgId);
			buffer.flip();
			ids.add(buffer.getLong());
		}
		return ids;
	}

	public byte[] longToBytes(long x) {
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.putLong(x);
		return buffer.array();
	}

}
