package com.eht.common.util;

import java.util.UUID;

public class UUIDGenerator {
	
	public static String uuid(){
		return UUID.randomUUID().toString().replace("-", "").toUpperCase();
	}
}
