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
package io.lindb.lingo.runtime.utils;

import org.springframework.util.StringUtils;

/**
 * ObjectUtil
 */
public final class ObjectUtil {

	private ObjectUtil() {
	}

	public static String[] toStrings(Object[] values) {
		if (values == null || values.length == 0) {
			return null;
		}
		String[] result = new String[values.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = (String) values[i];
		}
		return result;
	}

	public static double toDouble(Object obj) {
		if (obj == null) {
			return 0;
		}
		if (obj instanceof Byte) {
			return ((Byte) obj).doubleValue();
		} else if (obj instanceof Short) {
			return ((Short) obj).doubleValue();
		} else if (obj instanceof Integer) {
			return ((Integer) obj).doubleValue();
		} else if (obj instanceof Float) {
			return ((Float) obj).doubleValue();
		} else if (obj instanceof Long) {
			return (long) obj;
		} else if (obj instanceof Double) {
			return (double) obj;
		} else {
			String v = toString(obj);
			if (!StringUtils.isEmpty(v)) {
				try {
					return Double.parseDouble(v);
				} catch (Exception ignore) {
				}
			}
		}
		return Double.NaN;
	}

	public static long toLong(Object obj) {
		if (obj == null) {
			return 0;
		}
		if (obj instanceof Byte) {
			return ((Byte) obj).longValue();
		} else if (obj instanceof Short) {
			return ((Short) obj).longValue();
		} else if (obj instanceof Integer) {
			return ((Integer) obj).longValue();
		} else if (obj instanceof Float) {
			return ((Float) obj).longValue();
		} else if (obj instanceof Long) {
			return (long) obj;
		} else if (obj instanceof Double) {
			return ((Double) obj).longValue();
		} else {
			String v = toString(obj);
			if (!StringUtils.isEmpty(v)) {
				try {
					return Long.parseLong(v);
				} catch (Exception ignore) {
				}
			}
		}
		return 0;
	}

	public static String toString(Object obj) {
		if (obj == null) {
			return null;
		}
		if (obj instanceof String) {
			return (String) obj;
		} else {
			return obj.toString();
		}
	}

}
