package com.eht.common.util;

import java.util.Collection;
import java.util.Map;

public class CollectionUtil {
	/**
	 * 从map返回key的value，value为字符串类型，当map不包含key值的时候返回null
	 * @param map
	 * @param key
	 * @return
	 */
	public static String getString(Map<String,Object> map,String key){
		return map.get(key)!=null?String.valueOf(map.get(key)):null;
	}
	
	/**
	 * 当集合不为null，并且集合的size>0时，返回true，认为是有效的集合
	 * @param c
	 * @return
	 */
	public static boolean isValidateCollection(@SuppressWarnings("rawtypes") Collection c){
		return c!=null && !c.isEmpty();
	}
}
