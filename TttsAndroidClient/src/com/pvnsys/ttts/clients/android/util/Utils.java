package com.pvnsys.ttts.clients.android.util;

import org.apache.commons.lang3.StringUtils;

public class Utils {

	public static String rPad(String s, int size) {
//		return String.format("%1$-" + size + "s", s);
		return StringUtils.rightPad(s, size);
	}

	public static String lPad(String s, int size) {
//		return String.format("%-$" + size + "s", s);
		return StringUtils.leftPad(s, size);
	}
	
}
