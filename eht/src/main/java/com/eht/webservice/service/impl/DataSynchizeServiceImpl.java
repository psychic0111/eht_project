package com.eht.webservice.service.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.apache.log4j.Logger;
import org.htmlparser.util.ParserException;
import org.jeecgframework.core.util.JSONHelper;
import org.jeecgframework.core.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.security.web.authentication.rememberme.CookieTheftException;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationException;

import com.eht.comment.entity.CommentEntity;
import com.eht.comment.service.CommentServiceI;
import com.eht.common.bean.ResponseStatus;
import com.eht.common.cache.DataCache;
import com.eht.common.constant.ActionName;
import com.eht.common.constant.Constants;
import com.eht.common.constant.RoleName;
import com.eht.common.constant.SynchConstants;
import com.eht.common.enumeration.DataSynchAction;
import com.eht.common.enumeration.DataType;
import com.eht.common.enumeration.HeaderName;
import com.eht.common.enumeration.ResponseCode;
import com.eht.common.util.BeanMapUtils;
import com.eht.common.util.FilePathUtil;
import com.eht.common.util.FileToolkit;
import com.eht.common.util.HtmlParser;
import com.eht.common.util.JsonUtil;
import com.eht.common.util.MD5FileUtil;
import com.eht.common.util.ReflectionUtils;
import com.eht.common.util.UUIDGenerator;
import com.eht.group.service.GroupService;
import com.eht.log.entity.SynchLogEntity;
import com.eht.log.entity.SynchronizedLogEntity;
import com.eht.log.service.SynchLogServiceI;
import com.eht.note.entity.AttachmentEntity;
import com.eht.note.entity.NoteEntity;
import com.eht.note.entity.NoteTag;
import com.eht.note.entity.NoteVersionEntity;
import com.eht.note.service.AttachmentServiceI;
import com.eht.note.service.NoteServiceI;
import com.eht.resource.entity.ClassName;
import com.eht.resource.entity.ResourceAction;
import com.eht.resource.entity.ResourcePermission;
import com.eht.resource.service.ResourceActionService;
import com.eht.resource.service.ResourcePermissionService;
import com.eht.role.entity.Role;
import com.eht.role.entity.RoleUser;
import com.eht.role.service.RoleService;
import com.eht.subject.entity.DirectoryEntity;
import com.eht.subject.entity.SubjectEntity;
import com.eht.subject.service.DirectoryServiceI;
import com.eht.subject.service.SubjectServiceI;
import com.eht.system.bean.ClientEntity;
import com.eht.system.service.DataInitService;
import com.eht.tag.entity.TagEntity;
import com.eht.tag.service.TagServiceI;
import com.eht.template.entity.TemplateEntity;
import com.eht.template.service.TemplateServiceI;
import com.eht.user.entity.AccountEntity;
import com.eht.user.service.AccountServiceI;
import com.eht.webservice.bean.BatchDataBean;
import com.eht.webservice.bean.DataBean;
import com.eht.webservice.bean.SynchResult;
import com.eht.webservice.service.DataSynchizeService;
import com.eht.webservice.service.SynchResultService;
import com.eht.webservice.util.DataSynchizeUtil;
import com.eht.webservice.util.SynchDataCache;

public class DataSynchizeServiceImpl implements DataSynchizeService {

	private Logger logger = Logger.getLogger(DataSynchizeServiceImpl.class);
	
	private boolean needAuth = true;

	@Autowired
	private AccountServiceI accountService;

	@Autowired
	private DataInitService dataInitService;

	@Autowired
	private GroupService groupService;

	@Autowired
	private NoteServiceI noteService;

	@Autowired
	private DirectoryServiceI directoryService;

	@Autowired
	private TagServiceI tagService;

	@Autowired
	private TemplateServiceI templateService;

	@Autowired
	private CommentServiceI commentService;

	@Autowired
	private AttachmentServiceI attachmentService;

	@Autowired
	private SubjectServiceI subjectService;

	@Autowired
	private RoleService roleService;
	
	@Autowired
	private ResourceActionService resourceActionService;

	@Autowired
	private ResourcePermissionService resourcePermissionService;
	
	@Autowired
	private SynchLogServiceI synchLogService;
	
	@Autowired
	private SynchResultService synchResultService;
	
	@Autowired
	private PersistentTokenRepository tokenRepository;

	@Override
	@POST
	@Path("/uploadNoteFile/{noteId}")
	public String uploadNoteFile(@PathParam("noteId") String noteId, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletRequest request, @Context HttpServletResponse res) throws IOException, ParserException {
		AccountEntity user = accountService.getUser4Session();
		logger.info("上传条目.eht文件：" + noteId);
		if(!action.equals(DataSynchAction.FINISH.toString()) && !action.equals(DataSynchAction.NEXT.toString())){
			List<AttachmentEntity> attachmentList = attachmentService.findNeedUploadAttachmentByNote(noteId, Constants.FILE_TYPE_NOTEHTML);
			
			if(attachmentList != null && !attachmentList.isEmpty()){
				AttachmentEntity attachment = attachmentList.get(0);
				if(attachment.getMd5() != null){
					AttachmentEntity attaServer = attachmentService.findAttachmentByMd5(attachment.getMd5());
					// 服务器存在相同文件
					if (attaServer != null && attaServer.getStatus() == 1) {
						logger.info("服务器存在该文件，直接复制服务器文件！！！");
						DataSynchizeUtil.copyServerFile(attaServer, attachment);
						attachmentService.updateAttachment(attachment);
						String uploadData = DataSynchizeUtil.queryUploadFile(user, res);
						DataBean bean = new DataBean(uploadData, "");
						return JsonUtil.bean2json(bean);
					}
				}
		
				// 上次已经上传过该文件或分块传输
				/*if ((attachment.getTranSfer() != null && attachment.getTranSfer() > 0 && attachment.getStatus() == 0)) {
					logger.info("上次传输文件中断，共传输了" + attachment.getTranSfer() + "字节，准备继续传输！！！");
					return resumeUploadAttachment(attachmentId, 1, request, res); // 1默认文件数据都提交过来
				}*/
		
				InputStream ins = null;
				OutputStream ous = null;
				long transfered = 0; // 已传输字节
				attachment.setStatus(0); // 文件上传未完成
		
				File folder = new File(attachment.getFilePath());
				if (!folder.exists()) {
					folder.mkdirs();
				}
				String zipName = attachment.getFileName();
				File savefile = new File(attachment.getFilePath() + File.separator + zipName);
				try {
					logger.info("开始 上传条目.eht文件数据：" + savefile.getPath());
					ins = request.getInputStream();
					//transfered = FileToolkit.copyFileFromStreamToZIP(ins, savefile, true, attachment.getFileName());
					ous = new FileOutputStream(savefile);
					int n = 0;
					int buffer = 1024;
					byte[] bytes = new byte[buffer];
					while ((n = ins.read(bytes)) != -1) {
						ous.write(bytes, 0, n);
						transfered += n;
					}
		
					logger.info("条目.eht文件传输完成，本次共传输：" + transfered + "字节。");
					attachment.setStatus(Constants.FILE_TRANS_COMPLETED);
				} catch (Exception e) {
					ResponseStatus rs = new ResponseStatus(ResponseCode.SERVER_ERROR);
					return rs.toString();
				} finally {
					try {
						if(ous != null){
							ous.flush();
							ous.close();
						}
						if(ins != null){
							ins.close();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				attachment.setTranSfer(transfered);
				attachmentService.updateAttachment(attachment);
				logger.info("文件传输完成，更新数据库文件状态！");
				
				//将客户端上传的HTML内容同步到数据库中
				DataSynchizeUtil.synchNoteContent(noteId, savefile);
				
				res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
				res.setHeader(HeaderName.DATATYPE.toString(), DataType.FILE.toString());
				
				res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
				String uploadData = DataSynchizeUtil.queryUploadFile(user, res);
				DataBean bean = new DataBean(uploadData, "");
				String returnVal = JsonUtil.bean2json(bean);
				logger.info("上传notehtml完成后，返回值：" + returnVal);
				return returnVal;
			}
		}else if(action.equals(DataSynchAction.NEXT.toString())){
			logger.info("上传条目.eht时，客户传来action:NEXT");
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.FILE.toString());
			
			List<AttachmentEntity> attachmentList = attachmentService.findNeedUploadAttachmentByNote(noteId, Constants.FILE_TYPE_NOTEHTML);
			if(attachmentList != null && !attachmentList.isEmpty()){
				AttachmentEntity attachment = attachmentList.get(0);
				attachment.setStatus(Constants.FILE_TRANS_COMPLETED);//暂时的
				attachmentService.updateAttachment(attachment);
			}
			
			String uploadData = DataSynchizeUtil.queryUploadFile(user, res);
			DataBean bean = new DataBean(uploadData, "");
			String returnVal = JsonUtil.bean2json(bean);
			logger.info("客户传来action:NEXT，返回值：" + returnVal);
		}else{
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.REQUEST.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.ALL.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.FILE.toString());
			
			res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
		}
		DataBean bean = new DataBean("", "");
		String returnVal = JsonUtil.bean2json(bean);
		logger.info("上传notehtml全部完成后，返回值：" + returnVal);
		return returnVal;
	}

	@Override	
	@POST
	@Path("/upload/{attachmentId}")
	public String uploadAttachment(@PathParam("attachmentId") String attachmentId, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletRequest request, @Context HttpServletResponse res) {
		AccountEntity user = accountService.getUser4Session();
		logger.info("上传条目.eht文件：" + attachmentId);
		if(!action.equals(DataSynchAction.FINISH.toString())){
			AttachmentEntity attachment = attachmentService.getAttachment(attachmentId);
			if(attachment.getMd5() != null){
				AttachmentEntity attaServer = attachmentService.findAttachmentByMd5(attachment.getMd5());
				// 服务器存在相同文件
				if (attaServer != null && attaServer.getStatus() == 1) {
					logger.info("服务器存在该文件，直接复制服务器文件！！！");
					DataSynchizeUtil.copyServerFile(attaServer, attachment);
					attachmentService.updateAttachment(attachment);
					String uploadData = DataSynchizeUtil.queryUploadFile(user, res);
					DataBean bean = new DataBean(uploadData, "");
					return JsonUtil.bean2json(bean);
				}
			}
	
			// 上次已经上传过该文件或分块传输
			/*if ((attachment.getTranSfer() != null && attachment.getTranSfer() > 0 && attachment.getStatus() == 0)) {
				logger.info("上次传输文件中断，共传输了" + attachment.getTranSfer() + "字节，准备继续传输！！！");
				return resumeUploadAttachment(attachmentId, 1, request, res); // 1默认文件数据都提交过来
			}*/
	
			InputStream ins = null;
			//OutputStream ous = null;
			long transfered = 0; // 已传输字节
			attachment.setStatus(0); // 文件上传未完成
	
			File folder = new File(attachment.getFilePath());
			if (!folder.exists()) {
				folder.mkdirs();
			}
			String zipName = attachment.getFileName().substring(0, attachment.getFileName().lastIndexOf('.')) + ".zip";
			File savefile = new File(attachment.getFilePath() + File.separator + zipName);
			try {
				logger.info("开始 上传条目附件数据：" + savefile.getPath());
				ins = request.getInputStream();
				transfered = FileToolkit.copyFileFromStreamToZIP(ins, savefile, true, attachment.getFileName());
				/*ous = new FileOutputStream(file);
				int n = 0;
				int buffer = 1024;
				byte[] bytes = new byte[buffer];
				while ((n = ins.read(bytes)) != -1) {
					ous.write(bytes, 0, n);
					transfered += n;
				}*/
	
				logger.info("条目附件传输完成，本次共传输：" + transfered + "字节。");
				attachment.setStatus(Constants.FILE_TRANS_COMPLETED);
			} catch (Exception e) {
				ResponseStatus rs = new ResponseStatus(ResponseCode.SERVER_ERROR);
				return rs.toString();
			} finally {
				/*try {
					ous.flush();
					ous.close();
					ins.close();
				} catch (IOException e) {
					e.printStackTrace();
				}*/
			}
			attachment.setTranSfer(transfered);
			attachmentService.updateAttachment(attachment);
			logger.info("文件传输完成，更新数据库文件状态！");
			if (attachment.getStatus() != null && attachment.getStatus() == 1) {
				// 上传文件完成后，重命名文件为正式文件
				//file.renameTo(new File(attachment.getFilePath() + File.separator + attachment.getFileName()));
			}
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.FILE.toString());
			
			res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
			String uploadData = DataSynchizeUtil.queryUploadFile(user, res);
			DataBean bean = new DataBean(uploadData, "");
			return JsonUtil.bean2json(bean);
		}else{
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.REQUEST.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.ALL.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.FILE.toString());
			
			res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
			logger.info("条目附件传输全部完成！！！！");
			DataBean bean = new DataBean("", "");
			return JsonUtil.bean2json(bean);
		}
	}

	/**
	 * 续传文件
	 */
	@Override
	@POST
	@Path("/upload/resume/{attachmentId}/{flag}")
	public String resumeUploadAttachment(@PathParam("attachmentId") String attachmentId, @DefaultValue("1") @PathParam("attachmentId") int flag, @Context HttpServletRequest request, @Context HttpServletResponse res) {
		ResponseStatus rs = new ResponseStatus(); // 上传文件操作结果
		AttachmentEntity attachment = attachmentService.getAttachment(attachmentId);
		AttachmentEntity attaServer = attachmentService.findAttachmentByMd5(attachment.getMd5());
		// 服务器存在相同文件
		if (attaServer != null) {
			logger.info("服务器存在该文件，直接复制服务器文件！！！");
			rs = DataSynchizeUtil.copyServerFile(attaServer, attachment);
			attachmentService.updateAttachment(attachment);

			// 删除原上传临时文件
			File file = new File(attachment.getFilePath() + File.separator + attachment.getFileName() + ".tmp");
			file.delete();
			return res.toString();
		}

		InputStream ins = null;
		long transfered = attachment.getTranSfer() == null ? 0 : attachment.getTranSfer(); // 已传输字节
		attachment.setStatus(0); // 文件上传未完成

		File folder = new File(attachment.getFilePath());
		if (!folder.exists()) {
			folder.mkdirs();
		}
		File file = new File(attachment.getFilePath() + File.separator + attachment.getFileName() + ".tmp");
		if (!file.exists()) {
			try {
				file.createNewFile();
				logger.info("续传文件不存在，创建文件：" + file.getName());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(file, "rw");
			long length = Long.parseLong(request.getHeader("Content-Length"));
			raf.setLength(transfered + length);
			ins = request.getInputStream();
			int n = 0;
			int buffer = 1024;
			byte[] bytes = new byte[buffer];
			while ((n = ins.read(bytes)) != -1) {
				raf.seek(transfered);
				raf.write(bytes, 0, n);
				transfered += n;
			}

			if (flag == 1) {
				logger.info("文件传输完成，共传输：" + transfered + "字节。");
				attachment.setStatus(1); // 文件保存完成
			} else {
				logger.info("文件分块传输完成，本次共传输：" + transfered + "字节。");
			}
		} catch (Exception e) {
			rs = new ResponseStatus(ResponseCode.SERVER_ERROR);
			e.printStackTrace();
			return res.toString();
		} finally {
			try {
				raf.close();
				ins.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			attachment.setTranSfer(transfered);
			attachmentService.updateAttachment(attachment);
			logger.info("文件分块传输完成，更新数据库文件状态！");
			if (attachment.getStatus() != null && attachment.getStatus() == 1) {
				file.renameTo(new File(attachment.getFilePath() + File.separator + attachment.getFileName()));
			}
		}
		return rs.toString();
	}

	@Override
	@POST
	@Path("/upload_batch")
	public String uploadAttachmentBatch(@FormParam("attachmentId") String[] attachmentId, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletRequest request, @Context HttpServletResponse res) {
		String result = (new ResponseStatus()).toString();
		for (int i = 0; i < attachmentId.length; i++) {
			result = uploadAttachment(attachmentId[i], action, request, res);
			ResponseStatus rs = JSONHelper.fromJsonToObject(result, ResponseStatus.class);
			if (rs.getStatus() != ResponseCode.NEXT.getCode()) {
				return result;
			}
		}
		return result;
	}

	@Override
	@GET
	@Path("/downloadphoto/{userId}")
	public String downloadUserPhoto(@HeaderParam(SynchConstants.HEADER_CLIENT_ID) String clientId, @PathParam("userId") String userId, @Context HttpServletResponse response) throws Exception {
		//AccountEntity user = accountService.getUser4Session();
		AccountEntity theUser = accountService.getUser(userId);
		String photo = theUser.getPhoto();
		String fileName = photo.substring(photo.lastIndexOf("/") + 1);
		
		File file = new File(FilePathUtil.getWebRootPath() + "/" + photo);
		
		FileInputStream fis = new FileInputStream(file);
		int length = fis.available();
		
		byte[] b = new byte[length];
		fis.read(b);
		response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
		response.addHeader("Content-Length", "" + length);
		response.setContentType("application/octet-stream;charset=UTF-8");
		
		/*response.setHeader(HeaderName.ACTION.toString(), DataSynchAction.DOWNLOAD.toString());
		response.setHeader(HeaderName.DATATYPE.toString(), DataType.FILE.toString());
		
		response.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.REQUEST.toString());
		response.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.FILE.toString());*/
		
		OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
		try {
			outputStream.write(b);
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			outputStream.flush();
			outputStream.close();
			fis.close();
		}
		DataBean bean = new DataBean("", "");
		return JsonUtil.bean2json(bean);
	}
	
	@Override
	@GET
	@Path("/download/{attachmentId}")
	public String downloadAttachment(@HeaderParam(SynchConstants.HEADER_CLIENT_ID) String clientId, @PathParam("attachmentId") String attachmentId, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse response) throws Exception {
		AccountEntity user = accountService.getUser4Session();
		
		if(!action.equals(DataSynchAction.FINISH.toString())){
			AttachmentEntity attachment = attachmentService.getAttachment(attachmentId);
			File file = null;
			
			String zipName = "";
			//传过来的是条目ID，以后再改
			if(attachment == null){
				//List<AttachmentEntity> attaList = attachmentService.findAttachmentByNote(noteService.getNote(attachmentId), Constants.FILE_TYPE_NOTEHTML);
				
				NoteEntity note = noteService.getNote(attachmentId);
				zipName = FilePathUtil.getNoteZipFileName(attachmentId, null);
				file = new File(FilePathUtil.getNoteHtmlPath(note) + zipName);
			}else{
				zipName = attachment.getFileName().substring(0, attachment.getFileName().lastIndexOf('.')) + ".zip";
				file = new File(attachment.getFilePath() + File.separator + zipName);
			}
			if(file != null && file.exists()){
				FileInputStream fis = new FileInputStream(file);
				int length = fis.available();
				
				logger.info("客户端请求下载文件：" + file.getPath() + ", 请求主键：" + attachmentId);
				//String nextRes = DataSynchizeUtil.queryDownloadFile(user.getId(), clientId, DataAction.REQUEST.toString(), synchLogService, response);
				//System.out.println("下载文件返回字符串：" + nextRes);
				byte[] b = new byte[length];
				fis.read(b);
				//byte[] bs = ArrayUtils.addAll(b, nextRes.getBytes());
				response.setHeader("Content-Disposition", "attachment; filename=\"" + zipName + "\"");
				//response.addHeader("File-Length", "" + length);
				response.addHeader("Content-Length", "" + length);
				response.setContentType("application/octet-stream;charset=UTF-8");
				
				response.setHeader(HeaderName.ACTION.toString(), DataSynchAction.DOWNLOAD.toString());
				response.setHeader(HeaderName.DATATYPE.toString(), DataType.FILE.toString());
				
				response.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.REQUEST.toString());
				response.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.FILE.toString());
				
				OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
				try {
					outputStream.write(b);
				} catch (Exception e) {
					e.printStackTrace();
				} finally{
					outputStream.flush();
					outputStream.close();
					fis.close();
					logger.info("客户端请求下载文件完成！");
				}
			}else{
				response.setHeader(HeaderName.ACTION.toString(), DataSynchAction.DOWNLOAD.toString());
				response.setHeader(HeaderName.DATATYPE.toString(), DataType.FILE.toString());
				
				response.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.REQUEST.toString());
				response.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.FILE.toString());
			}
			
			/*String downloadData = DataSynchizeUtil.queryDownloadFile(user.getId(), clientId, DataSynchAction.REQUEST.toString(), response);
			DataBean bean = new DataBean("", downloadData);
			String returnVal = JsonUtil.bean2json(bean);
			logger.info("返回下载文件数据：" + returnVal);
			return returnVal;*/
			
		}else{
			response.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.FINISH.toString());
			response.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.FILE.toString());
			
			response.setHeader(HeaderName.ACTION.toString(), DataSynchAction.FINISH.toString());
			response.setHeader(HeaderName.DATATYPE.toString(), DataType.FILE.toString());
		}
		DataBean bean = new DataBean("", "");
		return JsonUtil.bean2json(bean);
	}

	@Override
	@GET
	@Path("/download_batch")
	public String downloadAttachmentBatch(@FormParam("attachmentId") String[] attachmentId, @Context HttpServletResponse res) {
		return null;
	}

	@Override
	@POST
	@Path("/send/attachment/a/{timeStamp}")
	public String addAttachment(@FormParam("data") String data, @PathParam("timeStamp") long timeStamp, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) throws ParserException, InsufficientAuthenticationException {
		logger.info("添加附件信息 : " + data);
		AccountEntity user = accountService.getUser4Session();
		if(!action.equals(DataSynchAction.FINISH.toString())){
			AttachmentEntity attachment = (AttachmentEntity) JsonUtil.getObject4JsonString(data, AttachmentEntity.class);
			attachment.setClientId(user.getClientId());
			String subjectId = null;
			NoteEntity note = null;
			if(!StringUtil.isEmpty(attachment.getNoteId())){
				note = noteService.getNote(attachment.getNoteId());
				//条目不存在了
				if(note == null || note.getDeleted().intValue() == Constants.DATA_DELETED){
					logger.info("附件所属条目已无效 : " + attachment.getNoteId());
					res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.ATTACHMENT.toString());
					
					String uploadData = DataSynchizeUtil.queryUploadFile(user, res);
					DataBean bean = new DataBean(uploadData, "");
					return JsonUtil.bean2json(bean);
				}else{
					subjectId = note.getSubjectId();
				}
			}else{
				DirectoryEntity dir = directoryService.getDirectory(attachment.getDirectoryId());
				subjectId = dir.getSubjectId();
			}
			
			boolean saveLog = false;
			//用户是否在条目目录黑名单中
			if(!saveLog){
				if(!StringUtil.isEmpty(note.getDirId())){
					boolean isBan = directoryService.inDirBlackList(user.getId(), note.getDirId());
					if(isBan){
						logger.warn("用户在目录黑名单中", new InsufficientAuthenticationException("用户在目录黑名单中"));
						saveLog = true;
						
						res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
						res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.ATTACHMENT.toString());
						
						res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.BAN.toString());
						res.setHeader(HeaderName.DATATYPE.toString(), DataType.ATTACHMENT.toString());
						
						logger.info("用户在目录黑名单中：");
					}
				}
			}
			
			//用户是否在条目黑名单中
			if(!saveLog){
				boolean isBan = noteService.inNoteBlackList(user.getId(), note.getId());
				if(isBan){
					logger.warn("用户在条目黑名单中", new InsufficientAuthenticationException("用户在条目黑名单中"));
					saveLog = true;
					
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.ATTACHMENT.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.BAN.toString());
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.ATTACHMENT.toString());
					
					logger.info("用户在条目黑名单中：");
				}
			}
			
			if(!saveLog){
				//作者
				boolean isOwner = (note != null && note.getCreateUserId().equals(user.getId())) || note == null;
				//编辑
				boolean hasPermission = DataSynchizeUtil.hasPermission(user.getId(), subjectId, ActionName.ADD_DIRECTORY);
				if(!hasPermission && needAuth && !isOwner){
					logger.warn("用户操作权限不足", new InsufficientAuthenticationException("用户没有进行此操作的权限： 【上传附件】, 请联系专题管理员。"));
					
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.ATTACHMENT.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.ATTACHMENT.toString());
					
					logger.info("权限不足，不能添加附件，删除新增附件：");
				}
			}
			
			if(saveLog){
				DataSynchizeUtil.saveBanLog(DataType.ATTACHMENT.toString(), DataSynchAction.TRUNCATE.toString(), attachment.getId(), user.getId(), user.getId(), System.currentTimeMillis(), System.currentTimeMillis());
				DataBean bean = new DataBean();
				String returnVal = JsonUtil.bean2json(bean);
				logger.info("权限不足，不能添加附件，删除新增附件：" + returnVal);
				return returnVal;
			}
			
			//普通附件
			if (attachment.getFileType().intValue() == Constants.FILE_TYPE_NORMAL) {
				String filePath = FilePathUtil.getFileUploadPath(note, attachment.getDirectoryId());
				attachment.setFilePath(filePath);
			} else { // 目前设计应该没有下面的附件类型
				if(note != null){
					String suffix = attachment.getFileName().substring(attachment.getFileName().lastIndexOf('.') + 1);
					String filePath = FilePathUtil.getImageUploadPath(note);
					attachment.setFilePath(filePath);
					attachment.setId(UUIDGenerator.uuid());
					attachment.setFilePath(filePath);
					attachment.setSuffix(suffix);
					attachment.setFileType(Constants.FILE_TYPE_IMAGE);
					if(!StringUtil.isEmpty(suffix) && suffix.equalsIgnoreCase("js")){
						attachment.setFileType(Constants.FILE_TYPE_JS);
					}
					if(!StringUtil.isEmpty(suffix) && suffix.equalsIgnoreCase("css")){
						attachment.setFileType(Constants.FILE_TYPE_CSS);
					}
					attachment.setFileType(Constants.FILE_TYPE_IMAGE);
					
					HtmlParser parser = new HtmlParser(note.getContent());
					String imgUrl = FilePathUtil.getImageUrl(note);
					String content = parser.parseNoteContentByFileName(attachment.getFileName(), imgUrl + "/" + attachment.getFileName());
					note.setContent(content);
					noteService.updateEntitie(note);
				}
			}
			attachment.setDeleted(Constants.DATA_NOT_DELETED);
			attachment.setStatus(Constants.FILE_TRANS_NOT_COMPLETED);
			attachment.setTranSfer(0L);
			attachment.setCreateUser(user.getId());
			if(StringUtil.isEmpty(attachment.getId())){
				attachment.setId(UUIDGenerator.uuid());
			}
			if(StringUtil.isEmpty(attachment.getSuffix())){
				attachment.setSuffix(FilenameUtils.getExtension(attachment.getFileName()));
			}
			attachmentService.addAttachment(attachment);
			
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.ATTACHMENT.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.ATTACHMENT.toString());
			
			res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
			/*int count = synchLogService.countAttachmentLogByNote(user.getClientId(), user.getId(), timeStamp, attachment.getNoteId());
			if(count > 0){
				res.setHeader("ACTION", "NEXT");
				res.setHeader("dataType", "ATTACHMENT");
			}*/
		}else{
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.ATTACHMENT.toString());
			
			String uploadData = DataSynchizeUtil.queryUploadFile(user, res);
			DataBean bean = new DataBean(uploadData, "");
			return JsonUtil.bean2json(bean);
		}
		DataBean bean = new DataBean("", "");
		return JsonUtil.bean2json(bean);
	}

	@Override
	@DELETE
	@Path("/send/attachment/d/{id}/{timeStamp}")
	public String deleteAttachment(@PathParam("id") String id, @PathParam("timeStamp") long timeStamp, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) {
		logger.info("删除附件信息 ！！！");
		if(!action.equals(DataSynchAction.FINISH.toString())){
			AttachmentEntity attachment = attachmentService.getAttachment(id);
			AccountEntity user = accountService.getUser4Session();
			if(attachment != null && attachment.getDeleted() == Constants.DATA_NOT_DELETED){
				if(!StringUtil.isEmpty(attachment.getNoteId())){
					NoteEntity note = noteService.getNote(attachment.getNoteId());
					boolean hasPermission = DataSynchizeUtil.hasPermission(user.getId(), note.getSubjectId(), ActionName.ADD_NOTE);
					if(!hasPermission && needAuth){
						throw new InsufficientAuthenticationException("用户没有进行此操作的权限：" + ActionName.UPDATE_NOTE + ", 请联系专题管理员。");
					}
				}
				
				attachment.setUpdateUser(user.getId());
				attachmentService.markDelAttachment(attachment);
			}
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.ATTACHMENT.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.ATTACHMENT.toString());
			
			/*int count = synchLogService.countAttachmentLogByNote(user.getClientId(), user.getId(), timeStamp, attachment.getNoteId());
			if(count > 0){
				rs.setResponse(ResponseCode.NEED_SYNCH_ATTACHMENT);
				rs.setData(attachment.getNoteId());
			}*/
			
		}else{
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.ATTACHMENT.toString());
		}
		DataBean bean = new DataBean("", "");
		return JsonUtil.bean2json(bean);
	}

	@Override
	@GET
	@Path("/get_count/{clientId}/{userId}/{timeStamp}")
	public int countSynchData(@PathParam("clientId") String clientId, @PathParam("userId") String userId, @PathParam("timeStamp") String timeStamp, @Context HttpServletResponse res) {
		long time = Long.parseLong(timeStamp);
		int count = synchLogService.countSynchLogsByTarget(clientId, userId, "", time, 0);
		return count;
	}
	
	/**
	 * 查询truncate操作日志
	 * @param dataStr
	 * @param clientId
	 * @param timeStamp
	 * @param dataClass
	 * @param action
	 * @param res
	 * @return
	 * @throws Exception
	 */
	@Override
	@POST
	@Path("/gettrunclogs/{timeStamp}")
	public String getTruncateLogs(@FormParam("data") String dataStr, @HeaderParam(SynchConstants.HEADER_CLIENT_ID) String clientId, @PathParam("timeStamp") long timeStamp, String endTimeStr, @DefaultValue(SynchConstants.DATA_CLASS_ALL) @HeaderParam(SynchConstants.HEADER_DATATYPE) String dataClass, @DefaultValue(SynchConstants.CLIENT_SYNCH_REQUEST) @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) throws Exception {
		logger.info("客户端获取truncate日志:" + dataStr);
		AccountEntity user = accountService.getUser4Session();
		String[] dataTypes = SynchDataCache.getReverseDatasSort();
		
		// 从data中提取本次要查询的数据类型
		if(!StringUtil.isEmpty(dataStr)){
			Map<String, String> map = JsonUtil.getMap4Json(dataStr);
			dataClass = map.get("dataType");
		}
		
		long endTime = System.currentTimeMillis();
		if(!StringUtil.isEmptyOrBlank(endTimeStr)){
			endTime = Long.parseLong(endTimeStr);
		}else{
			SynchResult sr = synchResultService.findSynchResults(clientId, user.getId());
			if(sr.getStatus() == SynchConstants.SYNCHRONIZE_STATUS_CHECK){
				endTime = sr.getLastSynTimestamp();
			}
		}
		
		List<SynchLogEntity> result = synchLogService.findTruncSynchLogs(clientId, user.getId(), timeStamp, endTime, dataClass);
		// 如果存在需要同步的日志
		if(result != null && !result.isEmpty()){
			//从查询到的日志中确定当前的数据类型
			if(dataClass.equals(DataType.ALL.toString()) || dataClass.equals(DataType.BATCHDATA.toString())){
				SynchLogEntity theLog = result.get(0);
				dataClass = theLog.getClassName();
			}
			
			BatchDataBean bean = new BatchDataBean();
			List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
			for(SynchLogEntity theLog : result){
				if(theLog.getClassName().equals(dataClass)){
					Map<String, Object> map = DataSynchizeUtil.parseDeleteLog(theLog);
					list.add(map);
				}
			}
			bean.setDatas(JsonUtil.list2json(list));
			bean.setDataType(dataClass);
			String data = JsonUtil.bean2json(bean);
			
			// 查询下一有同步日志的数据类型
			String nextDataType = DataSynchizeUtil.queryNextDataType(clientId, user.getId(), timeStamp, endTime, DataSynchAction.TRUNCATE.toString(), dataClass, dataTypes, synchLogService);
			// 设置response头
			if(nextDataType != null){
				res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.REQUEST.toString());
				res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
				
				res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.TRUNCATE.toString());
				res.setHeader(HeaderName.DATATYPE.toString(), DataType.BATCHDATA.toString());
				
				Map<String, String> delMap = new HashMap<String, String>();
				delMap.put("dataType", nextDataType);
				delMap.put("action", DataSynchAction.TRUNCATE.toString());
				delMap.put("endTimestamp", endTimeStr);            // 同步结束时间，同步过程中为空，同步结束再查同步这段时间的同步日志时为这次同步的结束时间
				String returnData = JsonUtil.map2json(delMap);  // 客户端再次请求需提交的数据类型
				
				
				DataBean returnBean = new DataBean(returnData, data);
				String returnVal = JsonUtil.bean2json(returnBean);
				
				logger.info("truncate日志返回1：" + returnVal);
				return returnVal;
			}else{
				// count = 0, 剩下的数据类型中未找到需同步的日志
				res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.REQUEST.toString());
				res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
				
				res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.TRUNCATE.toString());
				res.setHeader(HeaderName.DATATYPE.toString(), DataType.BATCHDATA.toString());
				
				Map<String, String> delMap = new HashMap<String, String>();
				delMap.put("dataType", dataTypes[0]);
				delMap.put("action", DataSynchAction.DELETE.toString());
				String returnData = JsonUtil.map2json(delMap);
				
				DataBean returnBean = new DataBean(returnData, data);
				String returnVal = JsonUtil.bean2json(returnBean);
				logger.info("truncate日志返回2：" + returnVal);
				return returnVal;
			}
			
		}else{
			//已经到了最后一个数据类型
			if(dataClass.equals(dataTypes[dataTypes.length - 1]) || dataClass.equals(DataType.ALL.toString())){
				res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.REQUEST.toString());
				res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
				
				res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
				res.setHeader(HeaderName.DATATYPE.toString(), DataType.BATCHDATA.toString());
				
				BatchDataBean bean = new BatchDataBean();
				bean.setDataType(dataTypes[0]);
				String data = JsonUtil.bean2json(bean);
				Map<String, String> delMap = new HashMap<String, String>();
				delMap.put("dataType", dataTypes[0]);
				delMap.put("action", DataSynchAction.DELETE.toString());
				String returnData = JsonUtil.map2json(delMap);
				
				DataBean returnBean = new DataBean(returnData, data);
				String returnVal = JsonUtil.bean2json(returnBean);
				logger.info("truncate日志返回3：" + returnVal);
				return returnVal;
			}else{
				// 查询下一有同步日志的数据类型
				String nextDataType = DataSynchizeUtil.queryNextDataType(clientId, user.getId(), timeStamp, endTime, DataSynchAction.TRUNCATE.toString(), dataClass, dataTypes, synchLogService);
				if(nextDataType != null){
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.REQUEST.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.TRUNCATE.toString());
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.BATCHDATA.toString());
					
					Map<String, String> delMap = new HashMap<String, String>();
					delMap.put("dataType", nextDataType);
					delMap.put("action", DataSynchAction.TRUNCATE.toString());
					String returnData = JsonUtil.map2json(delMap);  // 客户端再次请求需提交的数据类型
					
					DataBean returnBean = new DataBean(returnData, "");
					String returnVal = JsonUtil.bean2json(returnBean);
					logger.info("truncate日志返回4：" + returnVal);
					return returnVal;
				}else{
					// nextDataType = null, 剩下的数据类型中未找到需同步的日志
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.REQUEST.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.BATCHDATA.toString());
					
					BatchDataBean bean = new BatchDataBean();
					bean.setDataType(dataTypes[0]);
					String data = JsonUtil.bean2json(bean);
					Map<String, String> delMap = new HashMap<String, String>();
					delMap.put("dataType", dataTypes[0]);
					delMap.put("action", DataSynchAction.DELETE.toString());
					String returnData = JsonUtil.map2json(delMap);
					
					DataBean returnBean = new DataBean(returnData, data);
					String returnVal = JsonUtil.bean2json(returnBean);
					logger.info("truncate日志返回5：" + returnVal);
					return returnVal;
				}
			}
			
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@POST
	@Path("/getdellogs/{timeStamp}")
	public String getDeleteLogs(@FormParam("data") String dataStr, @HeaderParam(SynchConstants.HEADER_CLIENT_ID) String clientId, @PathParam("timeStamp") long timeStamp, String endTimeStr, @DefaultValue(SynchConstants.DATA_CLASS_ALL) @HeaderParam(SynchConstants.HEADER_DATATYPE) String dataClass, @DefaultValue(SynchConstants.CLIENT_SYNCH_REQUEST) @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) throws Exception {
		logger.info("客户端获取删除日志" + dataStr);
		AccountEntity user = accountService.getUser4Session();
		String[] dataTypes = SynchDataCache.getReverseDatasSort();
		
		// 从data中提取本次要查询的数据类型
		if(!StringUtil.isEmpty(dataStr)){
			Map<String, String> map = JsonUtil.getMap4Json(dataStr);
			dataClass = map.get("dataType");
			action = map.get("action");
			
			if(action.equals(DataSynchAction.TRUNCATE.toString())){
				return getTruncateLogs(dataStr, clientId, timeStamp, endTimeStr, dataClass, action, res);
			}
		}
		
		long endTime = System.currentTimeMillis();
		if(!StringUtil.isEmptyOrBlank(endTimeStr)){
			endTime = Long.parseLong(endTimeStr);
		}else{
			SynchResult sr = synchResultService.findSynchResults(clientId, user.getId());
			if(sr.getStatus() == SynchConstants.SYNCHRONIZE_STATUS_CHECK){
				endTime = sr.getLastSynTimestamp();
			}
		}
		
		boolean filterDelete = false;  //只查询删除操作日志,过滤掉其它
		logger.info("获取delete日志，[" + dataClass + "]" + timeStamp + " -- " + endTime);
		List<SynchLogEntity> result = synchLogService.findSynchLogsByTarget(clientId, user.getId(), timeStamp, endTime, dataClass, filterDelete);
		// 如果存在需要同步的日志
		if(result != null && !result.isEmpty()){
			//从查询到的日志中确定当前的数据类型
			if(dataClass.equals(DataType.ALL.toString()) || dataClass.equals(DataType.BATCHDATA.toString())){
				SynchLogEntity theLog = result.get(0);
				dataClass = theLog.getClassName();
			}
			
			BatchDataBean bean = new BatchDataBean();
			List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
			
			// 判断回收站中的日志是否要返回，目录和条目
			for(SynchLogEntity theLog : result){
				if(theLog.getClassName().equals(dataClass)){
					if(dataClass.equals(DataType.NOTE.toString())){
						NoteEntity note = noteService.getNote(theLog.getClassPK());
						if(note != null && note.getDeleted().intValue() != Constants.DATA_NOT_DELETED && !theLog.getAction().equals(DataSynchAction.DELETE.toString()) && !theLog.getAction().equals(DataSynchAction.TRUNCATE.toString())){
							boolean isReturn = DataSynchizeUtil.recyclePermission(user.getId(), note);
							if(isReturn){
								// 如果一条ADD日志和一条DELETE日志，合并返回就只有ADD日志，为了客户端正常处理，必须ADD和DELETE都返回
								if(theLog.getAction().equals(DataSynchAction.ADD.toString())){
									SynchronizedLogEntity synchedLog = synchLogService.findSynchedLogByLogId(theLog.getId());
									synchLogService.delete(synchedLog);
									
									/*Map<String, Object> map = DataSynchizeUtil.parseLog(theLog);
									map.put("createTimeStamp", note.getCreateTimeStamp());
									map.put("updateTimeStamp", note.getCreateTimeStamp());
									list.add(map);
									
									Map<String, Object> delMap = DataSynchizeUtil.parseDeleteLog(theLog);
									delMap.put("operation", DataSynchAction.DELETE.toString());
									list.add(delMap);*/
								}
							}
						}else{
							Map<String, Object> map = DataSynchizeUtil.parseDeleteLog(theLog);
							list.add(map);
						}
					}else if(dataClass.equals(DataType.DIRECTORY.toString())){
						DirectoryEntity dir = directoryService.getDirectory(theLog.getClassPK());
						if(dir != null && dir.getDeleted().intValue() != Constants.DATA_NOT_DELETED && !theLog.getAction().equals(DataSynchAction.DELETE.toString()) && !theLog.getAction().equals(DataSynchAction.TRUNCATE.toString())){
							boolean isReturn = DataSynchizeUtil.recyclePermission(user.getId(), dir);
							if(isReturn){
								if(theLog.getAction().equals(DataSynchAction.ADD.toString())){
									SynchronizedLogEntity synchedLog = synchLogService.findSynchedLogByLogId(theLog.getId());
									synchLogService.delete(synchedLog);
									
									/*Map<String, Object> map = DataSynchizeUtil.parseLog(theLog);
									map.put("createTimeStamp", dir.getCreateTimeStamp());
									map.put("updateTimeStamp", dir.getCreateTimeStamp());
									list.add(map);
									
									Map<String, Object> delMap = DataSynchizeUtil.parseDeleteLog(theLog);
									delMap.put("operation", DataSynchAction.DELETE.toString());
									list.add(delMap);*/
								}
							}
						}else{
							Map<String, Object> map = DataSynchizeUtil.parseDeleteLog(theLog);
							list.add(map);
						}
					}else{
						Map<String, Object> map = DataSynchizeUtil.parseDeleteLog(theLog);
						list.add(map);
					}
				}
			}
			
			bean.setDatas(JsonUtil.list2json(list));
			bean.setDataType(dataClass);
			String data = JsonUtil.bean2json(bean);
			if(list != null && !list.isEmpty()){
				// 查询下一有同步日志的数据类型
				String nextDataType = DataSynchizeUtil.queryNextDataType(clientId, user.getId(), timeStamp, endTime, DataSynchAction.DELETE.toString(), dataClass, dataTypes, synchLogService);
				// 设置response头
				if(nextDataType != null){
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.REQUEST.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.DELETE.toString());
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.BATCHDATA.toString());
					
					Map<String, String> delMap = new HashMap<String, String>();
					delMap.put("dataType", nextDataType);
					delMap.put("action", DataSynchAction.DELETE.toString());
					String returnData = JsonUtil.map2json(delMap);  // 客户端再次请求需提交的数据类型
					
					
					DataBean returnBean = new DataBean(returnData, data);
					String returnVal = JsonUtil.bean2json(returnBean);
					
					logger.info("客户端获取删除日志返回：" + returnVal);
					return returnVal;
				}else{
					// count = 0, 剩下的数据类型中未找到需同步的日志
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.BATCHDATA.toString());
					
					Map<String, String> delMap = new HashMap<String, String>();
					delMap.put("dataType", dataTypes[0]);
					delMap.put("action", DataSynchAction.TRUNCATE.toString());
					String returnData = JsonUtil.map2json(delMap);
					
					DataBean returnBean = new DataBean(returnData, data);
					String returnVal = JsonUtil.bean2json(returnBean);
					logger.info("客户端获取删除日志返回：" + returnVal);
					return returnVal;
				}
			}else{
				// 查询下一有同步日志的数据类型
				String nextDataType = DataSynchizeUtil.queryNextDataType(clientId, user.getId(), timeStamp, endTime, DataSynchAction.DELETE.toString(), dataClass, dataTypes, synchLogService);
				if(nextDataType != null){
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.REQUEST.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.DELETE.toString());
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.BATCHDATA.toString());
					
					Map<String, String> delMap = new HashMap<String, String>();
					delMap.put("dataType", nextDataType);
					delMap.put("action", DataSynchAction.DELETE.toString());
					String returnData = JsonUtil.map2json(delMap);  // 客户端再次请求需提交的数据类型
					
					DataBean returnBean = new DataBean(returnData, "");
					String returnVal = JsonUtil.bean2json(returnBean);
					logger.info("客户端获取删除日志返回1：" + returnVal);
					return returnVal;
				}else{
					// nextDataType = null, 剩下的数据类型中未找到需同步的日志
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.BATCHDATA.toString());
					
					Map<String, String> delMap = new HashMap<String, String>();
					delMap.put("dataType", dataTypes[0]);
					delMap.put("action", DataSynchAction.TRUNCATE.toString());
					String returnData = JsonUtil.map2json(delMap);
					
					DataBean returnBean = new DataBean(returnData, data);
					String returnVal = JsonUtil.bean2json(returnBean);
					logger.info("客户端获取删除日志返回：" + returnVal);
					return returnVal;
				}
			}
		}else{
			//已经到了最后一个数据类型
			if(dataClass.equals(dataTypes[dataTypes.length - 1]) || dataClass.equals(DataType.ALL.toString()) || dataClass.equals(DataType.BATCHDATA.toString())){
				res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
				res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
				
				res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
				res.setHeader(HeaderName.DATATYPE.toString(), DataType.BATCHDATA.toString());
				
				BatchDataBean bean = new BatchDataBean();
				bean.setDataType(dataTypes[0]);
				String data = JsonUtil.bean2json(bean);
				Map<String, String> delMap = new HashMap<String, String>();
				delMap.put("dataType", dataTypes[0]);
				delMap.put("action", DataSynchAction.TRUNCATE.toString());
				String returnData = JsonUtil.map2json(delMap);
				
				DataBean returnBean = new DataBean(returnData, data);
				String returnVal = JsonUtil.bean2json(returnBean);
				logger.info("客户端获取删除日志返回：" + returnVal);
				return returnVal;
			}else{
				// 查询下一有同步日志的数据类型
				String nextDataType = DataSynchizeUtil.queryNextDataType(clientId, user.getId(), timeStamp, endTime, DataSynchAction.DELETE.toString(), dataClass, dataTypes, synchLogService);
				if(nextDataType != null){
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.REQUEST.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.DELETE.toString());
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.BATCHDATA.toString());
					
					Map<String, String> delMap = new HashMap<String, String>();
					delMap.put("dataType", nextDataType);
					delMap.put("action", DataSynchAction.DELETE.toString());
					String returnData = JsonUtil.map2json(delMap);  // 客户端再次请求需提交的数据类型
					
					DataBean returnBean = new DataBean(returnData, "");
					String returnVal = JsonUtil.bean2json(returnBean);
					logger.info("客户端获取删除日志返回1：" + returnVal);
					return returnVal;
				}else{
					// nextDataType = null, 剩下的数据类型中未找到需同步的日志
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.BATCHDATA.toString());
					
					BatchDataBean bean = new BatchDataBean();
					bean.setDataType(dataTypes[0]);
					String data = JsonUtil.bean2json(bean);
					Map<String, String> delMap = new HashMap<String, String>();
					delMap.put("dataType", dataTypes[0]);
					delMap.put("action", DataSynchAction.TRUNCATE.toString());
					String returnData = JsonUtil.map2json(delMap);
					
					DataBean returnBean = new DataBean(returnData, data);
					String returnVal = JsonUtil.bean2json(returnBean);
					logger.info("客户端获取删除日志返回：" + returnVal);
					return returnVal;
				}
			}
			
		}
	}

	@Override
	@GET
	@Path("/getlogs/{timeStamp}")
	public String getSynchDataByStep(@HeaderParam(SynchConstants.HEADER_CLIENT_ID) String clientId, @PathParam("timeStamp") long timeStamp, String endTimeStr, @DefaultValue(SynchConstants.DATA_CLASS_ALL) @HeaderParam(SynchConstants.HEADER_DATATYPE) String dataClass, @DefaultValue(SynchConstants.CLIENT_SYNCH_REQUEST) @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) throws Exception {
		AccountEntity user = accountService.getUser4Session();
		String[] dataTypes = SynchDataCache.getDatasSort();
		if(dataClass.equals(DataType.FILE.toString())){ // 请求下载文件
			String downloadData = DataSynchizeUtil.queryDownloadFile(user.getId(), clientId, DataSynchAction.REQUEST.toString(), res);
			DataBean bean = new DataBean("", downloadData);
			String returnVal = JsonUtil.bean2json(bean);
			logger.info("返回下载文件数据：" + returnVal);
			return returnVal;
		}
		long endTime = System.currentTimeMillis();
		if(!StringUtil.isEmpty(endTimeStr)){
			endTime = Long.parseLong(endTimeStr);
		}else{
			SynchResult sr = synchResultService.findSynchResults(clientId, user.getId());
			if(sr.getStatus() == SynchConstants.SYNCHRONIZE_STATUS_CHECK){
				endTime = sr.getLastSynTimestamp();
			}
		}
		
		boolean isDeleteFilter = true;
		List<SynchLogEntity> result = synchLogService.findSynchLogsByTarget(clientId, user.getId(), timeStamp, endTime, dataClass, isDeleteFilter);
		// 如果存在需要同步的日志
		if(result != null && !result.isEmpty()){
			//判断回收站状态下的数据是否要返回客户端
			List<SynchLogEntity> list = new ArrayList<SynchLogEntity>();
			if(dataClass.equals(DataType.NOTE.toString())){
				SynchLogEntity lg = result.get(0);
				NoteEntity note = noteService.getNote(lg.getClassPK());
				if(note.getDeleted().intValue() != Constants.DATA_NOT_DELETED){
					boolean isReturn = DataSynchizeUtil.recyclePermission(user.getId(), note);
					// 需要返回
					if(isReturn){
						list.add(lg);
						/*//如果是添加一个回收站下的条目，必须还有一条delete操作
						if(lg.getAction().equals(DataSynchAction.ADD.toString())){
							SynchronizedLogEntity synchedLog = synchLogService.findSynchedLog(clientId, user.getId(), DataType.NOTE.toString(), lg.getClassPK(), DataSynchAction.DELETE.toString());
							if(synchedLog != null){
								synchLogService.delete(synchedLog);
							}
						}*/
					}else{
						list.add(null);
					}
				}else{
					list.add(lg);
				}
				if(result.size() > 1){
					list.add(result.get(1));
				}
			}else if(dataClass.equals(DataType.DIRECTORY.toString())){
				SynchLogEntity lg = result.get(0);
				DirectoryEntity dir = directoryService.getDirectory(lg.getClassPK());
				if(dir.getDeleted().intValue() != Constants.DATA_NOT_DELETED){
					boolean isReturn = DataSynchizeUtil.recyclePermission(user.getId(), dir);
					if(isReturn){
						list.add(lg);
						/*//如果是添加一个回收站下的条目，必须还有一条delete操作
						if(lg.getAction().equals(DataSynchAction.ADD.toString())){
							SynchronizedLogEntity synchedLog = synchLogService.findSynchedLog(clientId, user.getId(), DataType.DIRECTORY.toString(), lg.getClassPK(), DataSynchAction.DELETE.toString());
							if(synchedLog != null){
								synchLogService.delete(synchedLog);
							}
						}*/
						
					}else{
						list.add(null);
					}
				}else{
					list.add(lg);
				}
				if(result.size() > 1){
					list.add(result.get(1));
				}
			}else{
				list.addAll(result);
			}
			
			if(list != null && !list.isEmpty()){
				if(list.size() == 1){
					SynchLogEntity theLog = result.get(0);
					Map<String, Object> map = DataSynchizeUtil.parseLog(theLog);
					
					String nextDataType = DataSynchizeUtil.queryNextDataType(clientId, user.getId(), timeStamp, endTime, null, theLog.getClassName(), dataTypes, synchLogService);
					// 设置response头
					if(nextDataType !=  null){
						res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.REQUEST.toString());
						res.setHeader(HeaderName.NEXT_DATATYPE.toString(), nextDataType);
					}else{
						// count = 0, 剩下的数据类型中未找到需同步的日志
						res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.REQUEST.toString());
						res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.FILE.toString());
					}
					
					res.setHeader(HeaderName.ACTION.toString(), theLog.getAction());
					res.setHeader(HeaderName.DATATYPE.toString(), theLog.getClassName());
					
					String data = "";
					if(map != null){
						data = JsonUtil.map2json(map);
					}
					DataBean bean = new DataBean("", data);
					String returnVal = JsonUtil.bean2json(bean);
					logger.info("返回下载元数据：" + returnVal);
					return returnVal;
				}else{
					SynchLogEntity theLog = list.get(0);
					Map<String, Object> map = DataSynchizeUtil.parseLog(theLog);
					SynchLogEntity nextLog = result.get(1);
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.REQUEST.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), nextLog.getClassName());
					
					res.setHeader(HeaderName.ACTION.toString(), theLog.getAction());
					res.setHeader(HeaderName.DATATYPE.toString(), theLog.getClassName());
					String data = "";
					if(map != null){
						data = JsonUtil.map2json(map);
					}
					DataBean bean = new DataBean("", data);
					String returnVal = JsonUtil.bean2json(bean);
					logger.info("返回下载元数据：" + returnVal);
					return returnVal;
				}
			}else{
				//已经到了最后一个数据类型
				if(dataClass.equals(dataTypes[dataTypes.length - 1]) || dataClass.equals(DataType.ALL.toString())){
					String downloadData = DataSynchizeUtil.queryDownloadFile(user.getId(), clientId, DataSynchAction.REQUEST.toString(), res);
					DataBean bean = new DataBean("", downloadData);
					return JsonUtil.bean2json(bean);
				}else if(dataClass.equals(DataType.ALL.toString())){    //所有数据类型均查询不到需要同步的日志
					res.setHeader(SynchConstants.HEADER_ACTION, DataSynchAction.FINISH.toString());
					res.setHeader(SynchConstants.HEADER_DATATYPE, DataType.ALL.toString());
					
					res.setHeader(SynchConstants.HEADER_NEXT_ACTION, DataSynchAction.FINISH.toString());
					res.setHeader(SynchConstants.HEADER_NEXT_DATATYPE, DataType.ALL.toString());
					DataBean bean = new DataBean("", "");
					String returnVal = JsonUtil.bean2json(bean);
					logger.info("返回下载数据：" + returnVal);
					return returnVal;
				}
			}
		}else{
			String nextDataType = DataSynchizeUtil.queryNextDataType(clientId, user.getId(), timeStamp, endTime, null, dataClass, dataTypes, synchLogService);
			if(nextDataType !=  null){
				res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.REQUEST.toString());
				res.setHeader(HeaderName.NEXT_DATATYPE.toString(), nextDataType);
			}else{
				// count = 0, 剩下的数据类型中未找到需同步的日志
				res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.REQUEST.toString());
				res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.FILE.toString());
			}
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), dataClass);
			
			DataBean bean = new DataBean("", "");
			String returnVal = JsonUtil.bean2json(bean);
			logger.info("返回下载数据01：" + returnVal);
			return returnVal;
		}
		DataBean bean = new DataBean("", "");
		String returnVal = JsonUtil.bean2json(bean);
		logger.info("返回下载数据00：" + returnVal);
		return returnVal;
	}
	
	@Deprecated
	@Override
	@GET
	@Path("/get/{logId}")
	public String getSynchDataByLogId(@PathParam("logId") String logId, @Context HttpServletResponse res) throws NumberFormatException, Exception {
		AccountEntity user = accountService.getUser4Session();
		synchLogService.findSynchLogsBySQL(user.getClientId(), user.getId(), Long.parseLong(logId), DataType.ALL.toString());
		SynchLogEntity log = synchLogService.getSynchLog(logId);
		Map<String, Object> map = DataSynchizeUtil.parseLog(log);
		return JsonUtil.map2json(map);
	}
	
	@Override
	@GET
	@Path("/get/role")
	public String initRole(@HeaderParam(SynchConstants.HEADER_CLIENT_ID) String clientId, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) {
		List<Role> list = roleService.findAllRoles();
		
		res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.REQUEST.toString());
		res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.RESOURCE.toString());
		
		res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
		res.setHeader(HeaderName.DATATYPE.toString(), DataType.ROLE.toString());
		res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
		
		return JsonUtil.bean2json(new DataBean("", JsonUtil.list2json(list)));
	}
	
	@Override
	@GET
	@Path("/get/resource")
	public String initResouce(@HeaderParam(SynchConstants.HEADER_CLIENT_ID) String clientId, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) {
		ClassName cn = DataCache.getSubjectResource();
		
		res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.REQUEST.toString());
		res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.RESOURCEACTION.toString());
		
		res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
		res.setHeader(HeaderName.DATATYPE.toString(), DataType.RESOURCE.toString());
		res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
		
		DataBean dataBean = new DataBean("", JsonUtil.bean2json(cn));
		return JsonUtil.bean2json(dataBean);
	}
	
	@Override
	@GET
	@Path("/get/resourceaction")
	public String initResouceAction(@HeaderParam(SynchConstants.HEADER_CLIENT_ID) String clientId, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) {
		List<ResourceAction> actionList = resourceActionService.findActionsByName(Constants.SUBJECT_MODULE_NAME);
		
		res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.REQUEST.toString());
		res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.RESOURCEPERMISSION.toString());
		
		res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
		res.setHeader(HeaderName.DATATYPE.toString(), DataType.RESOURCEACTION.toString());
		res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
		
		DataBean dataBean = new DataBean("", JsonUtil.list2json(actionList));
		return JsonUtil.bean2json(dataBean);
	}
	
	@Override
	@GET
	@Path("/get/resourcepermission")
	public String initResoucePermission(@HeaderParam(SynchConstants.HEADER_CLIENT_ID) String clientId, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) {
		ClassName cn = DataCache.getSubjectResource();
		List<ResourcePermission> list = resourcePermissionService.findResourcePermission(Constants.SUBJECT_MODULE_NAME, String.valueOf(cn.getClassNameId()));
		
		res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.REQUEST.toString());
		res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.RESOURCEPERMISSION.toString());
		
		res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
		res.setHeader(HeaderName.DATATYPE.toString(), DataType.RESOURCEACTION.toString());
		res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
		
		DataBean dataBean = new DataBean("", JsonUtil.list2json(list));
		return JsonUtil.bean2json(dataBean);
	}
	
	@Override
	@POST
	@Path("/send/client/a/{clientType}")
	public String registerClient(@PathParam("clientType") String clientType, @Context HttpServletResponse res) {
		String clientId = dataInitService.registerClient(clientType,"PC");
		
		res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
		res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.CLIENT.toString());
		
		res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.REQUEST.toString());
		res.setHeader(HeaderName.DATATYPE.toString(), DataType.ROLE.toString());
		res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
		
		DataBean dataBean = new DataBean("", JsonUtil.string2json(clientId));
		return JsonUtil.bean2json(dataBean);
	}
	
	/**
	 * 同步开始结束
	 */
	@SuppressWarnings("unused")
	@Override
	@POST
	@Path("/checkcfg")
	public String checkConfig(@HeaderParam(SynchConstants.HEADER_CLIENT_ID) String clientId, @FormParam("data") String data, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res){
		SynchResult sr = (SynchResult) JsonUtil.getObject4JsonString(data, SynchResult.class);
		AccountEntity user = accountService.getUser4Session();
		if(action.equals(DataSynchAction.FINISH.toString())){
			logger.info(clientId + ",用户ID:" + user.getId() + "同步完成 ！！！");
			sr = synchResultService.findSynchResults(clientId, user.getId());
			long finishTime = System.currentTimeMillis();
			if(sr == null){
				sr = new SynchResult();
				sr.setClientId(clientId);
				sr.setUserId(user.getId());
				sr.setLastSynTimestamp(finishTime);
				sr.setStatus(SynchConstants.SYNCHRONIZE_STATUS_CHECK);
				synchResultService.addSynResult(sr);
			}else{
				sr.setLastSynTimestamp(finishTime);  // 完成更新结束时间
				sr.setStatus(SynchConstants.SYNCHRONIZE_STATUS_CHECK);
				synchResultService.updateSynResult(sr);
			}
			try {
				String[] actionSort = SynchDataCache.getActionSort();
				
				logger.info(clientId + ",用户ID:[" + user.getId() + "]同步完成,查询同步过程中其它客户端是否有日志生成["+actionSort[0]+"]:" + sr.getBeginTimestamp() + " -- " + sr.getLastSynTimestamp());
				List<SynchLogEntity> logList = synchLogService.findTruncSynchLogs(clientId, user.getId(), sr.getBeginTimestamp(), sr.getLastSynTimestamp(), DataType.BATCHDATA.toString());
				logger.info(clientId + ",用户ID:" + user.getId() + "同步完成,查询同步过程中其它客户端是否有日志生成, 查询结果：" + logList);
				if(logList != null && logList.size() > 0){
					SynchLogEntity log = logList.get(0);
					Map<String, String> delMap = new HashMap<String, String>();
					delMap.put("dataType", log.getClassName());
					delMap.put("action", DataSynchAction.TRUNCATE.toString());
					String result = getTruncateLogs(JsonUtil.map2json(delMap), clientId, sr.getBeginTimestamp(), sr.getLastSynTimestamp() + "", DataType.ALL.toString(), DataSynchAction.REQUEST.toString(), res);
					return result;
				}
				
				logger.info(clientId + ",用户ID:[" + user.getId() + "]同步完成,查询同步过程中其它客户端是否有日志生成["+actionSort[1]+"]:" + sr.getBeginTimestamp() + " -- " + sr.getLastSynTimestamp());
				logList = synchLogService.findSynchLogsByTarget(clientId, user.getId(), sr.getBeginTimestamp(), sr.getLastSynTimestamp(), DataType.BATCHDATA.toString(), false);
				logger.info(clientId + ",用户ID:" + user.getId() + "同步完成,查询同步过程中其它客户端是否有日志生成, 查询结果：" + logList);
				if(logList != null && logList.size() > 0){
					SynchLogEntity log = logList.get(0);
					Map<String, String> delMap = new HashMap<String, String>();
					delMap.put("dataType", log.getClassName());
					delMap.put("action", DataSynchAction.DELETE.toString());
					String result = getDeleteLogs(JsonUtil.map2json(delMap), clientId, sr.getBeginTimestamp(), sr.getLastSynTimestamp() + "", DataType.ALL.toString(), DataSynchAction.REQUEST.toString(), res);
					return result;
				}
				
				logger.info(clientId + ",用户ID:[" + user.getId() + "]同步完成,查询同步过程中其它客户端是否有日志生成["+actionSort[2]+"]:" + sr.getBeginTimestamp() + " -- " + sr.getLastSynTimestamp());
				logList = synchLogService.findSynchLogsByTarget(clientId, user.getId(), sr.getBeginTimestamp(), sr.getLastSynTimestamp(), DataType.ALL.toString(), true);
				logger.info(clientId + ",用户ID:" + user.getId() + "同步完成,查询同步过程中其它客户端是否有日志生成, 查询结果：" + logList);
				if(logList != null && logList.size() > 0){
					SynchLogEntity log = logList.get(0);
					
					String result = getSynchDataByStep(clientId, sr.getBeginTimestamp(), sr.getLastSynTimestamp() + "", log.getClassName(), DataSynchAction.REQUEST.toString(), res);
					return result;
				}
				
				//同步彻底结束
				sr.setStatus(SynchConstants.SYNCHRONIZE_STATUS_END); //同步流程完成，更新状态
				synchResultService.updateSynResult(sr);
				long synchTimeStamp = synchLogService.deleteSynchedLogs(clientId, user.getId());
				
				return JsonUtil.bean2json(sr);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return JsonUtil.bean2json(sr);
			
		}else{
			logger.info(clientId + ",用户ID:" + user.getId() + "同步开始，检查上次同步完成时间戳：" + sr.getLastSynTimestamp());
			long synchTimeStamp = synchLogService.deleteSynchedLogs(clientId, user.getId());
			
			ClientEntity client = dataInitService.getClient(clientId);
			if(client == null){
				dataInitService.registerClient(clientId, "PC");
			}
			
			long beginTime = sr.getBeginTimestamp();
			logger.info("同步开始时间：" + beginTime);
			sr = synchResultService.findSynchResults(clientId, user.getId());
			if(sr == null){
				sr = new SynchResult();
				sr.setClientId(clientId);
				sr.setUserId(user.getId());
				sr.setBeginTimestamp(beginTime);
				sr.setStatus(SynchConstants.SYNCHRONIZE_STATUS_BEGIN); //同步流程开始
				synchResultService.addSynResult(sr);
			}else{
				sr.setBeginTimestamp(beginTime);
				sr.setStatus(SynchConstants.SYNCHRONIZE_STATUS_BEGIN); //同步流程开始
				synchResultService.updateSynResult(sr);
			}
			
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.REQUEST.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.NOTE.toString());
			
			String[] dataTypes = SynchDataCache.getReverseDatasSort();//{DataType.NOTE.toString(), DataType.DIRECTORY.toString()};
			Map<String, String> delMap = new HashMap<String, String>();
			delMap.put("dataType", dataTypes[0]);
			delMap.put("action", DataSynchAction.TRUNCATE.toString());
			String returnData = JsonUtil.map2json(delMap);
			return returnData;
		}
	}
	
	@Override
	@POST
	@Path("/userLogin")
	public String checkUser(@HeaderParam(SynchConstants.HEADER_CLIENT_ID) String clientId, @FormParam("userName") String userName, @FormParam("token") String token, @Context HttpServletRequest request){
		String[] cookieTokens = DataSynchizeUtil.decodeCookie(token);
		
		final String presentedSeries = cookieTokens[0];
        final String presentedToken = cookieTokens[1];
        
        PersistentRememberMeToken tk = tokenRepository.getTokenForSeries(presentedSeries);
        if (tk == null) {
            // No series match, so we can't authenticate using this cookie
            throw new RememberMeAuthenticationException("No persistent token found for series id: " + presentedSeries);
        }

        // We have a match for this user/series combination
        if (!presentedToken.equals(tk.getTokenValue())) {
            // Token doesn't match series value. Delete all logins for this user and throw an exception to warn them.
            tokenRepository.removeUserTokens(tk.getUsername());

            throw new CookieTheftException("Invalid remember-me token (Series/token) mismatch. Implies previous cookie theft attack.");
        }
        
        if(!userName.equals(tk.getUsername())){
        	throw new RememberMeAuthenticationException("Remember-me token doesn't match the userName:" + userName);
        }
        
        if (tk.getDate().getTime() + AbstractRememberMeServices.TWO_WEEKS_S * 1000L < System.currentTimeMillis()) {
        	tokenRepository.removeUserTokens(tk.getUsername());
        	throw new RememberMeAuthenticationException("Remember-me token has expired");
        }
        
        AccountEntity user = accountService.findUserByAccount(tk.getUsername());
        if(user != null && user.isEnabled() && user.getDeleted() == Constants.DATA_NOT_DELETED){
        	return "true";
        }
		return "false";
	}
	
	@Override
	@DELETE
	@Path("/send/client/d/{clientId}")
	public String deleteClient(@PathParam("clientId") String clientId) {
		dataInitService.deleteClient(clientId);
		return "true";
	}
	
	@POST
	@Path("/send/login")
	public String tokenLogin() {
		return "true";
	}

	@Override
	@POST
	@Path("/send/subject/a")
	public String addSubject(@FormParam("data") String data, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) {
		logger.info("添加专题信息: " + data);
		AccountEntity user = accountService.getUser4Session();
		if(!action.equals(DataSynchAction.FINISH.toString())){
			SubjectEntity subject = (SubjectEntity) JsonUtil.getObject4JsonString(data, SubjectEntity.class);
			
			subject.setCreateUser(user.getId());
			subjectService.addSubject(subject, user.getId());
			
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.SUBJECT.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.SUBJECT.toString());
		}else{
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.SUBJECTUSER.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.SUBJECT.toString());
		}
		DataBean bean = new DataBean("", "");
		return JsonUtil.bean2json(bean);
	}

	@Override
	@POST
	@Path("/send/subject/u/{timeStamp}")
	public String updateSubject(@FormParam("data") String data, @PathParam("timeStamp") long timeStamp, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) throws Exception {
		logger.info("更新专题信息 ！！！");
		AccountEntity user = accountService.getUser4Session();
		ResponseStatus rs =new ResponseStatus();
		if(!action.equals(DataSynchAction.FINISH.toString())){
			SubjectEntity subject = (SubjectEntity) JsonUtil.getObject4JsonString(data, SubjectEntity.class);
			boolean hasPermission = DataSynchizeUtil.hasPermission(user.getId(), subject.getId(), ActionName.UPDATE);
			if(!hasPermission && needAuth){
				logger.warn("用户操作权限不足", new InsufficientAuthenticationException("用户没有进行此操作的权限： 【更新专题】, 请联系专题管理员。"));
				SubjectEntity sub = subjectService.getSubject(subject.getId());
				
				DataSynchizeUtil.saveBanLog(DataType.SUBJECT.toString(), DataSynchAction.UPDATE.toString(), subject.getId(), sub.getUpdateUser(), user.getId(), sub.getUpdateTimeStamp(), System.currentTimeMillis());
				
				res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
				res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.SUBJECT.toString());
				
				res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.BAN.toString());
				res.setHeader(HeaderName.DATATYPE.toString(), DataType.SUBJECT.toString());
				
				DataBean bean = new DataBean();
				String returnVal = JsonUtil.bean2json(bean);
				logger.info("权限不足，不能修改专题信息，恢复专题数据：" + returnVal);
				return returnVal;
			}
			
			SubjectEntity sub = subjectService.getSubject(subject.getId());
			SynchLogEntity log = synchLogService.findLogByData(user.getClientId(), user.getId(), subject.getUpdateTimeStamp() + 1, SynchConstants.DATA_CLASS_SUBJECT, sub.getId());
			if(sub != null && sub.getDeleted() == Constants.DATA_NOT_DELETED){  // 服务器存在此专题，并且不是删除状态
				//服务器上不存在该数据其它操作日志，或者有修改操作日志并且操作早于此次操作，才更新数据库数据
				if(log == null || (log.getAction().equals(DataSynchAction.UPDATE.toString()) && log.getOperateTime() < subject.getUpdateTimeStamp())){
					subject.setUpdateUser(user.getId());
					subject.setUpdateUserId(user.getId());
					subject.setDeleted(Constants.DATA_NOT_DELETED);
					subject.setStatus(Constants.ENABLED);
					ReflectionUtils.copyBeanProperties(subject, sub);
					subjectService.updateSubject(sub);
					
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.SUBJECT.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.SUBJECT.toString());
				}
			}else{
				//header中添加控制位
				res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
				res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.SUBJECT.toString());
				
				res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.TRUNCATE.toString());
				res.setHeader(HeaderName.DATATYPE.toString(), DataType.SUBJECT.toString());
				
				String timestamp = "";
				String id = "";
				String updateUser = "";
				if(sub != null && sub.getDeleted() == Constants.DATA_DELETED){
					timestamp = sub.getUpdateTimeStamp() + "";
					id = sub.getId();
					updateUser = sub.getUpdateUser();
				}else{
					timestamp = log.getOperateTime() + "";
					id = log.getClassPK();
					updateUser = log.getOperateUser();
				}
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("id", id);
				map.put("className", DataType.SUBJECT.toString());
				map.put("operation", DataSynchAction.TRUNCATE.toString());
				map.put("updateUserId", updateUser);
				map.put("updateTimeStamp", timestamp);
				
				String dataStr = JsonUtil.map2json(map);
				DataBean bean = new DataBean("", dataStr);
				return JsonUtil.bean2json(bean);
			}
			if(log != null){
				if(log.getAction().equals(DataSynchAction.TRUNCATE.toString())){
					
					//header中添加控制位
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.SUBJECT.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.TRUNCATE.toString());
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.SUBJECT.toString());
					
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("id", log.getClassPK());
					map.put("className", DataType.SUBJECT.toString());
					map.put("operation", DataSynchAction.TRUNCATE.toString());
					map.put("updateUserId", log.getOperateUser());
					map.put("updateTimeStamp", log.getOperateTime());
					
					String dataStr = JsonUtil.map2json(map);
					DataBean bean = new DataBean("", dataStr);
					return JsonUtil.bean2json(bean);
				}else if(log.getAction().equals(DataSynchAction.DELETE.toString())){
					
					//header中添加控制位
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.SUBJECT.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.DELETE.toString());
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.SUBJECT.toString());
					
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("id", log.getClassPK());
					map.put("className", DataType.SUBJECT.toString());
					map.put("operation", DataSynchAction.DELETE.toString());
					map.put("updateUserId", log.getOperateUser());
					map.put("updateTimeStamp", log.getOperateTime());
					
					String dataStr = JsonUtil.map2json(map);
					DataBean bean = new DataBean("", dataStr);
					return JsonUtil.bean2json(bean);
				}else if(log.getAction().equals(DataSynchAction.UPDATE.toString()) && log.getOperateTime() >= subject.getUpdateTimeStamp()){
					//header中添加控制位
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.SUBJECT.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.UPDATE.toString());
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.SUBJECT.toString());
					
					sub.setClassName(DataType.SUBJECT.toString());
					sub.setOperation(DataSynchAction.UPDATE.toString());
					DataBean bean = new DataBean("", JsonUtil.bean2json(sub));
					return JsonUtil.bean2json(bean);
				}else {
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.SUBJECT.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.SUBJECT.toString());
				}
			}
		}else{
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.SUBJECTUSER.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.SUBJECT.toString());
		}
		DataBean bean = new DataBean("", "");
		return JsonUtil.bean2json(bean);
	}

	@Override
	@DELETE
	@Path("/send/subject/d/{id}")
	public String deleteSubject(@PathParam("id") String id, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) {
		logger.info("删除专题信息 ！！！");
		if(!action.equals(DataSynchAction.FINISH.toString())){
			SubjectEntity subject = subjectService.getSubject(id);
			if(subject != null && subject.getDeleted() == Constants.DATA_NOT_DELETED){
				AccountEntity user = accountService.getUser4Session();
				
				boolean hasPermission = DataSynchizeUtil.hasPermission(user.getId(), subject.getId(), ActionName.DELETE_SUBJECT);
				if(!hasPermission && needAuth){
					throw new InsufficientAuthenticationException("用户没有进行此操作的权限：" + ActionName.DELETE_SUBJECT + ", 请联系专题管理员。");
				}
				subject.setUpdateUser(user.getId());
				subjectService.deleteSubject(subject);
			}
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.SUBJECT.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.SUBJECT.toString());
		} else {
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.SUBJECT.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.SUBJECT.toString());
		}
		return new ResponseStatus().toString();
	}

	@Override
	@POST
	@Path("/send/directory/a")
	public String addDirectory(@FormParam("data") String data, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) throws InsufficientAuthenticationException{
		logger.info("添加目录信息 :" + data);
		if(!action.equals(DataSynchAction.FINISH.toString())){
			DirectoryEntity dir = (DirectoryEntity) JsonUtil.getObject4JsonString(data, DirectoryEntity.class);
			AccountEntity user = accountService.getUser4Session();
			
			if(!StringUtil.isEmpty(dir.getParentId())){
				boolean isBan = directoryService.inDirBlackList(user.getId(), dir.getParentId());
				if(isBan){
					logger.warn("用户在黑名单中", new InsufficientAuthenticationException("用户在黑名单中"));
					
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.DIRECTORY.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.BAN.toString());
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.DIRECTORY.toString());
					
					DataBean bean = new DataBean();
					String returnVal = JsonUtil.bean2json(bean);
					logger.info("用户在黑名单中：" + returnVal);
					return returnVal;
				}
			}
			
			boolean hasPermission = DataSynchizeUtil.hasPermission(user.getId(), dir.getSubjectId(), ActionName.ADD_DIRECTORY);
			if(!hasPermission && needAuth){
				logger.warn("用户操作权限不足", new InsufficientAuthenticationException("用户没有进行此操作的权限： 【添加目录】, 请联系专题管理员。"));
				
				res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
				res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.DIRECTORY.toString());
				
				res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.BAN.toString());
				res.setHeader(HeaderName.DATATYPE.toString(), DataType.DIRECTORY.toString());
				
				DataBean bean = new DataBean();
				String returnVal = JsonUtil.bean2json(bean);
				logger.info("权限不足，不能添加目录，删除新增目录数据：" + returnVal);
				return returnVal;
			}
			
			dir.setCreateUser(user.getId());
			dir.setDeleted(Constants.DATA_NOT_DELETED);
			directoryService.addDirectory(dir);
			
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.DIRECTORY.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.DIRECTORY.toString());
		}else{
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.TAG.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.DIRECTORY.toString());
		}
		DataBean bean = new DataBean("", "");
		return JsonUtil.bean2json(bean);
	}

	@Override
	@POST
	@Path("/send/directory/u/{timeStamp}")
	public String updateDirectory(@FormParam("data") String data, @PathParam("timeStamp") long timeStamp, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) throws Exception {
		logger.info("更新目录信息 ！！！");
		if(!action.equals(DataSynchAction.FINISH.toString())){
			AccountEntity user = accountService.getUser4Session();
			//客户端目录数据
			DirectoryEntity directory = (DirectoryEntity) JsonUtil.getObject4JsonString(data, DirectoryEntity.class);
			
			//服务器中的目录数据
			DirectoryEntity dir = directoryService.getDirectory(directory.getId());
			SynchLogEntity log = synchLogService.findLogByData(user.getClientId(), user.getId(), timeStamp, SynchConstants.DATA_CLASS_DIRECTORY, directory.getId());
			if(dir == null){
				//header中添加控制位
				res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
				res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.DIRECTORY.toString());
				
				String op = DataSynchAction.TRUNCATE.toString();
				res.setHeader(HeaderName.ACTION.toString(), op);
				res.setHeader(HeaderName.DATATYPE.toString(), DataType.DIRECTORY.toString());
				
				String timestamp = log.getOperateTime() + "";
				String id = log.getClassPK();
				String updateUser = log.getOperateUser();
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("id", id);
				map.put("className", DataType.DIRECTORY.toString());
				map.put("operation", op);
				map.put("updateUserId", updateUser);
				map.put("updateTimeStamp", timestamp);
				
				String dataStr = JsonUtil.map2json(map);
				DataBean bean = new DataBean("", dataStr);
				return JsonUtil.bean2json(bean);
			}
			
			boolean saveLog = false;
			
			boolean isThisBan = directoryService.inDirBlackList(user.getId(), dir.getId());
			if(!saveLog){
				if(isThisBan){
					logger.warn("用户在目录黑名单中", new InsufficientAuthenticationException("用户在目录黑名单中"));
					saveLog = true;
					
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.DIRECTORY.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.BAN.toString());
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.DIRECTORY.toString());
					
				}
			}
			
			if(!saveLog){
				if(!StringUtil.isEmpty(dir.getParentId())){
					boolean isBan = directoryService.inDirBlackList(user.getId(), dir.getParentId());
					if(isBan){
						logger.warn("用户在父目录黑名单中", new InsufficientAuthenticationException("用户在父目录黑名单中"));
						saveLog = true;
						
						res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
						res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.DIRECTORY.toString());
						
						res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.BAN.toString());
						res.setHeader(HeaderName.DATATYPE.toString(), DataType.DIRECTORY.toString());
					}
				}
			}
			
			if(!saveLog){
				boolean hasPermission = DataSynchizeUtil.hasPermission(user.getId(), directory.getSubjectId(), ActionName.UPDATE_DIRECTORY);
				if(!hasPermission && needAuth){
					logger.warn("用户操作权限不足", new InsufficientAuthenticationException("用户没有进行此操作的权限： 【修改目录】, 请联系专题管理员。"));
					saveLog = true;
					
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.DIRECTORY.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.BAN.toString());
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.DIRECTORY.toString());
					
				}
			}
			
			if(saveLog){
				DataSynchizeUtil.saveBanLog(DataType.DIRECTORY.toString(), DataSynchAction.UPDATE.toString(), dir.getId(), dir.getUpdateUser(), user.getId(), dir.getUpdateTimeStamp(), System.currentTimeMillis());
				
				DataBean bean = new DataBean();
				String returnVal = JsonUtil.bean2json(bean);
				return returnVal;
			}
			
			if (dir != null && (dir.getDeleted() == Constants.DATA_NOT_DELETED || dir.getDeleted() == Constants.DATA_NOTSEARCH)) {
				//服务器上不存在该数据其它操作日志，或者有修改操作日志并且操作早于此次操作，才更新数据库数据
				if(log == null || (log.getAction().equals(DataSynchAction.UPDATE.toString()) && log.getOperateTime() < directory.getUpdateTimeStamp())){
					directory.setUpdateUser(user.getId());
					directory.setUpdateUserId(user.getId());
					directory.setDeleted(Constants.DATA_NOT_DELETED);
					ReflectionUtils.copyBeanProperties(directory, dir);
					directoryService.updateDirectory(dir);
					
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.DIRECTORY.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.DIRECTORY.toString());
				}
			}else{
				
				//header中添加控制位
				res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
				res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.DIRECTORY.toString());
				
				String op = DataSynchAction.DELETE.toString();
				res.setHeader(HeaderName.ACTION.toString(), op);
				res.setHeader(HeaderName.DATATYPE.toString(), DataType.DIRECTORY.toString());
				
				res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
				
				String timestamp = "";
				String id = "";
				String updateUser = "";
				if(dir != null && dir.getDeleted() == Constants.DATA_DELETED){
					timestamp = dir.getUpdateTimeStamp() + "";
					id = dir.getId();
					updateUser = dir.getUpdateUser();
				}else{
					timestamp = log.getOperateTime() + "";
					id = log.getClassPK();
					updateUser = log.getOperateUser();
				}
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("id", id);
				map.put("className", DataType.DIRECTORY.toString());
				map.put("operation", op);
				map.put("updateUserId", updateUser);
				map.put("updateTimeStamp", timestamp);
				
				String dataStr = JsonUtil.map2json(map);
				DataBean bean = new DataBean("", dataStr);
				return JsonUtil.bean2json(bean);
			}
			
			if(log != null){
				if(log.getAction().equals(DataSynchAction.TRUNCATE.toString())){
					//header中添加控制位
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.DIRECTORY.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.TRUNCATE.toString());
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.DIRECTORY.toString());
					
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("id", log.getClassPK());
					map.put("className", DataType.DIRECTORY.toString());
					map.put("operation", DataSynchAction.TRUNCATE.toString());
					map.put("updateUserId", log.getOperateUser());
					map.put("updateTimeStamp", log.getOperateTime());
					
					String dataStr = JsonUtil.map2json(map);
					DataBean bean = new DataBean("", dataStr);
					return JsonUtil.bean2json(bean);
				}else if(log.getAction().equals(DataSynchAction.DELETE.toString())){
					//header中添加控制位
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.DIRECTORY.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.DELETE.toString());
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.DIRECTORY.toString());
					
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("id", log.getClassPK());
					map.put("className", DataType.DIRECTORY.toString());
					map.put("operation", DataSynchAction.DELETE.toString());
					map.put("updateUserId", log.getOperateUser());
					map.put("updateTimeStamp", log.getOperateTime());
					
					String dataStr = JsonUtil.map2json(map);
					DataBean bean = new DataBean("", dataStr);
					return JsonUtil.bean2json(bean);
				}else if(log.getAction().equals(DataSynchAction.UPDATE.toString()) && log.getOperateTime() > directory.getUpdateTimeStamp()){
					
					//header中添加控制位
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.DIRECTORY.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.UPDATE.toString());
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.DIRECTORY.toString());
					
					dir.setClassName(DataType.NOTE.toString());
					dir.setOperation(DataSynchAction.UPDATE.toString());
					DataBean bean = new DataBean("", JsonUtil.bean2json(dir));
					return JsonUtil.bean2json(bean);
				}else {
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.DIRECTORY.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.DIRECTORY.toString());
				}
			}
		}else{
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.TAG.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.DIRECTORY.toString());
		}
		DataBean bean = new DataBean("", "");
		return JsonUtil.bean2json(bean);
	}

	@Override
	@DELETE
	@Path("/send/directory/d/{id}")
	public String deleteDirectory(@PathParam("id") String id, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) {
		logger.info("删除目录信息 ！！！");
		if(!action.equals(DataSynchAction.FINISH.toString())){
			DirectoryEntity dir = directoryService.getDirectory(id);
			if(dir != null && dir.getDeleted() == Constants.DATA_NOT_DELETED){
				AccountEntity user = accountService.getUser4Session();
				
				boolean hasPermission = DataSynchizeUtil.hasPermission(user.getId(), dir.getSubjectId(), ActionName.DELETE_DIRECTORY);
				if(!hasPermission && needAuth){
					throw new InsufficientAuthenticationException("用户没有进行此操作的权限：" + ActionName.DELETE_DIRECTORY + ", 请联系专题管理员。");
				}
				
				dir.setUpdateUser(user.getId());
				directoryService.markDelDirectory(dir);
			}
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.DIRECTORY.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.DIRECTORY.toString());
		}else {
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.DIRECTORY.toString());
		}
		res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
		return new ResponseStatus().toString();
	}

	@Override
	@POST
	@Path("/send/directoryblack/a/{directoryId}/{userId}")
	public String addDirectoryBlack(@PathParam("directoryId") String directoryId, @PathParam("userId") String userId, @Context HttpServletResponse res) {
		directoryService.blackListedAllUser(userId, directoryId, System.currentTimeMillis());
		return new ResponseStatus().toString();
	}

	@Override
	@DELETE
	@Path("/send/directoryblack/d/{directoryId}/{userId}")
	public String deleteDirectoryBlack(@PathParam("directoryId") String directoryId, @PathParam("userId") String userId, @Context HttpServletResponse res) {
		directoryService.removeUserALL4lacklist(userId, directoryId, System.currentTimeMillis());
		return new ResponseStatus().toString();
	}

	@Override
	@POST
	@Path("/send/note/a")
	public String addNote(@FormParam("data") String data, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) {
		logger.info("添加条目信息 :" + data);
		if(!action.equals(DataSynchAction.FINISH.toString())){
			AccountEntity user = accountService.getUser4Session();
			NoteEntity note = (NoteEntity) JsonUtil.getObject4JsonString(data, NoteEntity.class);
			
			if(!StringUtil.isEmpty(note.getDirId())){
				boolean isBan = directoryService.inDirBlackList(user.getId(), note.getDirId());
				if(isBan){
					logger.warn("用户在黑名单中", new InsufficientAuthenticationException("用户在黑名单中"));
					
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.NOTE.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.BAN.toString());
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.NOTE.toString());
					
					Map<String, String> delMap = new HashMap<String, String>();
					delMap.put("dataType", DataType.NOTE.toString());
					delMap.put("action", DataSynchAction.TRUNCATE.toString());
					
					DataBean bean = new DataBean(JsonUtil.map2json(delMap), "");
					String returnVal = JsonUtil.bean2json(bean);
					logger.info("用户在黑名单中：" + returnVal);
					return returnVal;
				}
			}
			
			boolean hasPermission = DataSynchizeUtil.hasPermission(user.getId(), note.getSubjectId(), ActionName.ADD_NOTE);
			if(!hasPermission && needAuth){
				logger.warn("用户操作权限不足", new InsufficientAuthenticationException("用户没有进行此操作的权限： 【添加条目】, 请联系专题管理员。"));
				
				res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
				res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.NOTE.toString());
				
				res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.BAN.toString());
				res.setHeader(HeaderName.DATATYPE.toString(), DataType.NOTE.toString());
				
				Map<String, String> delMap = new HashMap<String, String>();
				delMap.put("dataType", DataType.NOTE.toString());
				delMap.put("action", DataSynchAction.TRUNCATE.toString());
				
				DataBean bean = new DataBean(JsonUtil.map2json(delMap), "");
				String returnVal = JsonUtil.bean2json(bean);
				logger.info("权限不足，不能添加目录，删除新增目录数据：" + returnVal);
				return returnVal;
			}
			
			return addNote(note, res);
		}else{
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.NOTETAG.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.NOTE.toString());
		}
		DataBean bean = new DataBean("", "");
		return JsonUtil.bean2json(bean);
	}
	
	private String addNote(NoteEntity note, HttpServletResponse res) {
		AccountEntity user = accountService.getUser4Session();
		if(!StringUtil.isEmpty(note.getContent())){
			note.setMd5(MD5FileUtil.getMD5String(note.getContent()));
		}
		note.setCreateUser(user.getId());
		note.setDeleted(Constants.DATA_NOT_DELETED);
		try {
			noteService.addNote(note);
		} catch (Exception e) {
		}
		
		//保存note的html文件记录，等下通知客户端上传
		DataSynchizeUtil.addNoteHtmlFile(note, user);
		
		res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
		res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.NOTE.toString());
		
		res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
		res.setHeader(HeaderName.DATATYPE.toString(), DataType.NOTE.toString());
		DataBean bean = new DataBean("", "");
		return JsonUtil.bean2json(bean);
	}
	
	@Override
	@POST
	@Path("/send/notehtml/a")
	public String addNoteHtml(@Multipart("data") String data,@Multipart("noteFile") InputStream ins, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletRequest request, @Context HttpServletResponse res) throws IOException {
		logger.info("上传条目HTML数据 :" + data);
		if(action == null || !action.equals(DataSynchAction.FINISH.toString())){
			AccountEntity user = accountService.getUser4Session();
			OutputStream ous = null;
			NoteEntity note = (NoteEntity) JsonUtil.getObject4JsonString(data, NoteEntity.class);
			
			if(!StringUtil.isEmpty(note.getDirId())){
				boolean isBan = directoryService.inDirBlackList(user.getId(), note.getDirId());
				if(isBan){
					logger.warn("用户在黑名单中", new InsufficientAuthenticationException("用户在黑名单中"));
					
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.NOTE.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.BAN.toString());
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.NOTE.toString());
					
					DataBean bean = new DataBean();
					String returnVal = JsonUtil.bean2json(bean);
					logger.info("用户在黑名单中：" + returnVal);
					return returnVal;
				}
			}
			
			boolean hasPermission = DataSynchizeUtil.hasPermission(user.getId(), note.getSubjectId(), ActionName.ADD_NOTE);
			if(!hasPermission && needAuth){
				logger.warn("用户操作权限不足", new InsufficientAuthenticationException("用户没有进行此操作的权限： 【添加条目】, 请联系专题管理员。"));
				
				res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
				res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.NOTE.toString());
				
				res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.BAN.toString());
				res.setHeader(HeaderName.DATATYPE.toString(), DataType.NOTE.toString());
				
				DataBean bean = new DataBean();
				String returnVal = JsonUtil.bean2json(bean);
				logger.info("权限不足，不能添加条目，删除新增条目数据：" + returnVal);
				return returnVal;
			}
			
			// 条目HTML存放路径
			String savePath = FilePathUtil.getNoteHtmlPath(note);
			String htmlFileName = savePath + note.getId();
			
			File folder = new File(savePath);
			if (!folder.exists()) {
				folder.mkdirs();
			}
			File file = new File(htmlFileName + ".zip" + ".tmp");
			File html = new File(htmlFileName + ".zip");
			if(html.exists()){
				boolean deleted = html.delete();
				if(!deleted){
					htmlFileName = htmlFileName + "副本";
					html = new File(htmlFileName + ".zip");
				}
			}
			try {
				//ins = request.getInputStream();
				ous = new FileOutputStream(file);
				int n = 0;
				int buffer = ins.available();
				byte[] bytes = new byte[buffer];
				while ((n = ins.read(bytes)) != -1) {
					ous.write(bytes, 0, n);
				}
				ous.flush();
				ous.close();
				file.renameTo(html);
				File htmlFile = DataSynchizeUtil.unZipNoteHtml(html, html.getParent());
				String content = HtmlParser.replaceHtmlImg(htmlFile, "../../notes/"+note.getSubjectId()+"/"+note.getId()+"/");
				note.setContent(content);
				String result = addNote(note, res);
				
				return result;
			} catch (Exception e) {
				ResponseStatus rs = new ResponseStatus(ResponseCode.SERVER_ERROR);
				e.printStackTrace();
				return rs.toString();
			} finally {
				ous.flush();
				ous.close();
				ins.close();
				if(file.exists()){
					file.delete();
				}
				if(html.exists()){
					html.delete();
				}
			}
		}else{
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.NOTETAG.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.NOTE.toString());
			
			DataBean bean = new DataBean("", "");
			return JsonUtil.bean2json(bean);
		}
	}
	
	@Override
	@POST
	@Path("/send/note/u/{timeStamp}")
	public String updateNote(@FormParam("data") String data, @PathParam("timeStamp") long timeStamp, @DefaultValue("true") @QueryParam("updateContent") boolean updateContent, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) throws Exception {
		logger.info("更新条目基本信息 : " + data);
		if(!action.equals(DataSynchAction.FINISH.toString())){
			NoteEntity note = (NoteEntity) JsonUtil.getObject4JsonString(data, NoteEntity.class);
			AccountEntity user = accountService.getUser4Session();
			
			boolean saveLog = false;
			//用户是否在条目目录黑名单中
			if(!saveLog){
				if(!StringUtil.isEmpty(note.getDirId())){
					boolean isBan = directoryService.inDirBlackList(user.getId(), note.getDirId());
					if(isBan){
						logger.warn("用户在目录黑名单中", new InsufficientAuthenticationException("用户在目录黑名单中"));
						saveLog = true;
						
						res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
						res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.NOTE.toString());
						
						res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.BAN.toString());
						res.setHeader(HeaderName.DATATYPE.toString(), DataType.NOTE.toString());
						
						logger.info("用户在目录黑名单中：");
					}
				}
			}
			
			//用户是否在条目黑名单中
			if(!saveLog){
				boolean isBan = noteService.inNoteBlackList(user.getId(), note.getId());
				if(isBan){
					logger.warn("用户在条目黑名单中", new InsufficientAuthenticationException("用户在条目黑名单中"));
					saveLog = true;
					
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.NOTE.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.BAN.toString());
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.NOTE.toString());
					
					logger.info("用户在条目黑名单中：");
				}
			}
			
			if(!saveLog){
				boolean hasPermission = DataSynchizeUtil.hasPermission(user.getId(), note.getSubjectId(), ActionName.UPDATE_NOTE);
				if(!hasPermission && needAuth && !note.getCreateUserId().equals(user.getId())){
					logger.warn("用户操作权限不足", new InsufficientAuthenticationException("用户没有进行此操作的权限： 【修改条目】, 请联系专题管理员。"));
					saveLog = true;
					
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.NOTE.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.BAN.toString());
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.NOTE.toString());
					
					logger.info("权限不足，不能修改条目，恢复条目数据：");
				}
			}
			
			if(saveLog){
				note = noteService.getNote(note.getId());
				DataSynchizeUtil.saveBanLog(DataType.NOTE.toString(), DataSynchAction.UPDATE.toString(), note.getId(), note.getUpdateUser(), user.getId(), note.getUpdateTimeStamp(), System.currentTimeMillis());
				
				DataBean bean = new DataBean();
				String returnVal = JsonUtil.bean2json(bean);
				logger.info("权限不足，不能修改条目，恢复条目数据：" + returnVal);
				return returnVal;
			}
			
			return updateNote(note, timeStamp, updateContent, res);
		}else{
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.NOTETAG.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.NOTE.toString());
		}
		DataBean bean = new DataBean("", "");
		return JsonUtil.bean2json(bean);
	}
	
	private String updateNote(NoteEntity note, long timeStamp, boolean updateContent, HttpServletResponse res) throws Exception {
		NoteEntity n = noteService.getNote(note.getId());
		AccountEntity user = accountService.getUser4Session();
		SynchLogEntity log = synchLogService.findLogByData(user.getClientId(), user.getId(), timeStamp, DataType.NOTE.toString(), note.getId());
		if (n != null && (n.getDeleted() == Constants.DATA_NOT_DELETED || n.getDeleted() == Constants.DATA_NOTSEARCH)){
			//服务器上不存在该数据其它操作日志，或者有修改操作日志并且操作早于此次操作，才更新数据库数据
			if(log == null || (log.getAction().equals(DataSynchAction.UPDATE.toString()) && log.getOperateTime() < note.getUpdateTimeStamp())){
				note.setUpdateUser(user.getId());
				note.setUpdateUserId(user.getId());
				if(note.getDeleted() == null){
					note.setDeleted(Constants.DATA_NOT_DELETED);
				}
				ReflectionUtils.copyBeanProperties(note, n);
				noteService.updateNote(n, updateContent);
				
				//保存note的html文件记录，等下通知客户端上传
				DataSynchizeUtil.updateNoteHtmlFile(note, user);
				
				res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
				res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.NOTE.toString());
				
				res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
				res.setHeader(HeaderName.DATATYPE.toString(), DataType.NOTE.toString());
			}
		}else{
			//header中添加控制位
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.NOTE.toString());
			
			String op = DataSynchAction.DELETE.toString();
			if(n == null){
				op = DataSynchAction.TRUNCATE.toString();
			}
			res.setHeader(HeaderName.ACTION.toString(), op);
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.NOTE.toString());
			
			String timestamp = "";
			String id = "";
			String updateUser = "";
			if(n != null && n.getDeleted().intValue() == Constants.DATA_DELETED){
				timestamp = n.getUpdateTimeStamp() + "";
				id = n.getId();
				updateUser = n.getUpdateUser();
			}else{
				timestamp = log.getOperateTime() + "";
				id = log.getClassPK();
				updateUser = log.getOperateUser();
			}
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("id", id);
			map.put("className", DataType.NOTE.toString());
			map.put("operation", op);
			map.put("updateUserId", updateUser);
			map.put("updateTimeStamp", timestamp);	
			
			String dataStr = JsonUtil.map2json(map);
			DataBean bean = new DataBean("", dataStr);
			return JsonUtil.bean2json(bean);
		}

		if(log != null){
			if(log.getAction().equals(DataSynchAction.TRUNCATE.toString())){
				//header中添加控制位
				res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
				res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.NOTE.toString());
				
				res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.TRUNCATE.toString());
				res.setHeader(HeaderName.DATATYPE.toString(), DataType.NOTE.toString());
				
				Map<String, Object> map = DataSynchizeUtil.parseDeleteLog(log);
				String dataStr = JsonUtil.map2json(map);
				DataBean bean = new DataBean("", dataStr);
				return JsonUtil.bean2json(bean);
			}else if(log.getAction().equals(DataSynchAction.DELETE.toString())){
				//header中添加控制位
				res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
				res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.NOTE.toString());
				
				res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.DELETE.toString());
				res.setHeader(HeaderName.DATATYPE.toString(), DataType.NOTE.toString());
				
				Map<String, Object> map = DataSynchizeUtil.parseDeleteLog(log);
				String dataStr = JsonUtil.map2json(map);
				DataBean bean = new DataBean("", dataStr);
				return JsonUtil.bean2json(bean);
			}else if(log.getAction().equals(DataSynchAction.UPDATE.toString()) && log.getOperateTime() >= note.getUpdateTimeStamp()){
				//header中添加控制位
				res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
				res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.NOTE.toString());
				
				res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.UPDATE.toString());
				res.setHeader(HeaderName.DATATYPE.toString(), DataType.NOTE.toString());
				
				NoteVersionEntity noteHistory = noteService.saveNoteHistory(note, user.getId());   //保存为历史版本
				note.setVersion(noteHistory.getVersion());
				note.setClassName(DataType.NOTE.toString());
				note.setOperation(DataSynchAction.UPDATE.toString());
				String dataStr = JsonUtil.bean2json(note);
				DataBean bean = new DataBean("", dataStr);
				return JsonUtil.bean2json(bean);
			}else {
				res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
				res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.NOTE.toString());
				
				res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
				res.setHeader(HeaderName.DATATYPE.toString(), DataType.NOTE.toString());
			}
		}
		
		DataBean bean = new DataBean("", "");
		return JsonUtil.bean2json(bean);
	}
	
	@Override
	@POST
	@Path("/send/notehtml/u")
	public String updateNoteHtml(@Multipart("data") String data, @Multipart("noteFile") InputStream ins, @PathParam("timeStamp") long timeStamp, @DefaultValue("true") @QueryParam("updateContent") boolean updateContent, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletRequest request, @Context HttpServletResponse res) throws IOException {
		logger.info("上传条目HTML数据 :" + data);
		String result = null;
		if(action == null || !action.equals(DataSynchAction.FINISH.toString())){
			
			OutputStream ous = null;
			AccountEntity user = accountService.getUser4Session();
			NoteEntity note = (NoteEntity) JsonUtil.getObject4JsonString(data, NoteEntity.class);
			
			boolean saveLog = false;
			//用户是否在条目目录黑名单中
			if(!saveLog){
				if(!StringUtil.isEmpty(note.getDirId())){
					boolean isBan = directoryService.inDirBlackList(user.getId(), note.getDirId());
					if(isBan){
						logger.warn("用户在目录黑名单中", new InsufficientAuthenticationException("用户在目录黑名单中"));
						saveLog = true;
						
						res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
						res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.NOTE.toString());
						
						res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.BAN.toString());
						res.setHeader(HeaderName.DATATYPE.toString(), DataType.NOTE.toString());
						
						logger.info("用户在目录黑名单中：");
					}
				}
			}
			
			//用户是否在条目黑名单中
			if(!saveLog){
				boolean isBan = noteService.inNoteBlackList(user.getId(), note.getId());
				if(isBan){
					logger.warn("用户在条目黑名单中", new InsufficientAuthenticationException("用户在条目黑名单中"));
					saveLog = true;
					
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.NOTE.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.BAN.toString());
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.NOTE.toString());
					
					logger.info("用户在条目黑名单中：");
				}
			}
			
			if(!saveLog){
				boolean hasPermission = DataSynchizeUtil.hasPermission(user.getId(), note.getSubjectId(), ActionName.UPDATE_NOTE);
				if(!hasPermission && needAuth && !note.getCreateUserId().equals(user.getId())){
					logger.warn("用户操作权限不足", new InsufficientAuthenticationException("用户没有进行此操作的权限： 【修改条目】, 请联系专题管理员。"));
					
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.NOTE.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.BAN.toString());
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.NOTE.toString());
					
					logger.info("权限不足，不能修改条目，恢复条目数据：");
				}
			}
			
			if(saveLog){
				note = noteService.getNote(note.getId());
				DataSynchizeUtil.saveBanLog(DataType.NOTE.toString(), DataSynchAction.UPDATE.toString(), note.getId(), note.getUpdateUser(), user.getId(), note.getUpdateTimeStamp(), System.currentTimeMillis());
				
				DataBean bean = new DataBean();
				String returnVal = JsonUtil.bean2json(bean);
				logger.info("权限不足，不能修改条目，恢复条目数据：" + returnVal);
				return returnVal;
			}
			
			// 条目HTML存放路径
			String savePath = FilePathUtil.getNoteHtmlPath(note);
			String htmlFileName = savePath + note.getId();
			
			File folder = new File(savePath);
			if (!folder.exists()) {
				folder.mkdirs();
			}
			File file = new File(htmlFileName + ".zip" + ".tmp");
			File html = new File(htmlFileName + ".zip");
			if(html.exists()){
				boolean deleted = html.delete();
				if(!deleted){
					htmlFileName = htmlFileName + "副本";
					html = new File(htmlFileName + ".zip");
				}
			}
			try {
				//ins = request.getInputStream();
				ous = new FileOutputStream(file);
				int n = 0;
				int buffer = 1024;
				byte[] bytes = new byte[buffer];
				while ((n = ins.read(bytes)) != -1) {
					ous.write(bytes, 0, n);
				}
				if(ous != null){
					ous.flush();
					ous.close();
				}
				NoteEntity oldNote = noteService.getNote(note.getId());
				if(oldNote != null){
					if(oldNote.getUpdateTimeStamp() == null || oldNote.getUpdateTimeStamp() < note.getUpdateTimeStamp()){
						file.renameTo(html);
						File htmlFile = DataSynchizeUtil.unZipNoteHtml(html, html.getParent());
						String content = HtmlParser.replaceHtmlImg(htmlFile, "../../notes/"+note.getSubjectId()+"/"+note.getId()+"/");
						note.setContent(content);
						result = updateNote(note, timeStamp, true, res);
					}else{
						result = updateNote(note, timeStamp, true, res);
						// 服务器上的条目较新, 此次上传条目内容存为历史版本
						html = new File(FilePathUtil.getNoteHtmlPath(note) + FilePathUtil.getNoteZipFileName(note.getId(), note.getVersion()));
						file.renameTo(html);
					}
				}
			} catch (Exception e) {
				ResponseStatus rs = new ResponseStatus(ResponseCode.SERVER_ERROR);
				e.printStackTrace();
				return rs.toString();
			} finally {
				ins.close();
				if(file.exists()){
					file.delete();
				}
				if(html.exists()){
					html.delete();
				}
			}
		}else{
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.NOTETAG.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.NOTE.toString());
		}
		if(result != null){
			return result;
		}else{
			DataBean bean = new DataBean("", "");
			return JsonUtil.bean2json(bean);
		}
	}
	
	@Deprecated
	@Override
	@POST
	@Path("/send/note_/u/{id}")
	public String updateNoteContent(@PathParam("id") String id, @FormParam("content") String content, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) {
		logger.info("更新条目内容 ！！！");
		NoteEntity oldNote = noteService.getNote(id);

		String md5 = MD5FileUtil.getMD5String(content);
		if (!oldNote.getMd5().equals(md5)) {
			oldNote.setMd5(md5);
			oldNote.setContent(content);
		}
		noteService.updateNote(oldNote, true);
		return oldNote.getId();
	}

	@Deprecated
	@Override
	@POST
	@Path("/send/note/d/{id}")
	public String deleteNote(@PathParam("id") String id, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) {
		if(!action.equals(DataSynchAction.FINISH.toString())){
			NoteEntity oldNote = noteService.getNote(id);
			if(oldNote != null && oldNote.getDeleted() == Constants.DATA_NOT_DELETED){
				AccountEntity user = accountService.getUser4Session();
				oldNote.setUpdateUser(user.getId());
				noteService.markDelNote(oldNote);
			}
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.NOTE.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.NOTE.toString());
		}else{
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.NOTE.toString());
		}
		res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
		return new ResponseStatus().toString();
	}

	@Deprecated
	@Override
	@POST
	@Path("/send/noteblack/a/{noteId}/{userId}")
	public String addNoteBlack(@PathParam("noteId") String noteId, @PathParam("userId") String userId, @Context HttpServletResponse res) {
		noteService.blacklistedUser(userId, noteId, System.currentTimeMillis());
		return new ResponseStatus().toString();
	}

	@Deprecated
	@Override
	@DELETE
	@Path("/send/noteblack/d/{noteId}/{userId}")
	public String deleteNoteBlack(@PathParam("noteId") String noteId, @PathParam("userId") String userId, @Context HttpServletResponse res) {
		noteService.removeUser4blacklist(userId, noteId, System.currentTimeMillis());
		return new ResponseStatus().toString();
	}
	
	@Override
	@POST
	@Path("/send/notetag/a")
	public String addNoteTag(@FormParam("data") String data, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) {
		AccountEntity user = accountService.getUser4Session();
		if(!action.equals(DataSynchAction.FINISH.toString())){
			NoteTag noteTag = (NoteTag) JsonUtil.getObject4JsonString(data, NoteTag.class);
			
			NoteEntity note = noteService.getNote(noteTag.getNoteId());
			if(note != null && note.getDeleted() == Constants.DATA_NOT_DELETED){
				String subjectId = note.getSubjectId();
				
				boolean saveLog = false;
				//用户是否在条目目录黑名单中
				if(!saveLog){
					if(!StringUtil.isEmpty(note.getDirId())){
						boolean isBan = directoryService.inDirBlackList(user.getId(), note.getDirId());
						if(isBan){
							logger.warn("用户在目录黑名单中", new InsufficientAuthenticationException("用户在目录黑名单中"));
							saveLog = true;
							
							res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
							res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.NOTETAG.toString());
							
							res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.BAN.toString());
							res.setHeader(HeaderName.DATATYPE.toString(), DataType.NOTETAG.toString());
							
							logger.info("用户在目录黑名单中：");
						}
					}
				}
				
				//用户是否在条目黑名单中
				if(!saveLog){
					boolean isBan = noteService.inNoteBlackList(user.getId(), note.getId());
					if(isBan){
						logger.warn("用户在条目黑名单中", new InsufficientAuthenticationException("用户在条目黑名单中"));
						saveLog = true;
						
						res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
						res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.NOTETAG.toString());
						
						res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.BAN.toString());
						res.setHeader(HeaderName.DATATYPE.toString(), DataType.NOTETAG.toString());
						
						logger.info("用户在条目黑名单中：");
					}
				}
				
				if(!saveLog){
					//作者
					boolean isOwner = note != null && note.getCreateUserId().equals(user.getId());
					//编辑
					boolean hasPermission = DataSynchizeUtil.hasPermission(user.getId(), subjectId, ActionName.ADD_DIRECTORY);
					if(!hasPermission && needAuth && !isOwner){
						logger.warn("用户操作权限不足", new InsufficientAuthenticationException("用户没有进行此操作的权限： 【添加条目标签】, 请联系专题管理员。"));
						
						res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
						res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.NOTETAG.toString());
						
						res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.BAN.toString());
						res.setHeader(HeaderName.DATATYPE.toString(), DataType.NOTETAG.toString());
						
						logger.info("权限不足，不能添加条目标签，删除新增条目标签：");
					}	
				}
				
				if(saveLog){
					DataSynchizeUtil.saveBanLog(DataType.NOTETAG.toString(), DataSynchAction.TRUNCATE.toString(), noteTag.getId(), user.getId(), user.getId(), System.currentTimeMillis(), System.currentTimeMillis());
					
					DataBean bean = new DataBean();
					String returnVal = JsonUtil.bean2json(bean);
					logger.info("权限不足，不能添加条目标签，删除新增条目标签：" + returnVal);
					return returnVal;
				}
				
				NoteTag nt = tagService.findNoteTag(noteTag.getNoteId(), noteTag.getTagId());
				if(nt == null){
					tagService.saveNoteTag(noteTag);
				}
			}
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.NOTETAG.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.NOTETAG.toString());
			
		}else{
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.ATTACHMENT.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.NOTETAG.toString());
		}
		DataBean bean = new DataBean("", "");
		return JsonUtil.bean2json(bean);
	}

	@Override
	@Path("/send/notetag/t/{id}")
	public String deleteNoteTag(@PathParam("id") String id, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) {
		logger.info("删除条目标签关系信息 ！！！");
		
		if(!action.equals(DataSynchAction.FINISH.toString())){
			NoteTag noteTag = tagService.get(NoteTag.class, id);
			if(noteTag != null){
				tagService.deleteNoteTag(noteTag);
			}
			
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.NOTETAG.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.NOTETAG.toString());
		} else{
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
			
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.NEXT.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.TAG.toString());
		}
		return new ResponseStatus().toString();
	}
	
	@Override
	@POST
	@Path("/send/tag/a")
	public String addTag(@FormParam("data") String data, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) {
		logger.info("添加标签信息 : " + data);
		ResponseStatus rs = new ResponseStatus();
		if(!action.equals(DataSynchAction.FINISH.toString())){
			TagEntity tag = (TagEntity) JsonUtil.getObject4JsonString(data, TagEntity.class);
			AccountEntity user = accountService.getUser4Session();
			tag.setCreateUser(user.getId());
			tag.setDeleted(Constants.DATA_NOT_DELETED);
			tagService.addTag(tag);
			
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.TAG.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.TAG.toString());
		}else{
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.NOTE.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.TAG.toString());
		}
		return rs.toString();
	}

	@Override
	@POST
	@Path("/send/tag/u/{timeStamp}")
	public String updateTag(@FormParam("data") String data, @PathParam("timeStamp") long timeStamp, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) throws Exception {
		logger.info("更新标签信息 ！！！");
		ResponseStatus rs = new ResponseStatus();
		if(!action.equals(DataSynchAction.FINISH.toString())){
			TagEntity tag = (TagEntity) JsonUtil.getObject4JsonString(data, TagEntity.class);
			TagEntity t = tagService.getTag(tag.getId());
			AccountEntity user = accountService.getUser4Session();
			
			SynchLogEntity log = synchLogService.findLogByData(user.getClientId(), user.getId(), timeStamp, DataType.TAG.toString(), tag.getId());
			if (t != null) {
				//服务器上不存在该数据其它操作日志，或者有修改操作日志并且操作早于此次操作，才更新数据库数据
				if(log == null || (log.getAction().equals(DataSynchAction.UPDATE.toString()) && log.getOperateTime() < tag.getUpdateTimeStamp())){
					tag.setUpdateUser(user.getId());
					tag.setUpdateUserId(user.getId());
					ReflectionUtils.copyBeanProperties(tag, t);
					tagService.updateTag(t);
					
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.TAG.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.TAG.toString());
				}
			}else{
				//header中添加控制位
				res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
				res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.TAG.toString());
				
				res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.TRUNCATE.toString());
				res.setHeader(HeaderName.DATATYPE.toString(), DataType.TAG.toString());
				
				String timestamp = log.getOperateTime() + "";
				String id = log.getClassPK();
				String updateUser = log.getOperateUser();
				
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("id", id);
				map.put("className", DataType.TAG.toString());
				map.put("operation", DataSynchAction.TRUNCATE.toString());
				map.put("updateUserId", updateUser);
				map.put("updateTimeStamp", timestamp);
				
				String dataStr = JsonUtil.map2json(map);
				DataBean bean = new DataBean("", dataStr);
				return JsonUtil.bean2json(bean);
			}
			
			if(log != null){
				if(log.getAction().equals(DataSynchAction.TRUNCATE.toString())){
					//header中添加控制位
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.TAG.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.TRUNCATE.toString());
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.TAG.toString());
					
					Map<String, Object> map = DataSynchizeUtil.parseDeleteLog(log);
					DataBean bean = new DataBean("", JsonUtil.map2json(map));
					return JsonUtil.bean2json(bean);
				}else if(log.getAction().equals(DataSynchAction.UPDATE.toString()) && log.getOperateTime() >= tag.getUpdateTimeStamp()){
					rs.setResponse(ResponseCode.UPDATE);
					rs.setData(log.getId());
					
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.TAG.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.UPDATE.toString());
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.TAG.toString());
				}else{
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.TAG.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.TAG.toString());
				}
			}
		}else{
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.NOTE.toString());
			
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.NEXT.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.TAG.toString());
		}
		DataBean bean = new DataBean("", "");
		return JsonUtil.bean2json(bean);
	}

	@Deprecated
	@Override
	@DELETE
	@Path("/send/tag/t/{id}")
	public String deleteTag(@PathParam("id") String id, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) {
		logger.info("删除标签信息 ！！！");
		
		if(!action.equals(DataSynchAction.FINISH.toString())){
			TagEntity tag = tagService.getTag(id);
			if(tag != null){
				tagService.deleteTag(tag);
			}
			
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.TAG.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.TAG.toString());
		} else{
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
			
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.NEXT.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.TAG.toString());
		}
		return new ResponseStatus().toString();
	}

	@Override
	@POST
	@Path("/send/template/a")
	public String addTemplate(@FormParam("data") String data, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) {
		logger.info("添加模板信息 ！！！");
		TemplateEntity template = (TemplateEntity) JsonUtil.getObject4JsonString(data, TemplateEntity.class);
		String templateId = templateService.addTemplate(template);
		return templateId;
	}

	@Override
	@POST
	@Path("/send/comment/a")
	public String addComment(@FormParam("data") String data, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) {
		logger.info("添加评论信息 : " + data);
		AccountEntity user = accountService.getUser4Session();
		if(!action.equals(DataSynchAction.FINISH.toString())){
			CommentEntity comment = (CommentEntity) JsonUtil.getObject4JsonString(data, CommentEntity.class);
			comment.setCreateUser(user.getId());
			
			NoteEntity note = null;
			if(!StringUtil.isEmpty(comment.getNoteId())){
				note = noteService.getNote(comment.getNoteId());
				//条目不存在了
				if(note == null || note.getDeleted().intValue() == Constants.DATA_DELETED){
					logger.info("附件所属条目已无效 : " + comment.getNoteId());
					
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.COMMENT.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.BAN.toString());
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.COMMENT.toString());
					
					DataSynchizeUtil.saveBanLog(DataType.COMMENT.toString(), DataSynchAction.TRUNCATE.toString(), comment.getId(), user.getId(), user.getId(), System.currentTimeMillis(), System.currentTimeMillis());
					
				}else{
					boolean isBan = noteService.inNoteBlackList(user.getId(), comment.getNoteId());
					if(isBan){
						res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
						res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.COMMENT.toString());
						
						res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.BAN.toString());
						res.setHeader(HeaderName.DATATYPE.toString(), DataType.COMMENT.toString());
						
						DataSynchizeUtil.saveBanLog(DataType.COMMENT.toString(), DataSynchAction.TRUNCATE.toString(), comment.getId(), user.getId(), user.getId(), System.currentTimeMillis(), System.currentTimeMillis());
						
					}else{
						commentService.addComment(comment);
						
						res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
						res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.COMMENT.toString());
						
						res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
						res.setHeader(HeaderName.DATATYPE.toString(), DataType.COMMENT.toString());
					}
				}
				
			}
			
		}else{
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.FINISH.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.FILE.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.COMMENT.toString());
			
			//准备上传文件
			String uploadData = DataSynchizeUtil.queryUploadFile(user, res);
			DataBean bean = new DataBean(uploadData, "");
			return JsonUtil.bean2json(bean);
		}
		DataBean bean = new DataBean("", "");
		return JsonUtil.bean2json(bean);
	}

	@Deprecated
	@Override
	@DELETE
	@Path("/send/comment/d/{id}")
	public String deleteComment(@PathParam("id") String id, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) {
		
		if(!action.equals(DataSynchAction.FINISH.toString())){
			CommentEntity comment = commentService.getComment(id);
			if(comment != null){
				commentService.deleteComment(comment);
			}
			
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.COMMENT.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.COMMENT.toString());
		}else{
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.COMMENT.toString());
		}
		
		res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
		return new ResponseStatus().toString();
	}

	@Override
	@POST
	@Path("/send/user/u")
	public String updateUser(@FormParam("data") String data) {
		AccountEntity user = (AccountEntity) JsonUtil.getObject4JsonString(data, AccountEntity.class);
		accountService.updateEntitie(user);
		return new ResponseStatus().toString();
	}

	@Override
	@GET
	@Path("/get_user/{id}")
	public String getUserInfo(@PathParam("id") String id) {
		AccountEntity user = accountService.getUser(id);
		return JsonUtil.bean2json(user);
	}
	
	@Override
	@GET
	@Path("/get/subjectuser/{subjectId}/{clientId}/{userId}/{timeStamp}")
	public String getSubjectUser(@PathParam("subjectId") String subjectId, @PathParam("clientId") String clientId, @PathParam("userId") String userId, @PathParam("timeStamp") long timeStamp, @Context HttpServletResponse res) {
		SynchLogEntity log = synchLogService.findSubjectUserLogs(clientId, userId, timeStamp, subjectId);
		Map<String, Object> map = new HashMap<String, Object>();
		if(log != null){
			String memberId = log.getTargetUser();
			RoleUser ru = roleService.findUserRole(memberId, subjectId);
			map = DataSynchizeUtil.parseLog(log);
			map.put("subjectId", subjectId);
			map.put("userId", memberId);
			map.put("roleId", ru.getRoleId());
		}else{
			map.put("response", ResponseCode.PART_SYNCH_FINISHED.toString());
		}
		/*int count = synchLogService.countSubjectUserLogs(clientId, userId, timeStamp, subjectId);
		if(count > 0){
			map.put("response", ResponseCode.NEED_SYNCH_SUBJECTUSER.toString());
		}else{
			map.put("response", ResponseCode.PART_SYNCH_FINISHED.toString());
		}*/
		return JsonUtil.map2json(map);
	}
	
	@Override
	@GET
	@Path("/get/subjectdata/{subjectId}/{clientId}/{userId}/{timeStamp}")
	public String getSubjectRelatedLogs(@PathParam("subjectId") String subjectId, @PathParam("clientId") String clientId, @PathParam("userId") String userId, @PathParam("timeStamp") long timeStamp, @Context HttpServletResponse res) {
		/*int count = synchLogService.countSubjectRelatedLogs(clientId, userId, timeStamp, subjectId);
		if(count > 0){
			SynchLogEntity log = synchLogService.findSubjectRelatedLogs(clientId, userId, timeStamp, subjectId);
			Map<String, Object> map = DataSynchizeUtil.parseLog(log);
			if(count > 1){
				map.put("response", ResponseCode.NEED_SUBJECT_DATA.toString());
			}else{
				map.put("response", ResponseCode.PART_SYNCH_FINISHED.toString());
			}
			return JsonUtil.map2json(map);
		}*/
		return new ResponseStatus().toString();
	}
	
	@Override
	@GET
	@Path("/get/attachment/{noteId}/{clientId}/{userId}/{timeStamp}")
	public String getNoteAttachment(@PathParam("noteId") String noteId, @PathParam("clientId") String clientId, @PathParam("userId") String userId, @PathParam("timeStamp") long timeStamp, @Context HttpServletResponse res) {
		SynchLogEntity log = synchLogService.findAttachmentLogByNote(clientId, userId, timeStamp, noteId);
		Map<String, Object> map = new HashMap<String, Object>();
		/*if(log != null){
			map = DataSynchizeUtil.parseLog(log);
		}else{
			map.put("response", ResponseCode.PART_SYNCH_FINISHED.toString());
		}
		int count = synchLogService.countAttachmentLogByNote(clientId, userId, timeStamp, noteId);
		if(count > 0){
			map.put("response", ResponseCode.NEED_SYNCH_ATTACHMENT.toString());
		}else{
			map.put("response", ResponseCode.PART_SYNCH_FINISHED.toString());
		}*/
		return JsonUtil.map2json(map);
	}
	
	@Override
	@POST
	@Path("/send/subjectuser/a")
	public String addSubjectMember(@FormParam("data") String data, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) {
		logger.info("添加专题成员信息: 专题ID--" + data);
		AccountEntity user = accountService.getUser4Session();
		
		if(!action.equals(DataSynchAction.FINISH.toString())){
			RoleUser ru = (RoleUser) JsonUtil.getObject4JsonString(data, RoleUser.class);
			boolean hasPermission = DataSynchizeUtil.hasPermission(user.getId(), ru.getSubjectId(), ActionName.ASSIGN_MEMBER);
			if(!hasPermission && needAuth){
				logger.warn("用户操作权限不足", new InsufficientAuthenticationException("用户没有进行此操作的权限： 【添加专题成员】, 请联系专题管理员。"));
				
				res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
				res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.SUBJECTUSER.toString());
				
				res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.BAN.toString());
				res.setHeader(HeaderName.DATATYPE.toString(), DataType.SUBJECTUSER.toString());
				
				/*RoleUser ru = new RoleUser();
				ru.setGroupId(subjectId);
				ru.setUserId(userId);
				ru.setClassName(DataType.SUBJECTUSER.toString());
				ru.setOperation(DataSynchAction.DELETE.toString());*/
				DataBean bean = new DataBean();
				String returnVal = JsonUtil.bean2json(bean);
				logger.info("权限不足，不能添加专题成员，删除新增成员：" + returnVal);
				return returnVal;
			}
			
			String rId = null;
			if (StringUtil.isEmpty(ru.getRoleId())) {
				Role role = roleService.findRoleByName(RoleName.READER);
				rId = role.getId();
			}else{
				Role role = roleService.findRoleByName(ru.getRoleId());
				rId = role.getId();
			}
			
			roleService.addRoleUser(ru.getSubjectId(), ru.getUserId(), rId, user.getId(), System.currentTimeMillis());
			
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.SUBJECTUSER.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.SUBJECTUSER.toString());
		}else{
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.DIRECTORY.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.SUBJECTUSER.toString());
		}
		DataBean bean = new DataBean("", "");
		return JsonUtil.bean2json(bean);
	}
	
	@Override
	@POST
	@Path("/send/subjectuser/u/{timeStamp}")
	public String updateSubjectMember(@FormParam("data") String data, @PathParam("timeStamp") long timeStamp, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) {
		logger.info("修改专题成员角色: 专题ID--" + data);
		AccountEntity user = accountService.getUser4Session();
		
		if(!action.equals(DataSynchAction.FINISH.toString())){
			RoleUser ru = (RoleUser) JsonUtil.getObject4JsonString(data, RoleUser.class);
			boolean hasPermission = DataSynchizeUtil.hasPermission(user.getId(), ru.getSubjectId(), ActionName.ASSIGN_MEMBER);
			if(!hasPermission && needAuth){
				logger.warn("用户操作权限不足", new InsufficientAuthenticationException("用户没有进行此操作的权限： 【修改专题成员角色】, 请联系专题管理员。"));
				
				res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
				res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.SUBJECTUSER.toString());
				
				res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.BAN.toString());
				res.setHeader(HeaderName.DATATYPE.toString(), DataType.SUBJECTUSER.toString());
				
				SynchLogEntity log = new SynchLogEntity();
				log.setId(UUIDGenerator.uuid());
				log.setClassName(DataType.SUBJECTUSER.toString());
				log.setAction(DataSynchAction.UPDATE.toString());
				log.setClassPK(ru.getId());
				log.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
				log.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
				log.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
				log.setOperateUser(user.getId());
				log.setOperateTime(System.currentTimeMillis());
				log.setTargetUser(user.getId());
				log.setSynchTime(System.currentTimeMillis());
				synchLogService.saveSynchLog(log);
				
				/*RoleUser ru = new RoleUser();
				ru.setGroupId(subjectId);
				ru.setUserId(userId);
				ru.setClassName(DataType.SUBJECTUSER.toString());
				ru.setOperation(DataSynchAction.RESTORE.toString());*/
				DataBean bean = new DataBean();
					
				String returnVal = JsonUtil.bean2json(bean);
				logger.info("权限不足，不能修改专题成员角色，恢复成员角色：" + returnVal);
				return returnVal;
			}
			
			String rId = null;
			if (StringUtil.isEmpty(ru.getRoleId())) {
				Role role = roleService.findRoleByName(RoleName.READER);
				rId = role.getId();
			}else{
				Role role = roleService.findRoleByName(ru.getRoleId());
				rId = role.getId();
			}
			
			roleService.updateRoleUser(ru.getSubjectId(), ru.getUserId(), rId);
			
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.SUBJECTUSER.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.SUBJECTUSER.toString());
		}else{
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.DIRECTORY.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.SUBJECTUSER.toString());
		}
		DataBean bean = new DataBean("", "");
		return JsonUtil.bean2json(bean);
	}
	
	@Override
	@DELETE
	@Path("/send/subjectuser/t/{id}")
	public String deleteSubjectMember(@PathParam("id") String id, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) {
		logger.info("添加专题成员信息: 专题ID--" +id);
		
		AccountEntity user = accountService.getUser4Session();
		RoleUser roleUser = roleService.getRoleUser(id);
		if(!action.equals(DataSynchAction.FINISH.toString())){
			boolean hasPermission = DataSynchizeUtil.hasPermission(user.getId(), roleUser.getSubjectId(), ActionName.ASSIGN_MEMBER);
			if(!hasPermission && needAuth){
				logger.warn("用户操作权限不足", new InsufficientAuthenticationException("用户没有进行此操作的权限： 【删除专题成员】, 请联系专题管理员。"));
				
				res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
				res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.SUBJECTUSER.toString());
				
				res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
				res.setHeader(HeaderName.DATATYPE.toString(), DataType.SUBJECTUSER.toString());
				
				RoleUser ru = new RoleUser();
				roleUser.setClassName(DataType.SUBJECTUSER.toString());
				roleUser.setOperation(DataSynchAction.RESTORE.toString());
				DataBean bean = new DataBean("", JsonUtil.bean2json(ru));
				
				String returnVal = JsonUtil.bean2json(bean);
				logger.info("权限不足，不能删除专题成员，恢复成员：" + returnVal);
				return returnVal;
			}
			
			roleService.removeRoleUser(roleUser.getSubjectId(), roleUser.getUserId());
			
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.SUBJECTUSER.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.SUBJECTUSER.toString());
		}else{
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.DIRECTORY.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.SUBJECTUSER.toString());
		}
		DataBean bean = new DataBean("", "");
		return JsonUtil.bean2json(bean);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	@POST
	@Path("/send/batchdata/t")
	public String truncateBatchData(@FormParam("data") String data, @DefaultValue(SynchConstants.CLIENT_SYNCH_SEND) @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res){
		BatchDataBean bean = new BatchDataBean();
		List result = new ArrayList();
		String[] dataTypes = SynchDataCache.getReverseDatasSort();
		if(!StringUtil.isEmpty(data)){
			logger.info("批量truncate数据：" + data);
			Map map = JsonUtil.getMap4Json(data);
			String dataType = map.get("dataType").toString();
			
			if(!action.equals(DataSynchAction.FINISH.toString())){
				AccountEntity user = accountService.getUser4Session();
				String datas = map.get("datas").toString();
				if(datas.charAt(0) == '"'){
					datas = datas.substring(1, datas.length() - 1);
				}
				List list = JsonUtil.getList4Json(datas, HashMap.class);
				
				if(dataType.equals(DataType.SUBJECT.toString())){
					for(Object obj : list){
						Map<String, String> objMap = (Map<String, String>) obj;
						String id = objMap.get("id");
						SubjectEntity subject = subjectService.getSubject(id);

						boolean hasPermission = DataSynchizeUtil.hasPermission(user.getId(), subject.getId(), ActionName.DELETE_SUBJECT);
						if(!hasPermission && needAuth){
							logger.warn("用户操作权限不足", new InsufficientAuthenticationException("用户没有进行此操作的权限： 【删除专题】, 请联系专题管理员。"));
							DataSynchizeUtil.saveBanLog(dataType, DataSynchAction.RESTORE.toString(), subject.getId(), subject.getCreateUser(), user.getId(), subject.getUpdateTimeStamp(), System.currentTimeMillis());
							subject.setOperation(DataSynchAction.BAN.toString());
							
							/*res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
							res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
							
							res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.RESTORE.toString());
							res.setHeader(HeaderName.DATATYPE.toString(), DataType.BATCHDATA.toString());
							
							DataBean bean = new DataBean();
							String returnVal = JsonUtil.bean2json(bean);
							logger.info("权限不足，不能删除专题信息，恢复专题数据：" + returnVal);
							return returnVal;*/
						}else{
							try {
								subjectService.deleteSubject(subject);
							} catch (Exception e) {
								e.printStackTrace();
							}
							subject.setOperation(DataSynchAction.SUCCESS.toString());
						}
						result.add(subject);
					}
				}else if(dataType.equals(DataType.DIRECTORY.toString())){
					for(Object obj : list){
						Map<String, String> objMap = (Map<String, String>) obj;
						String id = objMap.get("id");
						DirectoryEntity dir = directoryService.getDirectory(id);
						
						boolean isBan = directoryService.inDirBlackList(user.getId(), dir.getId());
						boolean hasPermission = DataSynchizeUtil.hasPermission(user.getId(), dir.getSubjectId(), ActionName.DELETE_DIRECTORY);
						if((!hasPermission || isBan) && needAuth){
							logger.warn("用户操作权限不足", new InsufficientAuthenticationException("用户没有进行此操作的权限： 【删除目录】, 请联系专题管理员。"));
							
							String act = DataSynchAction.RESTORE.toString();
							if(!isBan){
								DataSynchizeUtil.saveBanLog(dataType, act, dir.getId(), dir.getCreateUser(), user.getId(), dir.getUpdateTimeStamp(), System.currentTimeMillis());
							}
							
							dir.setOperation(DataSynchAction.BAN.toString());
							/*res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
							res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
							
							res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.RESTORE.toString());
							res.setHeader(HeaderName.DATATYPE.toString(), DataType.BATCHDATA.toString());
								
							DataBean bean = new DataBean();
							String returnVal = JsonUtil.bean2json(bean);
							logger.info("权限不足，不能删除，恢复目录数据：" + returnVal);
							return returnVal;*/
						}else{
							try {
								directoryService.deleteOnlyDirectory(dir);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							dir.setOperation(DataSynchAction.SUCCESS.toString());
						}
						result.add(dir);
					}
				}else if(dataType.equals(DataType.TAG.toString())){
					for(Object obj : list){
						Map<String, String> objMap = (Map<String, String>) obj;
						String id = objMap.get("id");
						TagEntity tag = tagService.getTag(id);
						tagService.deleteTagAll(tag);
						tag.setOperation(DataSynchAction.SUCCESS.toString());
						
						result.add(tag);
					}
					
				}else if(dataType.equals(DataType.NOTE.toString())){
					for(Object obj : list){
						Map<String, String> objMap = (Map<String, String>) obj;
						String id = objMap.get("id");
						NoteEntity note = noteService.getNote(id.toString());
						Map<String, String> beanMap = DataSynchizeUtil.toBeanMap(note);
						
						boolean isBan = noteService.inNoteBlackList(user.getId(), note.getId());
						boolean hasPermission = DataSynchizeUtil.hasPermission(user.getId(), note.getSubjectId(), ActionName.DELETE_NOTE);
						if((!hasPermission || isBan) && needAuth){
							logger.warn("用户操作权限不足", new InsufficientAuthenticationException("用户没有进行此操作的权限： 【删除条目】, 请联系专题管理员。"));
							String act = DataSynchAction.RESTORE.toString();
							if(!isBan){
								DataSynchizeUtil.saveBanLog(dataType, act, note.getId(), note.getCreateUser(), user.getId(), note.getUpdateTimeStamp(), System.currentTimeMillis());
							}
							beanMap.put("operation", DataSynchAction.BAN.toString());
							/*res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
							res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
							
							res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
							res.setHeader(HeaderName.DATATYPE.toString(), DataType.BATCHDATA.toString());
							
							note.setClassName(DataType.NOTE.toString());
							note.setOperation(DataSynchAction.RESTORE.toString());
							DataBean bean = new DataBean("", JsonUtil.bean2json(note));
							
							String returnVal = JsonUtil.bean2json(bean);
							logger.info("权限不足，不能删除条目，恢复条目数据：" + returnVal);
							return returnVal;*/
						}else{
							try {
								noteService.deleteNote(note);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							beanMap.put("operation", DataSynchAction.SUCCESS.toString());
						}
						result.add(beanMap);
					}
				}else if(dataType.equals(DataType.COMMENT.toString())){
					for(Object obj : list){
						Map<String, String> objMap = (Map<String, String>) obj;
						String id = objMap.get("id");
						CommentEntity comment = commentService.getComment(id);
						
						CommentEntity newComment = new CommentEntity();
						try {
							BeanUtils.copyProperties(newComment, comment);
						} catch (Exception e) {
							e.printStackTrace();
						}
						
						try {
							commentService.deleteComment(comment);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						newComment.setOperation(DataSynchAction.SUCCESS.toString());
						result.add(newComment);
					}
				}else if(dataType.equals(DataType.ATTACHMENT.toString())){
					for(Object obj : list){
						Map<String, String> objMap = (Map<String, String>) obj;
						String id = objMap.get("id");
						AttachmentEntity attachment = attachmentService.getAttachment(id);
						
						String subjectId = null;
						boolean isBan = false;
						if(!StringUtil.isEmpty(attachment.getNoteId())){
							subjectId = noteService.getNote(attachment.getNoteId()).getSubjectId();
							isBan = noteService.inNoteBlackList(user.getId(), attachment.getNoteId());
						}else{
							subjectId = directoryService.getDirectory(attachment.getDirectoryId()).getSubjectId();
						}
						
						boolean hasPermission = DataSynchizeUtil.hasPermission(user.getId(), subjectId, ActionName.ADD_NOTE);
						if((!hasPermission || isBan) && needAuth){
							logger.warn("用户操作权限不足", new InsufficientAuthenticationException("用户没有进行此操作的权限： 【删除附件】, 请联系专题管理员。"));
							
							String act = DataSynchAction.RESTORE.toString();
							if(!isBan){
								DataSynchizeUtil.saveBanLog(dataType, act, attachment.getId(), attachment.getCreateUser(), user.getId(), attachment.getCreateTimeStamp(), System.currentTimeMillis());
							}
							attachment.setOperation(DataSynchAction.BAN.toString());
							/*res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
							res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
							
							res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
							res.setHeader(HeaderName.DATATYPE.toString(), DataType.BATCHDATA.toString());
							
							attachment.setClassName(DataType.NOTE.toString());
							attachment.setOperation(DataSynchAction.RESTORE.toString());
							DataBean bean = new DataBean("", JsonUtil.bean2json(attachment));
							
							String returnVal = JsonUtil.bean2json(bean);
							logger.info("权限不足，不能删除附件，恢复附件数据：" + returnVal);
							return returnVal;*/
						}else{
							try {
								attachmentService.deleteAttachment(attachment);
							} catch (Exception e) {
								e.printStackTrace();
							}
							attachment.setOperation(DataSynchAction.SUCCESS.toString());
						}
						result.add(attachment);
					}
				}else if(dataType.equals(DataType.SUBJECTUSER.toString())){
					for(Object obj : list){
						Map<String, String> objMap = (Map<String, String>) obj;
						String id = objMap.get("id");
						RoleUser ru = roleService.getRoleUser(id);
						
						String subjectId = ru.getSubjectId();
						
						boolean hasPermission = DataSynchizeUtil.hasPermission(user.getId(), subjectId, ActionName.ASSIGN_MEMBER);
						if(!hasPermission && needAuth){
							logger.warn("用户操作权限不足", new InsufficientAuthenticationException("用户没有进行此操作的权限： 【删除专题成员】, 请联系专题管理员。"));
							DataSynchizeUtil.saveBanLog(dataType, DataSynchAction.RESTORE.toString(), ru.getId(), ru.getCreateUserId(), user.getId(), ru.getCreateTimeStamp(), System.currentTimeMillis());
							ru.setOperation(DataSynchAction.BAN.toString());
							/*res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
							res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
							
							res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
							res.setHeader(HeaderName.DATATYPE.toString(), DataType.BATCHDATA.toString());
							
							attachment.setClassName(DataType.NOTE.toString());
							attachment.setOperation(DataSynchAction.RESTORE.toString());
							DataBean bean = new DataBean("", JsonUtil.bean2json(attachment));
							
							String returnVal = JsonUtil.bean2json(bean);
							logger.info("权限不足，不能删除附件，恢复附件数据：" + returnVal);
							return returnVal;*/
						}else{
							try {
								roleService.removeRoleUser(ru);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							ru.setOperation(DataSynchAction.SUCCESS.toString());
						}
						result.add(ru);
					}
				}else if(dataType.equals(DataType.NOTETAG.toString())){
					for(Object obj : list){
						Map<String, String> objMap = (Map<String, String>) obj;
						String id = objMap.get("id");
						NoteTag noteTag = tagService.getNoteTag(id);
						NoteEntity note = noteService.getNote(noteTag.getNoteId());
						String subjectId = note.getSubjectId();
						
						boolean isBan = noteService.inNoteBlackList(user.getId(), noteTag.getNoteId());
						boolean hasPermission = DataSynchizeUtil.hasPermission(user.getId(), subjectId, ActionName.UPDATE_NOTE);
						if((!hasPermission || isBan) && needAuth){
							logger.warn("用户操作权限不足", new InsufficientAuthenticationException("用户没有进行此操作的权限： 【删除条目标签关系】, 请联系专题管理员。"));
							
							String act = DataSynchAction.RESTORE.toString();
							if(!isBan){
								DataSynchizeUtil.saveBanLog(dataType, act, noteTag.getId(), noteTag.getCreateUserId(), user.getId(), noteTag.getCreateTimeStamp(), System.currentTimeMillis());
							}
							noteTag.setOperation(DataSynchAction.BAN.toString());
							/*res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
							res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
							
							res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
							res.setHeader(HeaderName.DATATYPE.toString(), DataType.BATCHDATA.toString());
							
							attachment.setClassName(DataType.NOTE.toString());
							attachment.setOperation(DataSynchAction.RESTORE.toString());
							DataBean bean = new DataBean("", JsonUtil.bean2json(attachment));
							
							String returnVal = JsonUtil.bean2json(bean);
							logger.info("权限不足，不能删除附件，恢复附件数据：" + returnVal);
							return returnVal;*/
						}else{
							try {
								tagService.deleteNoteTag(noteTag);
							} catch (Exception e) {
								e.printStackTrace();
							}
							noteTag.setOperation(DataSynchAction.SUCCESS.toString());
						}
						result.add(noteTag);
					}
				}
				
				res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
				res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
				
				res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.TRUNCATE.toString());
				res.setHeader(HeaderName.DATATYPE.toString(), DataType.BATCHDATA.toString());
				
			}else{
				res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
				res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
				
				res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
				res.setHeader(HeaderName.DATATYPE.toString(), DataType.BATCHDATA.toString());
			}
			
			String nextDataType = DataSynchizeUtil.getNextDataType(dataType, dataTypes);
			if(nextDataType != null){
				bean.setDataType(dataType);
				bean.setDatas(JsonUtil.list2json(result));
				
				String str = JsonUtil.bean2json(bean);
				Map<String, String> delMap = new HashMap<String, String>();
				delMap.put("dataType", nextDataType);
				delMap.put("action", DataSynchAction.TRUNCATE.toString());
				String returnData = JsonUtil.map2json(delMap);
				
				DataBean db = new DataBean(returnData, str);
				
				String returnVal = JsonUtil.bean2json(db);
				logger.info("上传truncate数据日志返回：" + returnVal);
				return returnVal;
			}else{  //nextDataType == null 批量truncate操作完成了
				res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
				res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
				
				res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
				res.setHeader(HeaderName.DATATYPE.toString(), DataType.BATCHDATA.toString());
				
				bean.setDatas(JsonUtil.list2json(result));
				String str = JsonUtil.bean2json(bean);
				Map<String, String> delMap = new HashMap<String, String>();
				delMap.put("dataType", dataTypes[0]);
				delMap.put("action", DataSynchAction.DELETE.toString());
				String returnData = JsonUtil.map2json(delMap);
				
				DataBean db = new DataBean(returnData, str);
				String returnVal = JsonUtil.bean2json(db);
				logger.info("上传truncate数据日志返回0：" + returnVal);
				return returnVal;
			}
		}else{
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.BATCHDATA.toString());
		}
		String str = JsonUtil.bean2json(bean);
		Map<String, String> delMap = new HashMap<String, String>();
		delMap.put("dataType", dataTypes[0]);
		delMap.put("action", DataSynchAction.DELETE.toString());
		String returnData = JsonUtil.map2json(delMap);
		
		DataBean db = new DataBean(returnData, str);
		String returnVal = JsonUtil.bean2json(db);
		logger.info("上传truncate数据日志返回：" + returnVal);
		return returnVal;
	}
	
	/**
	 * 上传删除数据
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@POST
	@Path("/send/batchdata")
	public String deleteBatchData(@FormParam("data") String data, @DefaultValue(SynchConstants.CLIENT_SYNCH_SEND) @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res){
		BatchDataBean bean = new BatchDataBean();
		List result = new ArrayList();
		String[] dataTypes = SynchDataCache.getReverseDatasSort();
		if(!StringUtil.isEmpty(data)){
			Map map = JsonUtil.getMap4Json(data);
			String dataType = map.get("dataType").toString();
			String operation = map.get("defaultOperation") == null ? DataSynchAction.DELETE.toString() : map.get("defaultOperation").toString();
			if(operation.equals(DataSynchAction.TRUNCATE.toString())){
				return truncateBatchData(data, action, res);
			}
			
			logger.info("批量删除数据：" + data);
			
			if(!action.equals(DataSynchAction.FINISH.toString())){
				AccountEntity user = accountService.getUser4Session();
				
				String datas = map.get("datas").toString();
				if(datas.charAt(0) == '"'){
					datas = datas.substring(1, datas.length() - 1);
				}
				List list = JsonUtil.getList4Json(datas, HashMap.class);
				
				if(dataType.equals(DataType.SUBJECT.toString())){
					for(Object obj : list){
						Map<String, String> objMap = (Map<String, String>) obj;
						String id = objMap.get("id");
						SubjectEntity subject = subjectService.getSubject(id);

						boolean hasPermission = DataSynchizeUtil.hasPermission(user.getId(), subject.getId(), ActionName.DELETE_SUBJECT);
						if(!hasPermission && needAuth){
							logger.warn("用户操作权限不足", new InsufficientAuthenticationException("用户没有进行此操作的权限： 【删除专题】, 请联系专题管理员。"));
							SubjectEntity sub = subjectService.getSubject(subject.getId());
							DataSynchizeUtil.saveBanLog(dataType, DataSynchAction.RESTORE.toString(), sub.getId(), sub.getCreateUser(), user.getId(), sub.getUpdateTimeStamp(), System.currentTimeMillis());
							subject.setOperation(DataSynchAction.BAN.toString());
							
							/*res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
							res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
							
							res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.RESTORE.toString());
							res.setHeader(HeaderName.DATATYPE.toString(), DataType.BATCHDATA.toString());
							
							DataBean bean = new DataBean();
							String returnVal = JsonUtil.bean2json(bean);
							logger.info("权限不足，不能删除专题信息，恢复专题数据：" + returnVal);
							return returnVal;*/
						}else{
							subjectService.deleteSubject(subject);
							subject.setOperation(DataSynchAction.SUCCESS.toString());
						}
						result.add(subject);
					}
				}else if(dataType.equals(DataType.DIRECTORY.toString())){
					for(Object obj : list){
						Map<String, String> objMap = (Map<String, String>) obj;
						String id = objMap.get("id");
						DirectoryEntity dir = directoryService.getDirectory(id);
						
						boolean isBan = directoryService.inDirBlackList(user.getId(), dir.getId());
						boolean hasPermission = DataSynchizeUtil.hasPermission(user.getId(), dir.getSubjectId(), ActionName.DELETE_DIRECTORY);
						if((!hasPermission || isBan) && needAuth){
							logger.warn("用户操作权限不足", new InsufficientAuthenticationException("用户没有进行此操作的权限： 【删除目录】, 请联系专题管理员。"));
							
							String act = DataSynchAction.RESTORE.toString();
							if(isBan){
								act = DataSynchAction.TRUNCATE.toString();
							}
							DataSynchizeUtil.saveBanLog(dataType, act, dir.getId(), dir.getCreateUser(), user.getId(), dir.getUpdateTimeStamp(), System.currentTimeMillis());
							dir.setOperation(DataSynchAction.BAN.toString());
							/*res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
							res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
							
							res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.RESTORE.toString());
							res.setHeader(HeaderName.DATATYPE.toString(), DataType.BATCHDATA.toString());
								
							DataBean bean = new DataBean();
							String returnVal = JsonUtil.bean2json(bean);
							logger.info("权限不足，不能删除，恢复目录数据：" + returnVal);
							return returnVal;*/
						}else{
							long timestamp = StringUtil.isEmpty(String.valueOf(objMap.get("updateTimeStamp"))) ? 0 : Long.parseLong(String.valueOf(objMap.get("updateTimeStamp")));
							if(timestamp > 0){
								dir.setUpdateTimeStamp(timestamp);
							}
							String updateUserId = objMap.get("updateUserId");
							if(!StringUtil.isEmpty(updateUserId)){
								dir.setUpdateUserId(updateUserId);
							}
							directoryService.markDelDirectory(dir);
							dir.setOperation(DataSynchAction.SUCCESS.toString());
						}
						result.add(dir);
					}
				}else if(dataType.equals(DataType.TAG.toString())){
					for(Object obj : list){
						Map<String, String> objMap = (Map<String, String>) obj;
						String id = objMap.get("id");
						TagEntity tag = tagService.getTag(id);
						tagService.deleteTagAll(tag);
						tag.setOperation(DataSynchAction.SUCCESS.toString());
						
						result.add(tag);
					}
					
				}else if(dataType.equals(DataType.NOTE.toString())){
					for(Object obj : list){
						Map<String, String> objMap = (Map<String, String>) obj;
						String id = objMap.get("id");
						NoteEntity note = noteService.getNote(id.toString());
						
						Map<String, String> beanMap = null;
						boolean isBan = noteService.inNoteBlackList(user.getId(), note.getId());
						boolean hasPermission = DataSynchizeUtil.hasPermission(user.getId(), note.getSubjectId(), ActionName.DELETE_NOTE);
						if((!hasPermission || isBan) && needAuth){
							logger.warn("用户操作权限不足", new InsufficientAuthenticationException("用户没有进行此操作的权限： 【删除条目】, 请联系专题管理员。"));
							String act = DataSynchAction.RESTORE.toString();
							if(isBan){
								act = DataSynchAction.TRUNCATE.toString();
							}
							DataSynchizeUtil.saveBanLog(dataType, act, note.getId(), note.getCreateUser(), user.getId(), note.getUpdateTimeStamp(), System.currentTimeMillis());
							beanMap = DataSynchizeUtil.toBeanMap(note);
							beanMap.put("operation", DataSynchAction.BAN.toString());
							/*res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
							res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
							
							res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
							res.setHeader(HeaderName.DATATYPE.toString(), DataType.BATCHDATA.toString());
							
							note.setClassName(DataType.NOTE.toString());
							note.setOperation(DataSynchAction.RESTORE.toString());
							DataBean bean = new DataBean("", JsonUtil.bean2json(note));
							
							String returnVal = JsonUtil.bean2json(bean);
							logger.info("权限不足，不能删除条目，恢复条目数据：" + returnVal);
							return returnVal;*/
						}else{
							long timestamp = StringUtil.isEmpty(String.valueOf(objMap.get("updateTimeStamp"))) ? 0 : Long.parseLong(String.valueOf(objMap.get("updateTimeStamp")));
							if(timestamp > 0){
								note.setUpdateTimeStamp(timestamp);
							}
							String updateUserId = objMap.get("updateUserId");
							if(!StringUtil.isEmpty(updateUserId)){
								note.setUpdateUserId(updateUserId);
							}
							noteService.markDelNote(note);
							beanMap = DataSynchizeUtil.toBeanMap(note);
							beanMap.put("operation", DataSynchAction.SUCCESS.toString());
						}
						result.add(beanMap);
					}
				}else if(dataType.equals(DataType.COMMENT.toString())){
					for(Object obj : list){
						Map<String, String> objMap = (Map<String, String>) obj;
						String id = objMap.get("id");
						CommentEntity comment = commentService.getComment(id);
						commentService.deleteComment(comment);
						comment.setOperation(DataSynchAction.SUCCESS.toString());
						result.add(comment);
					}
				}else if(dataType.equals(DataType.ATTACHMENT.toString())){
					for(Object obj : list){
						Map<String, String> objMap = (Map<String, String>) obj;
						String id = objMap.get("id");
						AttachmentEntity attachment = attachmentService.getAttachment(id);
						String subjectId = null;
						boolean isBan = false;
						if(!StringUtil.isEmpty(attachment.getNoteId())){
							subjectId = noteService.getNote(attachment.getNoteId()).getSubjectId();
							isBan = noteService.inNoteBlackList(user.getId(), attachment.getNoteId());
						}else{
							subjectId = directoryService.getDirectory(attachment.getDirectoryId()).getSubjectId();
						}
						
						boolean hasPermission = DataSynchizeUtil.hasPermission(user.getId(), subjectId, ActionName.ADD_NOTE);
						if((!hasPermission || isBan) && needAuth){
							logger.warn("用户操作权限不足", new InsufficientAuthenticationException("用户没有进行此操作的权限： 【删除附件】, 请联系专题管理员。"));
							
							String act = DataSynchAction.RESTORE.toString();
							if(isBan){
								act = DataSynchAction.TRUNCATE.toString();
							}
							
							DataSynchizeUtil.saveBanLog(dataType, act, attachment.getId(), attachment.getCreateUser(), user.getId(), attachment.getCreateTimeStamp(), System.currentTimeMillis());
							attachment.setOperation(DataSynchAction.BAN.toString());
							/*res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
							res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
							
							res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
							res.setHeader(HeaderName.DATATYPE.toString(), DataType.BATCHDATA.toString());
							
							attachment.setClassName(DataType.NOTE.toString());
							attachment.setOperation(DataSynchAction.RESTORE.toString());
							DataBean bean = new DataBean("", JsonUtil.bean2json(attachment));
							
							String returnVal = JsonUtil.bean2json(bean);
							logger.info("权限不足，不能删除附件，恢复附件数据：" + returnVal);
							return returnVal;*/
						}else{
						
							long timestamp = StringUtil.isEmpty(String.valueOf(objMap.get("updateTimeStamp"))) ? 0 : Long.parseLong(String.valueOf(objMap.get("updateTimeStamp")));
							if(timestamp > 0){
								attachment.setUpdateTimeStamp(timestamp);
							}
							String updateUserId = objMap.get("updateUserId");
							if(!StringUtil.isEmpty(updateUserId)){
								attachment.setUpdateUserId(updateUserId);
							}
							attachmentService.markDelAttachment(attachment);
							attachment.setOperation(DataSynchAction.SUCCESS.toString());
						}
						result.add(attachment);
					}
				}else if(dataType.equals(DataType.SUBJECTUSER.toString())){
					for(Object obj : list){
						Map<String, String> objMap = (Map<String, String>) obj;
						String id = objMap.get("id");
						RoleUser ru = roleService.getRoleUser(id);
						String subjectId = ru.getSubjectId();
						
						boolean hasPermission = DataSynchizeUtil.hasPermission(user.getId(), subjectId, ActionName.ASSIGN_MEMBER);
						if(!hasPermission && needAuth){
							logger.warn("用户操作权限不足", new InsufficientAuthenticationException("用户没有进行此操作的权限： 【删除专题成员】, 请联系专题管理员。"));
							DataSynchizeUtil.saveBanLog(dataType, DataSynchAction.RESTORE.toString(), ru.getId(), ru.getCreateUserId(), user.getId(), ru.getCreateTimeStamp(), System.currentTimeMillis());
							ru.setOperation(DataSynchAction.BAN.toString());
							/*res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
							res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
							
							res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
							res.setHeader(HeaderName.DATATYPE.toString(), DataType.BATCHDATA.toString());
							
							attachment.setClassName(DataType.NOTE.toString());
							attachment.setOperation(DataSynchAction.RESTORE.toString());
							DataBean bean = new DataBean("", JsonUtil.bean2json(attachment));
							
							String returnVal = JsonUtil.bean2json(bean);
							logger.info("权限不足，不能删除附件，恢复附件数据：" + returnVal);
							return returnVal;*/
						}else{
							roleService.removeRoleUser(ru);
							ru.setOperation(DataSynchAction.SUCCESS.toString());
						}
						result.add(ru);
					}
				}else if(dataType.equals(DataType.NOTETAG.toString())){
					for(Object obj : list){
						Map<String, String> objMap = (Map<String, String>) obj;
						String id = objMap.get("id");
						NoteTag noteTag = tagService.getNoteTag(id);
						NoteEntity note = noteService.getNote(noteTag.getNoteId());
						String subjectId = note.getSubjectId();
						
						boolean isBan = noteService.inNoteBlackList(user.getId(), noteTag.getNoteId());
						boolean hasPermission = DataSynchizeUtil.hasPermission(user.getId(), subjectId, ActionName.UPDATE_NOTE);
						if((!hasPermission || isBan) && needAuth){
							logger.warn("用户操作权限不足", new InsufficientAuthenticationException("用户没有进行此操作的权限： 【删除条目标签关系】, 请联系专题管理员。"));
							
							String act = DataSynchAction.RESTORE.toString();
							if(isBan){
								act = DataSynchAction.TRUNCATE.toString();
							}
							DataSynchizeUtil.saveBanLog(dataType, act, noteTag.getId(), noteTag.getCreateUserId(), user.getId(), noteTag.getCreateTimeStamp(), System.currentTimeMillis());
							noteTag.setOperation(DataSynchAction.BAN.toString());
							/*res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
							res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
							
							res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
							res.setHeader(HeaderName.DATATYPE.toString(), DataType.BATCHDATA.toString());
							
							attachment.setClassName(DataType.NOTE.toString());
							attachment.setOperation(DataSynchAction.RESTORE.toString());
							DataBean bean = new DataBean("", JsonUtil.bean2json(attachment));
							
							String returnVal = JsonUtil.bean2json(bean);
							logger.info("权限不足，不能删除附件，恢复附件数据：" + returnVal);
							return returnVal;*/
						}else{
							tagService.deleteNoteTag(noteTag);
							noteTag.setOperation(DataSynchAction.SUCCESS.toString());
						}
						result.add(noteTag);
					}
				}
				
				res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
				res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
				
				res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.DELETE.toString());
				res.setHeader(HeaderName.DATATYPE.toString(), DataType.BATCHDATA.toString());
				
				logger.info("action:" + DataSynchAction.DELETE.toString());
				logger.info("datatype:" + DataType.BATCHDATA.toString());
				
				logger.info("nextaction:" + DataSynchAction.SEND.toString());
				logger.info("nextdatatype:" + DataType.BATCHDATA.toString());
			}else{ //finish
				bean.setDataType(dataType);
				bean.setDatas(JsonUtil.list2json(result));
				res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
				res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
				
				res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
				res.setHeader(HeaderName.DATATYPE.toString(), DataType.BATCHDATA.toString());
				
				logger.info("action:" + DataSynchAction.NEXT.toString());
				logger.info("datatype:" + DataType.BATCHDATA.toString());
				
				logger.info("nextaction:" + DataSynchAction.SEND.toString());
				logger.info("nextdatatype:" + DataType.BATCHDATA.toString());
			}
			
			String nextDataType = DataSynchizeUtil.getNextDataType(dataType, dataTypes);
			bean.setDataType(dataType);
			bean.setDatas(JsonUtil.list2json(result));
			if(nextDataType != null){
				
				String str = JsonUtil.bean2json(bean);
				Map<String, String> delMap = new HashMap<String, String>();
				delMap.put("dataType", nextDataType);
				delMap.put("action", DataSynchAction.DELETE.toString());
				String returnData = JsonUtil.map2json(delMap);
				
				DataBean db = new DataBean(returnData, str);
				
				String returnVal = JsonUtil.bean2json(db);
				
				
				logger.info("批量删除数据返回：" + returnVal);
				return returnVal;
			}else{  //nextDataType == null 批量删除操作完成了
				res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
				res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.SUBJECT.toString());
				
				res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
				res.setHeader(HeaderName.DATATYPE.toString(), DataType.BATCHDATA.toString());
				
				logger.info("action:" + DataSynchAction.NEXT.toString());
				logger.info("datatype:" + DataType.BATCHDATA.toString());
				
				logger.info("nextaction:" + DataSynchAction.SEND.toString());
				logger.info("nextdatatype:" + DataType.SUBJECT.toString());
			}
			
		}else{
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.SUBJECT.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.BATCHDATA.toString());
			
			logger.info("action:" + DataSynchAction.NEXT.toString());
			logger.info("datatype:" + DataType.BATCHDATA.toString());
			
			logger.info("nextaction:" + DataSynchAction.SEND.toString());
			logger.info("nextdatatype:" + DataType.SUBJECT.toString());
		}
		
		String str = JsonUtil.bean2json(bean);
		Map<String, String> delMap = new HashMap<String, String>();
		delMap.put("dataType", "");
		delMap.put("action", DataSynchAction.DELETE.toString());
		String returnData = JsonUtil.map2json(delMap);
		
		DataBean db = new DataBean(returnData, str);
		String returnVal = JsonUtil.bean2json(db);
		logger.info("批量删除数据返回1：" + returnVal);
		return returnVal;
	}
	
	@Override
	@POST
	@Path("/send/user/u/{userId}")
	public String updatePassword(@PathParam("userId") String userId, @FormParam("oldPassword") String oldPassword, @FormParam("password") String password) {
		ResponseStatus res = null;
		AccountEntity user = accountService.getUser(userId);
		if (user.getPassword().equals(oldPassword)) {
			user.setPassword(password);
			accountService.updateAccount(user);
			res = new ResponseStatus();
			return res.toString();
		} else {
			res = new ResponseStatus(ResponseCode.AUTHORIZE_FAILED);
			return res.toString();
		}

	}

	@Override
	@GET
	@Path("/datetime")
	public long getServerTime() {
		return System.currentTimeMillis();
	}

	@Override
	@POST
	@Path("/send/subject")
	public String addOrUpdateSubject(@FormParam("data") String data, @Context HttpServletResponse res) throws IllegalAccessException, InvocationTargetException {
		SubjectEntity subject = (SubjectEntity) JsonUtil.getObject4JsonString(data, SubjectEntity.class);
		SubjectEntity sub = subjectService.getSubject(subject.getId());
		AccountEntity user = accountService.getUser4Session();
		ResponseStatus rs = new ResponseStatus();
		if (sub == null) {
			subject.setCreateUser(user.getId());
			subjectService.addSubject(subject, user.getId());
		} else {
			sub.setUpdateUser(user.getId());
			sub.setDeleted(Constants.DATA_NOT_DELETED);
			ReflectionUtils.copyBeanProperties(subject, sub);
			subjectService.updateSubject(sub);
		}
		return rs.toString();
	}

	@Override
	@POST
	@Path("/send/directory")
	public String addOrUpdateDirectory(@FormParam("data") String data, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) throws IllegalAccessException, InvocationTargetException {
		DirectoryEntity directory = (DirectoryEntity) JsonUtil.getObject4JsonString(data, DirectoryEntity.class);
		DirectoryEntity dir = directoryService.getDirectory(directory.getId());
		AccountEntity user = accountService.getUser4Session();
		ResponseStatus rs = new ResponseStatus();
		if (dir == null) {
			directory.setCreateUser(user.getId());
			directory.setDeleted(Constants.DATA_NOT_DELETED);
			directoryService.addDirectory(directory);
		} else {
			directory.setUpdateUser(user.getId());
			directory.setDeleted(Constants.DATA_NOT_DELETED);
			ReflectionUtils.copyBeanProperties(directory, dir);
			directoryService.updateDirectory(dir);
		}
		return rs.toString();
	}

	@Override
	@POST
	@Path("/send/note")
	public String addOrUpdateNote(@FormParam("data") String data, @DefaultValue("true") @FormParam("updateContent") boolean updateContent, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) throws IllegalAccessException, InvocationTargetException {
		NoteEntity note = (NoteEntity) JsonUtil.getObject4JsonString(data, NoteEntity.class);
		NoteEntity n = noteService.getNote(note.getId());
		AccountEntity user = accountService.getUser4Session();
		ResponseStatus rs = new ResponseStatus();
		if (n == null) {
			note.setCreateUser(user.getId());
			note.setDeleted(Constants.DATA_NOT_DELETED);
			noteService.addNote(note);
		} else {
			note.setUpdateUser(user.getId());
			note.setDeleted(Constants.DATA_NOT_DELETED);
			ReflectionUtils.copyBeanProperties(note, n);
			noteService.updateNote(n, updateContent);
		}
		return rs.toString();
	}

	@Override
	@POST
	@Path("/send/tag")
	public String addOrUpdateTag(@FormParam("data") String data, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) throws IllegalAccessException, InvocationTargetException {
		TagEntity tag = (TagEntity) JsonUtil.getObject4JsonString(data, TagEntity.class);
		TagEntity t = tagService.getTag(tag.getId());
		AccountEntity user = accountService.getUser4Session();
		ResponseStatus rs = new ResponseStatus();
		if (t == null) {
			tag.setCreateUser(user.getId());
			tag.setDeleted(Constants.DATA_NOT_DELETED);
			tagService.addTag(tag);
		} else {
			tag.setUpdateUser(user.getId());
			ReflectionUtils.copyBeanProperties(tag, t);
			tagService.updateTag(t);
		}
		return rs.toString();
	}
	
	/*public String dataBan(@FormParam("subjectId") String subjectId, @FormParam("noteId") String noteId, @FormParam("pageNo")int pageNo, @FormParam("pageSize")int pageSize, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res){
		ModelMap mmap = new ModelMap();
		ModelAndView mv = new ModelAndView("front/note/noteban");
		SubjectEntity subjectEntity = subjectService.getSubject(subjectId);
		List<RoleUser> list = roleService.findSubjectUsers(subjectEntity.getId(), pageResult);
		for (RoleUser roleUser : list) {
			roleUser.setBlackList(groupService.checkNoteUser(roleUser.getUserId(), nodeId));
		}
		mmap.put("subjectid", subjectid);
		mmap.put("nodeId", nodeId);
		mmap.put("pageResult", pageResult);
		mv.addAllObjects(mmap);
	}*/
}