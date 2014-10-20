package com.eht.common.enumeration;

/**
 * 同步过程中可用的ACTION
 * @author psychic
 *
 */
public enum DataSynchAction {
	/**
	 * 同步添加数据
	 */
	ADD,
	
	/**
	 * 同步更新数据
	 */
	UPDATE,
	
	/**
	 * 同步添加或更新数据
	 */
	CREATEORUPDATE,
	
	/**
	 * 同步还原数据
	 */
	RESTORE,
	
	/**
	 * 同步更新数据
	 */
	BAN,
	
	/**
	 * 同步删除数据
	 */
	DELETE,
	
	/**
	 * 同步发送
	 */
	SEND,
	
	/**
	 * 同步请求
	 */
	REQUEST,
	
	/**
	 * 同步下载文件
	 */
	DOWNLOAD,
	
	/**
	 * 同步上传文件
	 */
	UPLOAD,
	
	/**
	 * 同步成功
	 */
	SUCCESS,
	
	/**
	 * 同步下一数据类型
	 */
	NEXT,
	/**
	 * 同步完成
	 */
	FINISH
}
