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
