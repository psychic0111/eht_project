package com.eht.common.util;

import java.util.regex.Pattern;


public class StringUtil {
	/**
	 * 不为null并且长度大于0的字符串为有效字符串
	 * @param str
	 * @return
	 */
	public static boolean isValidateString(String str){
		return str!=null && !str.isEmpty();
	}
	
	public static String encode2HtmlUnicode(String str) {
		if (str == null)
			return "";

		StringBuilder sb = new StringBuilder(str.length() * 2);
		for (int i = 0; i < str.length(); i++) {
			sb.append(encode2HtmlUnicode(str.charAt(i)));
		}
		return sb.toString();
	}
	
	public static String encode2HtmlUnicode(char character) {
		if (character > 255) {
			return "&#" + (character & 0xffff) + ";";
		} else {
			return String.valueOf(character);
		}
	}
	
	public static String encode2HtmlUnicode(Character character) {
		if (character == null)
			return null;
		return encode2HtmlUnicode(character.charValue());
	}
		    
	public static void encode2HtmlUnicode(String[] value) {
		if (value == null || value.length < 1)
			return;

		for (int i = 0; i < value.length; i++) {
			value[i] = encode2HtmlUnicode(value[i]);
		}
	}
	
	/**
	 * SQL注入
	 */
	public static boolean SQLInjection(String str){
		String reg = "(?:')|(?:--)|(/\\*(?:.|[\\n\\r])*?\\*/)|"
				   + "(\\b(select|update|and|or|delete|insert|trancate|char|into|substr|ascii|declare|exec|count|master|into|drop|execute)\\b)";
		Pattern sqlPattern = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
		if (sqlPattern.matcher(str).find()) {  
	          
	        return true;  
	    }  
	    return false;
	}
	
	public static void main(String[] args){
		System.out.println(SQLInjection("%E5%8F%91%E9%80%81%20a/**/n/**/d%202734%3d2735"));
	}
}
