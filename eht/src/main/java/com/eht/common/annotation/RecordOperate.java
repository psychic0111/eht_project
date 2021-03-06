/**
 * 
 */
package com.eht.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.eht.common.enumeration.DataSynchAction;
import com.eht.common.enumeration.DataType;

/**
 * 同步日志记录注解, 默认值只对数组长度为1时有用, 所以大于1时还是都要设置
 * @author chenlong
 * 
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RecordOperate {
	/**
	 *  数据类型 : 同步数据分类
	 *  客户端	CLIENT
	 *	用户	USER
	 *	角色	ROLE
	 *	标签	TAG
	 *	条目标签	NOTETAG
	 *	模板	TEMPLATE
	 *	评论	COMMENT
	 *	专题	SUBJECT
	 *	目录	DIRECTORY
	 *	条目	NOTE
	 *	条目黑名单	NOTEBLACK
	 *	目录黑名单	DIRECTORYBLACK
	 *	专题成员	SUBJECTUSER
	 *	附件	ATTACHMENT
	 * @return
	 */
	DataType[] dataClass();
	/**
	 * 操作类型: A-添加 U-更新 D-删除
	 */
	DataSynchAction[] action();
	/**
	 * 数据主键位于方法参数的索引, 默认为0
	 */
	int[] keyIndex() default 0;
	
	/**
	 * 在实体中获取数据主键的方法名
	 */
	String[] keyMethod() default "";
	/**
	 * 在实体中时间戳的属性名
	 */
	String[] timeStamp() default "";
	/**
	 * 日志影响用户ID获取, 在方法参数中的位置
	 * @return
	 */
	int[] targetUser() default -1;
}
