package com.eht.common.enumeration;

public enum DataType {
	/**
	 * 同步数据类别：专题
	 */
	SUBJECT,
	
	/**
	 * 同步数据类别：目录
	 */
	DIRECTORY,
	
	/**
	 * 同步数据类别：客户端
	 */
	CLIENT,
	
	/**
	 * 同步数据类别：用户
	 */
	USER,
	
	/**
	 * 同步数据类别：角色
	 */
	ROLE,
	
	/**
	 * 同步数据类别：资源
	 */
	RESOURCE,
	
	/**
	 * 同步数据类别：资源操作
	 */
	RESOURCEACTION,
	
	/**
	 * 同步数据类别：资源权限
	 */
	RESOURCEPERMISSION,
	
	
	/**
	 * 同步数据类别：标签
	 */
	TAG,
	
	/**
	 * 同步数据类别：模板
	 */
	TEMPLATE,
	
	/**
	 * 同步数据类别：评论
	 */
	COMMENT,
	
	/**
	 * 同步数据类别：条目
	 */
	NOTE,
	
	/**
	 * 同步数据类别：条目黑名单
	 */
	NOTEBLACK,
	
	/**
	 * 同步数据类别：条目标签
	 */
	NOTETAG,
	
	/**
	 * 同步数据类别：目录黑名单
	 */
	DIRECTORYBLACK,
	
	/**
	 * 同步数据类别：专题成员
	 */
	SUBJECTUSER,
	
	/**
	 * 同步数据类别：附件元数据
	 */
	ATTACHMENT,
	
	/**
	 * 同步数据类别：文件
	 */
	FILE,
	
	/**
	 * 同步数据类别：所有类型
	 */
	ALL,
	
	/**
	 * 同步数据类别：批量删除数据时使用的类型
	 */
	BATCHDATA,
	
	/**
	 * 同步数据类别：批量TRUNCATE数据时使用的类型
	 */
	TRUNCDATA
}
