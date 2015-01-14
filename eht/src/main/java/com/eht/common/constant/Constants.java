package com.eht.common.constant;

public interface Constants {
	/**
	 * 用户session属性名称
	 */
	public static final String SESSION_USER_ATTRIBUTE = "SESSION_USER_ATTRIBUTE";
	
	/**
	 * 专题类型session属性名称
	 */
	public static final String SESSION_USER_SUBJECTTYPE = "SESSION_USER_SUBJECTTYPE";
	/**
	 * 用户登录帐号字段名称
	 */
	public static final String PARAMETER_USERNAME = "userName";
	/**
	 * 用户登录密码字段名称
	 */
	public static final String PARAMETER_PASSWORD = "password";
	/**
	 * 专题模块名称
	 */
	public static final String SUBJECT_MODULE_NAME = "SubjectManage";
	/**
	 * 专题文档资料目录名称
	 */
	public static final String SUBJECT_DOCUMENT_DIRNAME = "文档资料";
	/**
	 * 专题文档资料目录名称
	 */
	public static final String SUBJECT_TAG_DIRNAME = "专题标签";
	/**
	 * 回收站名称
	 */
	public static final String RECYCLE_NODE_NAME = "回收站";
	/**
	 * 专题模板
	 */
	public static final int TEMPLATE_SUBJECT_CLASSIFY = 1;
	
	/**
	 * 条目模板
	 */
	public static final int TEMPLATE_NOTE_CLASSIFY = 2;
	
	/**
	 * 系统内置条目模板
	 */
	public static final int TEMPLATE_SYSTEM_DEFAULT = 0;
	
	/**
	 * 用户自定义条目模板
	 */
	public static final int TEMPLATE_USER_DEFINED = 1;
	
	/**
	 * 个人专题
	 */
	public static final int SUBJECT_TYPE_P = 1;
	
	/**
	 * 多人专题
	 */
	public static final int SUBJECT_TYPE_M = 2;
	
	/**
	 * 个人专题树节点ID
	 */
	public static final String SUBJECT_PID_P = "-1";
	
	/**
	 * 多人专题树节点ID
	 */
	public static final String SUBJECT_PID_M = "-2";
	
	/**
	 * 消息中心树节点ID
	 */
	public static final String MSG_NODEID_R = "-100";
	
	/**
	 * 系统消息树节点ID
	 */
	public static final String MSG_NODEID_SYS = "-101";
	/**
	 * 未读消息树节点ID
	 */
	public static final String MSG_NODEID_NR = "-102";
	/**
	 * 用户消息树节点ID
	 */
	public static final String MSG_NODEID_U = "-103";
	/**
	 * 系统消息类型
	 */
	public static final int MSG_SYSTEM_TYPE = 1;
	/**
	 * 用户消息类型
	 */
	public static final int MSG_USER_TYPE = 2;
	/**
	 * 已读状态
	 */
	public static final Integer READED_OBJECT = 1;
	
	/**
	 * 未读状态
	 */
	public static final Integer NOT_READ_OBJECT = 0;
	
	/**
	 * 在专题下或者在回收站都不能看到的数据
	 */
	public static final int DATA_NOTSEARCH = 2;
	/**
	 * 删除标记
	 */
	public static final int DATA_DELETED = 1;
	/**
	 * 未删除标记
	 */
	public static final int DATA_NOT_DELETED = 0;
	/**
	 * 文件传输完成
	 */
	public static final int FILE_TRANS_COMPLETED = 1;
	/**
	 * 文件传输未完成
	 */
	public static final int FILE_TRANS_NOT_COMPLETED = 0;
	/**
	 * 文件传输临时路径
	 */
	public static final String PATH = "/TEMP/";
	
	/**
	 * 文件传输路径
	 */
	public static final String ATTACHMENTPATH = "/UPLOAD/";
	
	/**
	 * 启用
	 */
	public static final int ENABLED=0;
	
	/**
	 * 禁用
	 */
	public static final int DISABLED=1;
	
	/**
	 * 状态值：假、不可用、未完成。。。
	 */
	public static final int STATUS_FALSE = 0;
	
	/**
	 * 状态值：真、可用、完成。。。
	 */
	public static final int STATUS_TRUE = 1;
	
	/**
	 * 未激活
	 */
	public static final int ACTIVATE=3;
	
	/**
	 * 表格列表页记录每页数量
	 */
	public static final int PER_PAGE_COUNT = 20;
	
	/**
	 * 附件类型：条目、目录附件
	 */
	public static final int FILE_TYPE_NORMAL = 1;
	
	/**
	 * 附件类型：条目内容中的图片
	 */
	public static final int FILE_TYPE_IMAGE = 2;
	
	/**
	 * 附件类型：条目内容中的css
	 */
	public static final int FILE_TYPE_CSS = 3;
	
	/**
	 * 附件类型：条目内容中的js
	 */
	public static final int FILE_TYPE_JS = 4;
	
	/**
	 * 附件类型：条目zip文件
	 */
	public static final int FILE_TYPE_NOTEHTML = 5;
	
	/**
	 * 附件存储时的后缀名
	 */
	public static final String ATTACHMENT_SUFFIX = ".atm";
}
