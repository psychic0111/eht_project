package com.eht.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.eht.common.constant.SynchConstants;

/**
 * 同步控制
 * @author psychic
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SynchControl {
	/**
	 * 客户端本次操作类型
	 */
	String action() default SynchConstants.DATA_SYNCH_SUCCESS;
	/**
	 * 客户端本次操作数据类型
	 */
	String dataType();
	
	/**
	 * 客户端下一步操作类型
	 */
	String nextAction() default SynchConstants.CLIENT_SYNCH_SEND;
	/**
	 * 客户端下一步操作数据类型
	 */
	String nextDataType();
	
	/**
	 * 数据主键
	 */
	String UUID() default "";
}
