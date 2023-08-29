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
package io.lindb.lingo.common.queue;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Util
 */
public class Util {

	private static final String illegalChars = "/" + '\u0000' + '\u0001' + "-" + '\u001F' + '\u007F' + "-" + '\u009F'
			+ '\uD800' + "-" + '\uF8FF' + '\uFFF0'
			+ "-" + '\uFFFF';
	private static final Pattern p = Pattern.compile("(^\\.{1,2}$)|[" + illegalChars + "]");

	public static void validate(String name) {
		if (name == null || name.length() == 0) {
			throw new IllegalArgumentException("folder name is emtpy");
		}
		if (name.length() > 255) {
			throw new IllegalArgumentException("folder name is too long");
		}
		if (p.matcher(name).find()) {
			throw new IllegalArgumentException("folder name [" + name + "] is illegal");
		}
	}

	public static boolean isFilenameValid(String file) {
		File f = new File(file);
		try {
			f.getCanonicalPath();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * mod by shift
	 * 
	 * @param val
	 * @param bits
	 * @return
	 */
	public static long mod(long val, int bits) {
		return val - ((val >> bits) << bits);
	}

	/**
	 * multiply by shift
	 * 
	 * @param val
	 * @param bits
	 * @return
	 */
	public static long mul(long val, int bits) {
		return val << bits;
	}

	/**
	 * divide by shift
	 * 
	 * @param val
	 * @param bits
	 * @return
	 */
	public static long div(long val, int bits) {
		return val >> bits;
	}

}
