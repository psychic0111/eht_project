package com.eht.common.constant;

public class SynchConstants {
	
	/**
	 * 一次返回客户端日志数量
	 */
	public static final int RETURN_CLIENT_MAX_COUNT = 1;
	/**
	 * 返回客户端header中dataType的name值
	 */
	public static final String HEADER_DATATYPE = "DataType";
	
	/**
	 * 返回客户端header中action的name值
	 */
	public static final String HEADER_ACTION = "Action";
	
	/**
	 * 返回客户端header中nextdataType的name值
	 */
	public static final String HEADER_NEXT_DATATYPE = "NextDataType";
	
	/**
	 * 返回客户端header中nextaction的name值
	 */
	public static final String HEADER_NEXT_ACTION = "NextAction";
	
	/**
	 * 返回客户端header中UUID的name值
	 */
	public static final String HEADER_TIMESTAMP = "ServerTimeStamp";
	
	/**
	 * 同步数据类别：专题
	 */
	public static final String DATA_CLASS_SUBJECT = "SUBJECT";
	/**
	 * 同步数据类别：目录
	 */
	public static final String DATA_CLASS_DIRECTORY = "DIRECTORY";
	/**
	 * 同步数据类别：客户端
	 */
	public static final String DATA_CLASS_CLIENT = "CLIENT";
	/**
	 * 同步数据类别：用户
	 */
	public static final String DATA_CLASS_USER = "USER";
	/**
	 * 同步数据类别：角色
	 */
	public static final String DATA_CLASS_ROLE = "ROLE";
	/**
	 * 同步数据类别：标签
	 */
	public static final String DATA_CLASS_TAG = "TAG";
	/**
	 * 同步数据类别：模板
	 */
	public static final String DATA_CLASS_TEMPLATE = "TEMPLATE";
	/**
	 * 同步数据类别：评论
	 */
	public static final String DATA_CLASS_COMMENT = "COMMENT";
	/**
	 * 同步数据类别：条目
	 */
	public static final String DATA_CLASS_NOTE = "NOTE";
	/**
	 * 同步数据类别：条目黑名单
	 */
	public static final String DATA_CLASS_NOTEBLACK = "NOTEBLACK";
	/**
	 * 同步数据类别：目录黑名单
	 */
	public static final String DATA_CLASS_DIRECTORYBLACK = "DIRECTORYBLACK";
	/**
	 * 同步数据类别：专题成员
	 */
	public static final String DATA_CLASS_SUBJECTUSER = "SUBJECTUSER";
	/**
	 * 同步数据类别：附件
	 */
	public static final String DATA_CLASS_ATTACHMENT = "ATTACHMENT";
	
	/**
	 * 上传数据类别：文件
	 */
	public static final String DATA_CLASS_FILE = "FILE";
	
	/**
	 * 同步数据类别：所有类型，首次请求服务器数据时使用
	 */
	public static final String DATA_CLASS_ALL = "ALL";
	/**
	 * 同步下一条数据
	 */
	public static final String DATA_SYNCH_NEXT = "NEXT";
	
	/**
	 * 同步添加数据
	 */
	public static final String DATA_OPERATE_ADD = "ADD";
	/**
	 * 同步更新数据
	 */
	public static final String DATA_OPERATE_UPDATE = "UPDATE";
	/**
	 * 同步删除数据
	 */
	public static final String DATA_OPERATE_DELETE = "DELETE";
	
	/**
	 * 单次同步数据完成
	 */
	public static final String DATA_SYNCH_SUCCESS = "SUCCESS";
	
	/**
	 * 同步数据全部完成
	 */
	public static final String DATA_SYNCH_FINISHED = "FINISH";
	
	/**
	 * 客户端请求同步数据
	 */
	public static final String CLIENT_SYNCH_REQUEST = "REQUEST";
	
	/**
	 * 客户端发送同步数据
	 */
	public static final String CLIENT_SYNCH_SEND = "SEND";
	
	/**
	 * 客户端下载文件数据
	 */
	public static final String CLIENT_SYNCH_DOWNLOAD = "DOWNLOAD";
	
	/**
	 * 客户端上传文件数据
	 */
	public static final String CLIENT_SYNCH_UPLOAD = "UPLOAD";
	
	/**
	 * WEB程序客户端ID
	 */
	public static final String CLIENT_DEFAULT_ID = "WEB";
	
	/**
	 * WEB程序客户端类型
	 */
	public static final String CLIENT_DEFAULT_TYPE = "WEB";
	
	/**
	 * 日志同步状态--未同步
	 */
	public static final int LOG_NOT_SYNCHRONIZED = 0;
	
	/**
	 * 日志同步状态--已同步
	 */
	public static final int LOG_SYNCHRONIZED = 1;
	
	public static final String HEADER_CLIENT_ID = "clientid";
	
	public static final String HEADER_HOST_DATATYPE = "HostDataType";
	
	public static final String HEADER_HOST_UUID = "HostUUID";
}
