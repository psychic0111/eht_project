package com.eht.webservice.service;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.htmlparser.util.ParserException;

import com.eht.common.constant.SynchConstants;

@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN, MediaType.APPLICATION_FORM_URLENCODED, MediaType.MULTIPART_FORM_DATA})
public interface DataSynchizeService {
	
	/**
	 * 客户端注册
	 * @param clientType 客户端类型
	 * @return	客户端标识
	 */
	public String registerClient(@PathParam("clientType") String clientType, @Context HttpServletResponse res);

	/**
	 * 删除客户端
	 * @param clientId
	 * @return
	 */
	public String deleteClient(@PathParam("clientId") String clientId);
	
	/**
	 * 修改用户密码
	 * @param oldPassword
	 * @param password
	 * @return
	 */
	public String updatePassword(@PathParam("userId") String userId, @FormParam("oldPassword") String oldPassword, @FormParam("password") String password);
	
	/**
	 * 修改用户信息
	 * @param data
	 * @return
	 */
	public String updateUser(@FormParam("data") String data);
	
	/**
	 * 添加专题
	 * @param data	         专题数据
	 * @return
	 */
	public String addSubject(@FormParam("data") String data, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res);
	/**
	 * 更新专题
	 * @param data	         专题数据
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws Exception 
	 */
	public String updateSubject(@FormParam("data") String data, @PathParam("timeStamp") long timeStamp, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) throws IllegalAccessException, InvocationTargetException, Exception;
	
	/**
	 * 添加或修改专题
	 * @param data	         专题数据
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 */
	public String addOrUpdateSubject(@FormParam("data") String data, @Context HttpServletResponse res) throws IllegalAccessException, InvocationTargetException;
	
	/**
	 * 删除专题
	 * @param id	   专题ID
	 * @return
	 */
	public String deleteSubject(@PathParam("id") String id, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res);
	
	/**
	 * 客户端获取要同步的数据
	 * @param clientId	客户端标识
	 * @param userId	用户ID
	 * @param timeStamp	上次同步时间戳
	 * @return
	 * @throws Exception 
	 */
	public String getDeleteLogs(@FormParam("data") String data, @HeaderParam(SynchConstants.HEADER_CLIENT_ID) String clientId, @PathParam("timeStamp") long timeStamp, String endTimeStr, @DefaultValue(SynchConstants.DATA_CLASS_ALL) @HeaderParam(SynchConstants.HEADER_DATATYPE) String dataClass, @DefaultValue(SynchConstants.CLIENT_SYNCH_REQUEST) @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) throws Exception;
	
	/**
	 * 客户端获取要同步的数据数量
	 * @param clientId	客户端标识
	 * @param userId	用户ID
	 * @param timeStamp	上次同步时间戳
	 * @return
	 */
	public int countSynchData(String clientId, String userId, String timeStamp, @Context HttpServletResponse res);
	
	/**
	 * 添加目录
	 * @param data	         目录数据
	 * @return
	 */
	public String addDirectory(@FormParam("data") String data, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res);
	
	/**
	 * 更新专题
	 * @param data	         目录数据
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws Exception 
	 */
	public String updateDirectory(@FormParam("data") String data, @PathParam("timeStamp") long timeStamp, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) throws IllegalAccessException, InvocationTargetException, Exception;
	
	/**
	 * 添加或更新专题
	 * @param data	         目录数据
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 */
	public String addOrUpdateDirectory(@FormParam("data") String data, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) throws IllegalAccessException, InvocationTargetException;
	
	/**
	 * 删除目录
	 * @param clientId	客户端标识
	 * @param id		目录ID
	 * @return
	 */
	public String deleteDirectory(@PathParam("id") String id, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res);
	
	/**
	 * 将用户添加到目录黑名单
	 * @param clientId		客户端标识
	 * @param directoryId	目录ID
	 * @param userId		用户ID
	 * @return
	 */
	public String addDirectoryBlack(@PathParam("directoryId") String directoryId, @PathParam("userId") String userId, @Context HttpServletResponse res);
	/**
	 * 将用户从目录黑名单移除
	 * @param clientId		客户端标识
	 * @param directoryId	目录ID
	 * @param userId		用户ID
	 * @return
	 */
	public String deleteDirectoryBlack(@PathParam("directoryId") String directoryId, @PathParam("userId") String userId, @Context HttpServletResponse res);
	
	/**
	 * 添加条目
	 * @param data		条目数据
	 * @return
	 */
	public String addNote(@FormParam("data") String data, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res);
	/**
	 * 更新条目
	 * @param updateContent  是否更新内容, true or false默认为true
	 * @param data 		条目数据
	 * @return 更新结果信息
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws Exception 
	 */
	public String updateNote(@FormParam("data") String data, @PathParam("timeStamp") long timeStamp, @DefaultValue("true") @QueryParam("updateContent") boolean updateContent, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) throws IllegalAccessException, InvocationTargetException, Exception;
	
	/**
	 * 添加或更新条目
	 * @param data		条目数据
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 */
	public String addOrUpdateNote(@FormParam("data") String data, @DefaultValue("true") @FormParam("updateContent") boolean updateContent, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) throws IllegalAccessException, InvocationTargetException;
	
	/**
	 * 更新条目内容
	 * @param content	条目内容
	 * @return 更新结果信息
	 */
	public String updateNoteContent(@PathParam("id") String id, @FormParam("content") String content, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res);
	
	/**
	 * 删除条目
	 * @param id		条目ID
	 * @return
	 */
	public String deleteNote(@PathParam("id") String id, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res);
	
	/**
	 * 将用户添加到条目黑名单
	 * @param directoryId	条目ID
	 * @param userId		用户ID
	 * @return
	 */
	public String addNoteBlack(@PathParam("noteId") String noteId, @PathParam("userId") String userId, @Context HttpServletResponse res);
	
	/**
	 * 将用户添加到条目黑名单
	 * @param directoryId	条目ID
	 * @param userId		用户ID
	 * @return
	 */
	public String deleteNoteBlack(@PathParam("noteId") String noteId, @PathParam("userId") String userId, @Context HttpServletResponse res);
	
	/**
	 * 添加附件到数据库表
	 * @param data		附件数据
	 * @return
	 * @throws ParserException 
	 */
	public String addAttachment(@FormParam("data") String data, @PathParam("timeStamp") long timeStamp, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) throws ParserException;
	
	/**
	 * 表中删除附件
	 * @param id		附件ID
	 * @return
	 */
	public String deleteAttachment(@PathParam("id") String id, @PathParam("timeStamp") long timeStamp, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res);
	
	/**
	 * 上传文件
	 * @param attachmentId 文件ID
	 * @param flag  	         文件数据是否全部传输过来
	 * @param request 
	 * @return
	 * @throws IOException 
	 */
	public String uploadAttachment(@PathParam("attachmentId") String attachmentId, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletRequest request, @Context HttpServletResponse res) throws IOException;
	
	/**
	 * 上传文件
	 * @param attachmentId 文件ID
	 * @param flag  	         文件数据是否全部传输过来
	 * @param request 
	 * @return
	 * @throws IOException 
	 */
	public String resumeUploadAttachment(@PathParam("attachmentId") String attachmentId, @DefaultValue("1") @PathParam("flag") int flag, @Context HttpServletRequest request, @Context HttpServletResponse res) throws IOException;
	
	/**
	 * 批量上传文件
	 * @param attachmentId  附件ID
	 * @param request
	 * @return
	 * @throws IOException 
	 */
	public String uploadAttachmentBatch(@FormParam("attachmentId") String[] attachmentId, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletRequest request, @Context HttpServletResponse res) throws IOException;
	
	/**
	 * 下载文件
	 * @param attachmentId 文件ID
	 * @return
	 * @throws Exception 
	 */
	public String downloadAttachment(@HeaderParam(SynchConstants.HEADER_CLIENT_ID) String clientId, @PathParam("attachmentId") String attachmentId, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse response) throws Exception;
	
	/**
	 * 批量下载文件
	 * @param attachmentId 文件ID
	 * @return
	 */
	public String downloadAttachmentBatch(@FormParam("attachmentId") String[] attachmentId, @Context HttpServletResponse res);
	
	/**
	 * 添加标签
	 * @param data
	 * @return
	 */
	public String addTag(@FormParam("data") String data, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res);
	
	/**
	 * 修改标签
	 * @param data
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws Exception 
	 */
	public String updateTag(@FormParam("data") String data, @PathParam("timeStamp") long timeStamp, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) throws IllegalAccessException, InvocationTargetException, Exception;
	
	/**
	 * 添加或修改标签
	 * @param data
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 */
	public String addOrUpdateTag(@FormParam("data") String data, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) throws IllegalAccessException, InvocationTargetException;
	
	/**
	 * 删除标签
	 * @param id
	 * @return
	 */
	public String deleteTag(@PathParam("id") String id, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res);
	
	/**
	 * 添加或修改模板
	 * @param data
	 * @return
	 */
	public String addTemplate(@FormParam("data") String data, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res);
	
	/**
	 * 添加评论
	 * @param data
	 * @return
	 */
	public String addComment(@FormParam("data") String data, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res);
	
	/**
	 * 删除评论
	 * @param id
	 * @return
	 */
	public String deleteComment(@PathParam("id") String id, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res);
	
	/**
	 * 添加修改专题成员或角色
	 * @param subjectId
	 * @param userId
	 * @param roleId
	 * @return
	 */
	public String addSubjectMember(String data, String action, @Context HttpServletResponse res);
	
	/**
	 * 删除专题成员
	 * @param subjectId
	 * @param userId
	 * @return
	 */
	public String deleteSubjectMember(@PathParam("id") String id, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res);
	
	/**
	 * 获取服务器时间
	 * @return
	 */
	public long getServerTime();
	
	/**
	 * 上传图片类型附件
	 * @param noteId
	 * @param imgId
	 * @param fileName
	 * @param request
	 * @return
	 * @throws IOException
	 * @throws ParserException
	 */
	public String uploadNoteFile(String noteId, String action, HttpServletRequest request, HttpServletResponse res) throws IOException, ParserException;
	
	/**
	 * 按照操作和数据类型顺序查询用户需同步日志
	 * @param clientId
	 * @param userId
	 * @param timeStamp
	 * @param offset
	 * @return
	 * @throws Exception 
	 */
	public String getSynchDataByStep(@HeaderParam("clientId") String clientId, @PathParam("timeStamp") long timeStamp, String endTimeStr, @DefaultValue(SynchConstants.DATA_CLASS_ALL) @HeaderParam(SynchConstants.HEADER_DATATYPE) String dataClass, @DefaultValue(SynchConstants.CLIENT_SYNCH_REQUEST) @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) throws Exception;

	/**
	 * 
	 * @param clientId
	 * @param userId
	 * @param timeStamp
	 * @param logId
	 * @return
	 * @throws Exception 
	 * @throws NumberFormatException 
	 */
	public String getSynchDataByLogId(@PathParam("logId") String logId, @Context HttpServletResponse res) throws NumberFormatException, Exception;
	
	/**
	 * 根据ID获取用户信息
	 * @param id
	 * @return
	 */
	public String getUserInfo(@PathParam("id") String id);

	/**
	 * 获取专题成员日志
	 * @param subjectId
	 * @param clientId
	 * @param userId
	 * @param timeStamp
	 * @return
	 */
	public String getSubjectUser(@PathParam("subjectId") String subjectId, @PathParam("clientId") String clientId, @PathParam("userId") String userId, @PathParam("timeStamp") long timeStamp, @Context HttpServletResponse res);
	
	/**
	 * 获取条目附件日志
	 * @param noteId
	 * @param clientId
	 * @param userId
	 * @param timeStamp
	 * @return
	 */
	public String getNoteAttachment(@PathParam("noteId") String noteId, @PathParam("clientId") String clientId, @PathParam("userId") String userId, @PathParam("timeStamp") long timeStamp, @Context HttpServletResponse res);

	/**
	 * 获取专题下数据日志
	 * @param subjectId
	 * @param clientId
	 * @param userId
	 * @param timeStamp
	 * @return
	 */
	public String getSubjectRelatedLogs(@PathParam("subjectId") String subjectId, @PathParam("clientId") String clientId, @PathParam("userId") String userId, @PathParam("timeStamp") long timeStamp, @Context HttpServletResponse res);
	
	/**
	 * 客户端同步开始/结束 -- 检查更新时间戳等信息
	 * @param clientId
	 * @param res
	 * @return
	 */
	public String checkConfig(String clientId, String data, String action, HttpServletResponse res);
	
	/**
	 * 批量删除数据(真删除，不可恢复)
	 * @param data
	 * @param action
	 * @param res
	 * @return
	 */
	public String truncateBatchData(String data, String action, HttpServletResponse res);
	
	/**
	 * 批量删除数据
	 * @param data
	 * @param action
	 * @param res
	 * @return
	 */
	public String deleteBatchData(String data, String action, HttpServletResponse res);
	
	/**
	 * 添加条目，生成HTML
	 * @param data
	 * @param ins
	 * @param action
	 * @param request
	 * @param res
	 * @return
	 * @throws IOException
	 */
	public String addNoteHtml(String data, InputStream ins, String action,
			HttpServletRequest request, HttpServletResponse res)
			throws IOException;
	
	/**
	 * 更新条目HTML
	 * @param data
	 * @param ins
	 * @param action
	 * @param request
	 * @param res
	 * @return
	 * @throws IOException
	 */
	public String updateNoteHtml(String data, InputStream ins, long timeStamp,
			boolean updateContent, String action, HttpServletRequest request,
			HttpServletResponse res) throws IOException;
	
	/**
	 * 初始化资源操作
	 * @param clientId
	 * @param action
	 * @param res
	 * @return
	 */
	public String initResouceAction(String clientId, String action,
			HttpServletResponse res);

	/**
	 * 初始化资源权限
	 * @param clientId
	 * @param action
	 * @param res
	 * @return
	 */
	public String initResoucePermission(String clientId, String action,
			HttpServletResponse res);

	/**
	 * 初始化资源
	 * @param clientId
	 * @param action
	 * @param res
	 * @return
	 */
	public String initResouce(String clientId, String action, HttpServletResponse res);

	/**
	 * 初始化角色
	 * @param clientId
	 * @param action
	 * @param res
	 * @return
	 */
	public String initRole(String clientId, String action, HttpServletResponse res);
	
	/**
	 * 查询truncate日志
	 * @param dataStr
	 * @param clientId
	 * @param timeStamp
	 * @param dataClass
	 * @param action
	 * @param res
	 * @return
	 * @throws Exception
	 */
	public String getTruncateLogs(String dataStr, String clientId, long timeStamp, String endTimeStr, 
			String dataClass, String action, HttpServletResponse res)
			throws Exception;

	/**
	 * 更新专题成员角色
	 * @param subjectId
	 * @param userId
	 * @param roleName
	 * @param action
	 * @param res
	 * @return
	 */
	public String updateSubjectMember(String data, long timeStamp, String action, HttpServletResponse res);

	/**
	 * 添加条目标签
	 * @param data
	 * @param action
	 * @param res
	 * @return
	 */
	public String addNoteTag(String data, String action, HttpServletResponse res);
	
	/**
	 * 删除条目标签关系
	 * @param id
	 * @param action
	 * @param res
	 * @return
	 */
	public String deleteNoteTag(String id, String action, HttpServletResponse res);

	/**
	 * 下载用户头像
	 * @param clientId
	 * @param userId
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public String downloadUserPhoto(String clientId, String userId,
			HttpServletResponse response) throws Exception;

	/**
	 * 用户token验证
	 * @param clientId
	 * @param userName
	 * @param token
	 * @return
	 */
	public String checkUser(String clientId, String userName, String token, HttpServletRequest request);

}
