package com.eht.webservice.service;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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
	@POST
	@Path("/send/client/a/{clientType}")
	public String registerClient(@PathParam("clientType") String clientType, @Context HttpServletResponse res);

	/**
	 * 删除客户端
	 * @param clientId
	 * @return
	 */
	@DELETE
	@Path("/send/client/d/{clientId}")
	public String deleteClient(@PathParam("clientId") String clientId);
	
	/**
	 * 修改用户密码
	 * @param oldPassword
	 * @param password
	 * @return
	 */
	@POST
	@Path("/send/user/u/{userId}")
	public String updatePassword(@PathParam("userId") String userId, @FormParam("oldPassword") String oldPassword, @FormParam("password") String password);
	
	/**
	 * 修改用户信息
	 * @param data
	 * @return
	 */
	@POST
	@Path("/send/user")
	public String updateUser(@FormParam("data") String data);
	
	/**
	 * 添加专题
	 * @param data	         专题数据
	 * @return
	 */
	@POST
	@Path("/send/subject/a")
	public String addSubject(@FormParam("data") String data, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res);
	/**
	 * 更新专题
	 * @param data	         专题数据
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws Exception 
	 */
	@POST
	@Path("/send/subject/u/{timeStamp}")
	public String updateSubject(@FormParam("data") String data, @PathParam("timeStamp") long timeStamp, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) throws IllegalAccessException, InvocationTargetException, Exception;
	
	/**
	 * 添加或修改专题
	 * @param data	         专题数据
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 */
	@POST
	@Path("/send/subject")
	public String addOrUpdateSubject(@FormParam("data") String data, @Context HttpServletResponse res) throws IllegalAccessException, InvocationTargetException;
	
	/**
	 * 删除专题
	 * @param id	   专题ID
	 * @return
	 */
	@DELETE
	@Path("/send/subject/d/{id}")
	public String deleteSubject(@PathParam("id") String id, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res);
	
	/**
	 * 客户端获取要同步的数据
	 * @param clientId	客户端标识
	 * @param userId	用户ID
	 * @param timeStamp	上次同步时间戳
	 * @return
	 * @throws Exception 
	 */
	@POST
	@Path("/get_data/{clientId}/{userId}/{timeStamp}/{offset}")
	public String getDeleteLogs(@FormParam("data") String data, @HeaderParam(SynchConstants.HEADER_CLIENT_ID) String clientId, @PathParam("timeStamp") long timeStamp, @DefaultValue(SynchConstants.DATA_CLASS_ALL) @HeaderParam(SynchConstants.HEADER_DATATYPE) String dataClass, @DefaultValue(SynchConstants.CLIENT_SYNCH_REQUEST) @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) throws Exception;
	
	/**
	 * 客户端获取要同步的数据数量
	 * @param clientId	客户端标识
	 * @param userId	用户ID
	 * @param timeStamp	上次同步时间戳
	 * @return
	 */
	@GET
	@Path("/get_count/{clientId}/{userId}/{timeStamp}")
	public int countSynchData(String clientId, String userId, String timeStamp, @Context HttpServletResponse res);
	
	/**
	 * 添加目录
	 * @param data	         目录数据
	 * @return
	 */
	@POST
	@Path("/send/directory/a")
	public String addDirectory(@FormParam("data") String data, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res);
	
	/**
	 * 更新专题
	 * @param data	         目录数据
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws Exception 
	 */
	@POST
	@Path("/send/directory/u/{timeStamp}")
	public String updateDirectory(@FormParam("data") String data, @PathParam("timeStamp") long timeStamp, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) throws IllegalAccessException, InvocationTargetException, Exception;
	
	/**
	 * 添加或更新专题
	 * @param data	         目录数据
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 */
	@POST
	@Path("/send/directory")
	public String addOrUpdateDirectory(@FormParam("data") String data, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) throws IllegalAccessException, InvocationTargetException;
	
	/**
	 * 删除目录
	 * @param clientId	客户端标识
	 * @param id		目录ID
	 * @return
	 */
	@DELETE
	@Path("/send/directory/d/{id}")
	public String deleteDirectory(@PathParam("id") String id, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res);
	
	/**
	 * 将用户添加到目录黑名单
	 * @param clientId		客户端标识
	 * @param directoryId	目录ID
	 * @param userId		用户ID
	 * @return
	 */
	@POST
	@Path("/send/directoryblack/a/{directoryId}/{userId}")
	public String addDirectoryBlack(@PathParam("directoryId") String directoryId, @PathParam("userId") String userId, @Context HttpServletResponse res);
	/**
	 * 将用户从目录黑名单移除
	 * @param clientId		客户端标识
	 * @param directoryId	目录ID
	 * @param userId		用户ID
	 * @return
	 */
	@DELETE
	@Path("/send/directoryblack/d/{directoryId}/{userId}")
	public String deleteDirectoryBlack(@PathParam("directoryId") String directoryId, @PathParam("userId") String userId, @Context HttpServletResponse res);
	
	/**
	 * 添加条目
	 * @param data		条目数据
	 * @return
	 */
	@POST
	@Path("/send/note/a")
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
	@POST
	@Path("/send/note/u/{timeStamp}")
	public String updateNote(@FormParam("data") String data, @PathParam("timeStamp") long timeStamp, @DefaultValue("true") @QueryParam("updateContent") boolean updateContent, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) throws IllegalAccessException, InvocationTargetException, Exception;
	
	/**
	 * 添加或更新条目
	 * @param data		条目数据
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 */
	@POST
	@Path("/send/note")
	public String addOrUpdateNote(@FormParam("data") String data, @DefaultValue("true") @FormParam("updateContent") boolean updateContent, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) throws IllegalAccessException, InvocationTargetException;
	
	/**
	 * 更新条目内容
	 * @param content	条目内容
	 * @return 更新结果信息
	 */
	@POST
	@Path("/send/note/u/{id}")
	public String updateNoteContent(@PathParam("id") String id, @FormParam("content") String content, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res);
	
	/**
	 * 删除条目
	 * @param id		条目ID
	 * @return
	 */
	@DELETE
	@Path("/send/note/d/{id}")
	public String deleteNote(@PathParam("id") String id, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res);
	
	/**
	 * 将用户添加到条目黑名单
	 * @param directoryId	条目ID
	 * @param userId		用户ID
	 * @return
	 */
	@POST
	@Path("/send/noteblack/a/{noteId}/{userId}")
	public String addNoteBlack(@PathParam("noteId") String noteId, @PathParam("userId") String userId, @Context HttpServletResponse res);
	
	/**
	 * 将用户添加到条目黑名单
	 * @param directoryId	条目ID
	 * @param userId		用户ID
	 * @return
	 */
	@DELETE
	@Path("/send/noteblack/d/{noteId}/{userId}")
	public String deleteNoteBlack(@PathParam("noteId") String noteId, @PathParam("userId") String userId, @Context HttpServletResponse res);
	
	/**
	 * 添加附件到数据库表
	 * @param data		附件数据
	 * @return
	 * @throws ParserException 
	 */
	@POST
	@Path("/send/attachment/a/{timeStamp}")
	public String addAttachment(@FormParam("data") String data, @PathParam("timeStamp") long timeStamp, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) throws ParserException;
	
	/**
	 * 表中删除附件
	 * @param id		附件ID
	 * @return
	 */
	@DELETE
	@Path("/send/attachment/d/{id}/{timeStamp}")
	public String deleteAttachment(@PathParam("id") String id, @PathParam("timeStamp") long timeStamp, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res);
	
	/**
	 * 上传文件
	 * @param attachmentId 文件ID
	 * @param flag  	         文件数据是否全部传输过来
	 * @param request 
	 * @return
	 * @throws IOException 
	 */
	@POST
	@Path("/upload/{attachmentId}")
	public String uploadAttachment(@PathParam("attachmentId") String attachmentId, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletRequest request, @Context HttpServletResponse res) throws IOException;
	
	/**
	 * 上传文件
	 * @param attachmentId 文件ID
	 * @param flag  	         文件数据是否全部传输过来
	 * @param request 
	 * @return
	 * @throws IOException 
	 */
	@POST
	@Path("/upload/resume/{attachmentId}/{flag}")
	public String resumeUploadAttachment(@PathParam("attachmentId") String attachmentId, @DefaultValue("1") @PathParam("flag") int flag, @Context HttpServletRequest request, @Context HttpServletResponse res) throws IOException;
	
	/**
	 * 批量上传文件
	 * @param attachmentId  附件ID
	 * @param request
	 * @return
	 * @throws IOException 
	 */
	@POST
	@Path("/upload_batch")
	public String uploadAttachmentBatch(@FormParam("attachmentId") String[] attachmentId, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletRequest request, @Context HttpServletResponse res) throws IOException;
	
	/**
	 * 下载文件
	 * @param attachmentId 文件ID
	 * @return
	 * @throws Exception 
	 */
	@GET
	@Path("/download/{attachmentId}")
	public String downloadAttachment(@HeaderParam(SynchConstants.HEADER_CLIENT_ID) String clientId, @PathParam("attachmentId") String attachmentId, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse response) throws Exception;
	
	/**
	 * 批量下载文件
	 * @param attachmentId 文件ID
	 * @return
	 */
	@POST
	@Path("/download_batch")
	public String downloadAttachmentBatch(@FormParam("attachmentId") String[] attachmentId, @Context HttpServletResponse res);
	
	/**
	 * 添加标签
	 * @param data
	 * @return
	 */
	@POST
	@Path("/send/tag/a")
	public String addTag(@FormParam("data") String data, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res);
	
	/**
	 * 修改标签
	 * @param data
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws Exception 
	 */
	@POST
	@Path("/send/tag/u/{timeStamp}")
	public String updateTag(@FormParam("data") String data, @PathParam("timeStamp") long timeStamp, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) throws IllegalAccessException, InvocationTargetException, Exception;
	
	/**
	 * 添加或修改标签
	 * @param data
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 */
	@POST
	@Path("/send/tag")
	public String addOrUpdateTag(@FormParam("data") String data, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) throws IllegalAccessException, InvocationTargetException;
	
	/**
	 * 删除标签
	 * @param id
	 * @return
	 */
	@DELETE
	@Path("/send/tag/d/{id}")
	public String deleteTag(@PathParam("id") String id, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res);
	
	/**
	 * 添加或修改模板
	 * @param data
	 * @return
	 */
	@POST
	@Path("/send/template")
	public String addTemplate(@FormParam("data") String data, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res);
	
	/**
	 * 添加评论
	 * @param data
	 * @return
	 */
	@POST
	@Path("/send/comment/a")
	public String addComment(@FormParam("data") String data, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res);
	
	/**
	 * 删除评论
	 * @param id
	 * @return
	 */
	@DELETE
	@Path("/send/comment/d/{id}")
	public String deleteComment(@PathParam("id") String id, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res);
	
	/**
	 * 添加修改专题成员或角色
	 * @param subjectId
	 * @param userId
	 * @param roleId
	 * @return
	 */
	@POST
	@Path("/send/subjectuser/{subjectId}/{userId}/{roleId}")
	public String addSubjectMember(@PathParam("subjectId") String subjectId, @PathParam("userId") String userId, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @PathParam("roleId") String roleId, @Context HttpServletResponse res);
	
	/**
	 * 删除专题成员
	 * @param subjectId
	 * @param userId
	 * @return
	 */
	@DELETE
	@Path("/send/subjectuser/d/{subjectId}/{userId}")
	public String deleteSubjectMember(@PathParam("subjectId") String subjectId, @PathParam("userId") String userId, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res);
	
	/**
	 * 获取服务器时间
	 * @return
	 */
	@GET
	@Path("/datetime")
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
	@POST
	@Path("/uploadNoteFile/{noteId}")
	public String uploadNoteFile(@PathParam("noteId") String noteId, @Context HttpServletRequest request, @Context HttpServletResponse res) throws IOException, ParserException;
	
	/**
	 * 按照操作和数据类型顺序查询用户需同步日志
	 * @param clientId
	 * @param userId
	 * @param timeStamp
	 * @param offset
	 * @return
	 * @throws Exception 
	 */
	@GET
	@Path("/getlogs/{timeStamp}")
	public String getSynchDataByStep(@HeaderParam("clientId") String clientId, @PathParam("timeStamp") long timeStamp, @DefaultValue(SynchConstants.DATA_CLASS_ALL) @HeaderParam(SynchConstants.HEADER_DATATYPE) String dataClass, @DefaultValue(SynchConstants.CLIENT_SYNCH_REQUEST) @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) throws Exception;

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
	@GET
	@Path("/get/{logId}")
	public String getSynchDataByLogId(@PathParam("logId") String logId, @Context HttpServletResponse res) throws NumberFormatException, Exception;
	
	/**
	 * 根据ID获取用户信息
	 * @param id
	 * @return
	 */
	@GET
	@Path("/get_user/{id}")
	public String getUserInfo(@PathParam("id") String id);

	/**
	 * 获取专题成员日志
	 * @param subjectId
	 * @param clientId
	 * @param userId
	 * @param timeStamp
	 * @return
	 */
	@GET
	@Path("/get/subjectuser/{subjectId}/{clientId}/{userId}/{timeStamp}")
	public String getSubjectUser(@PathParam("subjectId") String subjectId, @PathParam("clientId") String clientId, @PathParam("userId") String userId, @PathParam("timeStamp") long timeStamp, @Context HttpServletResponse res);
	
	/**
	 * 获取条目附件日志
	 * @param noteId
	 * @param clientId
	 * @param userId
	 * @param timeStamp
	 * @return
	 */
	@GET
	@Path("/get/attachment/{noteId}/{clientId}/{userId}/{timeStamp}")
	public String getNoteAttachment(@PathParam("noteId") String noteId, @PathParam("clientId") String clientId, @PathParam("userId") String userId, @PathParam("timeStamp") long timeStamp, @Context HttpServletResponse res);

	/**
	 * 获取专题下数据日志
	 * @param subjectId
	 * @param clientId
	 * @param userId
	 * @param timeStamp
	 * @return
	 */
	@GET
	@Path("/get/subjectdata/{subjectId}/{clientId}/{userId}/{timeStamp}")
	public String getSubjectRelatedLogs(@PathParam("subjectId") String subjectId, @PathParam("clientId") String clientId, @PathParam("userId") String userId, @PathParam("timeStamp") long timeStamp, @Context HttpServletResponse res);
	
	/**
	 * 客户端同步开始/结束 -- 检查更新时间戳等信息
	 * @param clientId
	 * @param res
	 * @return
	 */
	public String checkConfig(String clientId, String data, String action, HttpServletResponse res);

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

}
