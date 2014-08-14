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

import org.apache.log4j.Logger;
import org.htmlparser.util.ParserException;
import org.jeecgframework.core.util.JSONHelper;
import org.jeecgframework.core.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;

import com.eht.comment.entity.CommentEntity;
import com.eht.comment.service.CommentServiceI;
import com.eht.common.bean.ResponseStatus;
import com.eht.common.cache.DataCacheTool;
import com.eht.common.constant.Constants;
import com.eht.common.constant.RoleName;
import com.eht.common.constant.SynchConstants;
import com.eht.common.enumeration.DataSynchAction;
import com.eht.common.enumeration.DataType;
import com.eht.common.enumeration.HeaderName;
import com.eht.common.enumeration.ResponseCode;
import com.eht.common.util.FilePathUtil;
import com.eht.common.util.HtmlParser;
import com.eht.common.util.JsonUtil;
import com.eht.common.util.MD5FileUtil;
import com.eht.common.util.ReflectionUtils;
import com.eht.common.util.UUIDGenerator;
import com.eht.group.service.GroupService;
import com.eht.log.entity.SynchLogEntity;
import com.eht.log.service.SynchLogServiceI;
import com.eht.note.entity.AttachmentEntity;
import com.eht.note.entity.NoteEntity;
import com.eht.note.service.AttachmentServiceI;
import com.eht.note.service.NoteServiceI;
import com.eht.role.entity.Role;
import com.eht.role.entity.RoleUser;
import com.eht.role.service.RoleService;
import com.eht.subject.entity.DirectoryEntity;
import com.eht.subject.entity.SubjectEntity;
import com.eht.subject.service.DirectoryServiceI;
import com.eht.subject.service.SubjectServiceI;
import com.eht.system.service.DataInitService;
import com.eht.tag.entity.TagEntity;
import com.eht.tag.service.TagServiceI;
import com.eht.template.entity.TemplateEntity;
import com.eht.template.service.TemplateServiceI;
import com.eht.user.entity.AccountEntity;
import com.eht.user.service.AccountServiceI;
import com.eht.webservice.bean.BatchDataBean;
import com.eht.webservice.bean.SynchResult;
import com.eht.webservice.service.DataSynchizeService;
import com.eht.webservice.service.SynchResultService;
import com.eht.webservice.service.util.DataSynchizeUtil;

public class DataSynchizeServiceImpl implements DataSynchizeService {

	private Logger logger = Logger.getLogger(DataSynchizeServiceImpl.class);

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
	private SynchLogServiceI synchLogService;
	
	@Autowired
	private SynchResultService synchResultService;

	@Override
	@POST
	@Path("/uploadNoteFile/{noteId}")
	public String uploadNoteFile(@PathParam("noteId") String noteId, @Context HttpServletRequest request, @Context HttpServletResponse res) throws IOException, ParserException {
		AccountEntity user = accountService.getUser4Session();
		/*InputStream ins = null;
		OutputStream ous = null;
		NoteEntity note = noteService.getNote(noteId);
		AttachmentEntity attachment = attachmentService.findNeedUploadAttachmentByNote(noteId, fileName);
		String imgPath = attachment.getFilePath();
		File folder = new File(imgPath);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		File file = new File(imgPath + File.separator + fileName + ".tmp");
		try {
			ins = request.getInputStream();
			ous = new FileOutputStream(file);
			int n = 0;
			int buffer = 1024;
			byte[] bytes = new byte[buffer];
			while ((n = ins.read(bytes)) != -1) {
				ous.write(bytes, 0, n);
			}

		} catch (Exception e) {
			ResponseStatus rs = new ResponseStatus(ResponseCode.SERVER_ERROR);
			e.printStackTrace();
			return rs.toString();
		} finally {
			ous.flush();
			ous.close();
			ins.close();
			File image = new File(imgPath + File.separator + fileName);
			file.renameTo(image);
			
			attachment.setStatus(Constants.FILE_TRANS_COMPLETED);
			attachment.setMd5(MD5FileUtil.getFileMD5String(image));
			attachmentService.updateEntitie(attachment);
		}*/
		return DataSynchizeUtil.queryUploadFile(user.getId(), attachmentService, res);
	}

	@Override
	@POST
	@Path("/upload/{attachmentId}")
	public String uploadAttachment(@PathParam("attachmentId") String attachmentId, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletRequest request, @Context HttpServletResponse res) {
		AccountEntity user = accountService.getUser4Session();
		
		if(!action.equals(DataSynchAction.FINISH.toString())){
			AttachmentEntity attachment = attachmentService.getAttachment(attachmentId);
			if(attachment.getMd5() != null){
				AttachmentEntity attaServer = attachmentService.findAttachmentByMd5(attachment.getMd5());
				// 服务器存在相同文件
				if (attaServer != null && attaServer.getStatus() == 1) {
					logger.info("服务器存在该文件，直接复制服务器文件！！！");
					DataSynchizeUtil.copyServerFile(attaServer, attachment);
					attachmentService.updateAttachment(attachment);
					return DataSynchizeUtil.queryUploadFile(user.getId(), attachmentService, res);
				}
			}
	
			// 上次已经上传过该文件或分块传输
			if ((attachment.getTranSfer() != null && attachment.getTranSfer() > 0 && attachment.getStatus() == 0)) {
				logger.info("上次传输文件中断，共传输了" + attachment.getTranSfer() + "字节，准备继续传输！！！");
				return resumeUploadAttachment(attachmentId, 1, request, res); // 1默认文件数据都提交过来
			}
	
			InputStream ins = null;
			OutputStream ous = null;
			long transfered = 0; // 已传输字节
			attachment.setStatus(0); // 文件上传未完成
	
			File folder = new File(attachment.getFilePath());
			if (!folder.exists()) {
				folder.mkdirs();
			}
			File file = new File(attachment.getFilePath() + File.separator + attachment.getFileName() + ".tmp");
			try {
				ins = request.getInputStream();
				ous = new FileOutputStream(file);
				int n = 0;
				int buffer = 1024;
				byte[] bytes = new byte[buffer];
				while ((n = ins.read(bytes)) != -1) {
					ous.write(bytes, 0, n);
					transfered += n;
				}
	
				logger.info("文件传输完成，本次共传输：" + transfered + "字节。");
				attachment.setStatus(Constants.FILE_TRANS_COMPLETED);
			} catch (Exception e) {
				ResponseStatus rs = new ResponseStatus(ResponseCode.SERVER_ERROR);
				return rs.toString();
			} finally {
				try {
					ous.flush();
					ous.close();
					ins.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			attachment.setTranSfer(transfered);
			attachmentService.updateAttachment(attachment);
			logger.info("文件传输完成，更新数据库文件状态！");
			if (attachment.getStatus() != null && attachment.getStatus() == 1) {
				// 上传文件完成后，重命名文件为正式文件
				file.renameTo(new File(attachment.getFilePath() + File.separator + attachment.getFileName()));
			}
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.FILE.toString());
			
			res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
			return DataSynchizeUtil.queryUploadFile(user.getId(), attachmentService, res);
		}else{
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.REQUEST.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.ALL.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.FILE.toString());
			
			res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
			return new ResponseStatus().toString();
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
	@Path("/download/{attachmentId}")
	public String downloadAttachment(@HeaderParam(SynchConstants.HEADER_CLIENT_ID) String clientId, @PathParam("attachmentId") String attachmentId, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse response) throws Exception {
		//AccountEntity user = accountService.getUser4Session();
		
		AttachmentEntity attachment = attachmentService.getAttachment(attachmentId);
		File file = new File(attachment.getFilePath() + File.separator + attachment.getFileName());
		FileInputStream fis = new FileInputStream(file);
		int length = fis.available();
		
		//String nextRes = DataSynchizeUtil.queryDownloadFile(user.getId(), clientId, DataAction.REQUEST.toString(), synchLogService, response);
		//System.out.println("下载文件返回字符串：" + nextRes);
		byte[] b = new byte[length];
		fis.read(b);
		//byte[] bs = ArrayUtils.addAll(b, nextRes.getBytes());
		response.setHeader("Content-Disposition", "attachment; filename=\"" + attachment.getFileName() + "\"");
		//response.addHeader("File-Length", "" + length);
		response.addHeader("Content-Length", "" + length);
		response.setContentType("application/octet-stream;charset=UTF-8");
		
		response.setHeader(HeaderName.ACTION.toString(), DataSynchAction.DOWNLOAD.toString());
		response.setHeader(HeaderName.DATATYPE.toString(), DataType.FILE.toString());
		
		response.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.REQUEST.toString());
		response.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.FILE.toString());
		
		response.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
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
		return "";
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
	public String addAttachment(@FormParam("data") String data, @PathParam("timeStamp") long timeStamp, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) throws ParserException {
		logger.info("添加附件信息 ！！！");
		AccountEntity user = accountService.getUser4Session();
		ResponseStatus rs = new ResponseStatus();
		if(!action.equals(DataSynchAction.FINISH.toString())){
			AttachmentEntity attachment = (AttachmentEntity) JsonUtil.getObject4JsonString(data, AttachmentEntity.class);
			NoteEntity note = noteService.getNote(attachment.getNoteId());
			
			//普通附件
			if (attachment.getFileType().intValue() == Constants.FILE_TYPE_NORMAL) {
				String filePath = FilePathUtil.getFileUploadPath(note, attachment.getDirectoryId());
				attachment.setFilePath(filePath);
			} else { // 目前设计应该没有下面的附件类型
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
			attachment.setDeleted(Constants.DATA_NOT_DELETED);
			attachment.setStatus(Constants.FILE_TRANS_NOT_COMPLETED);
			attachment.setTranSfer(0L);
			attachment.setCreateUser(user.getId());
			if(StringUtil.isEmpty(attachment.getId())){
				attachment.setId(UUIDGenerator.uuid());
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
			
			res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
			return DataSynchizeUtil.queryUploadFile(user.getId(), attachmentService, res);
		}
		return rs.toString();
	}

	@Override
	@DELETE
	@Path("/send/attachment/d/{id}/{timeStamp}")
	public String deleteAttachment(@PathParam("id") String id, @PathParam("timeStamp") long timeStamp, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) {
		logger.info("删除附件信息 ！！！");
		ResponseStatus rs = new ResponseStatus();
		if(!action.equals(DataSynchAction.FINISH.toString())){
			AttachmentEntity attachment = attachmentService.getAttachment(id);
			AccountEntity user = accountService.getUser4Session();
			if(attachment != null && attachment.getDeleted() == Constants.DATA_NOT_DELETED){
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
		res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
		return rs.toString();
	}

	@Override
	@GET
	@Path("/get_count/{clientId}/{userId}/{timeStamp}")
	public int countSynchData(@PathParam("clientId") String clientId, @PathParam("userId") String userId, @PathParam("timeStamp") String timeStamp, @Context HttpServletResponse res) {
		long time = Long.parseLong(timeStamp);
		int count = synchLogService.countSynchLogsByTarget(clientId, userId, time);
		return count;
	}

	@Override
	@GET
	@Path("/getdellogs/{timeStamp}")
	public String getDeleteLogs(@HeaderParam(SynchConstants.HEADER_CLIENT_ID) String clientId, @PathParam("timeStamp") long timeStamp, @DefaultValue(SynchConstants.DATA_CLASS_ALL) @HeaderParam(SynchConstants.HEADER_DATATYPE) String dataClass, @DefaultValue(SynchConstants.CLIENT_SYNCH_REQUEST) @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) throws Exception {
		AccountEntity user = accountService.getUser4Session();
		String[] dataTypes = DataCacheTool.getReverseDatasSort();
		boolean filterDelete = false;  //留下删除操作日志,过滤掉其它
		List<SynchLogEntity> result = synchLogService.findSynchLogsByTarget(clientId, user.getId(), timeStamp, dataClass, filterDelete);
		// 如果存在需要同步的日志
		if(result != null && !result.isEmpty()){
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
			
			// 查询下一有同步日志的数据类型
			String nextDataType = DataSynchizeUtil.queryNextDataType(clientId, user.getId(), timeStamp, dataClass, dataTypes, synchLogService);
			// 设置response头
			if(nextDataType != null){
				res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.REQUEST.toString());
				res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
			}else{
				// count = 0, 剩下的数据类型中未找到需同步的日志
				res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
				res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
				
				bean.setDataType(DataType.COMMENT.toString());
				String data = JsonUtil.bean2json(bean);
				Map<String, String> delMap = new HashMap<String, String>();
				delMap.put("dataType", DataType.COMMENT.toString());
				delMap.put("action", DataSynchAction.DELETE.toString());
				String returnData = JsonUtil.map2json(delMap);
				
				Map<String, String> map = new HashMap<String, String>();
				map.put("data", data);
				map.put("nextData", returnData);
				
				String returnVal = JsonUtil.map2json(map);
				return returnVal;
			}
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.DELETE.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.BATCHDATA.toString());
			
			bean.setDatas(JsonUtil.list2json(list));
			bean.setDataType(dataClass);
			String data = JsonUtil.bean2json(bean);
			
			Map<String, String> delMap = new HashMap<String, String>();
			delMap.put("dataType", nextDataType);
			delMap.put("action", DataSynchAction.DELETE.toString());
			String returnData = JsonUtil.map2json(delMap);  // 客户端再次请求需提交的数据类型
			
			
			Map<String, String> map = new HashMap<String, String>();
			map.put("data", data);
			map.put("nextData", returnData);
			
			String returnVal = JsonUtil.map2json(map);
			logger.info("删除日志返回：" + returnVal);
			return returnVal;
		}else{
			//已经到了最后一个数据类型
			if(dataClass.equals(dataTypes[dataTypes.length - 1]) || dataClass.equals(DataType.ALL.toString())){
				res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
				res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
				
				res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
				res.setHeader(HeaderName.DATATYPE.toString(), DataType.BATCHDATA.toString());
				
				BatchDataBean bean = new BatchDataBean();
				bean.setDataType(DataType.COMMENT.toString());
				String data = JsonUtil.bean2json(bean);
				Map<String, String> delMap = new HashMap<String, String>();
				delMap.put("dataType", DataType.COMMENT.toString());
				delMap.put("action", DataSynchAction.DELETE.toString());
				String returnData = JsonUtil.map2json(delMap);
				
				Map<String, String> map = new HashMap<String, String>();
				map.put("data", data);
				map.put("nextData", returnData);
				
				String returnVal = JsonUtil.map2json(map);
				return returnVal;
			}else{
				// 查询下一有同步日志的数据类型
				String nextDataType = DataSynchizeUtil.queryNextDataType(clientId, user.getId(), timeStamp, dataClass, dataTypes, synchLogService);
				if(nextDataType != null){
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.REQUEST.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.DELETE.toString());
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.BATCHDATA.toString());
					
					Map<String, String> delMap = new HashMap<String, String>();
					delMap.put(HeaderName.DATATYPE.toString(), nextDataType);
					String returnData = JsonUtil.map2json(delMap);  // 客户端再次请求需提交的数据类型
					
					Map<String, String> map = new HashMap<String, String>();
					map.put("data", "");
					map.put("nextData", returnData);
					return JsonUtil.map2json(map);
				}else{
					// nextDataType = null, 剩下的数据类型中未找到需同步的日志
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.BATCHDATA.toString());
					
					BatchDataBean bean = new BatchDataBean();
					bean.setDataType(DataType.COMMENT.toString());
					String data = JsonUtil.bean2json(bean);
					Map<String, String> delMap = new HashMap<String, String>();
					delMap.put("dataType", DataType.COMMENT.toString());
					delMap.put("action", DataSynchAction.DELETE.toString());
					String returnData = JsonUtil.map2json(delMap);
					
					Map<String, String> map = new HashMap<String, String>();
					map.put("data", data);
					map.put("nextData", returnData);
					
					String returnVal = JsonUtil.map2json(map);
					return returnVal;
				}
			}
			
		}
	}

	@Override
	@GET
	@Path("/getlogs/{timeStamp}")
	public String getSynchDataByStep(@HeaderParam(SynchConstants.HEADER_CLIENT_ID) String clientId, @PathParam("timeStamp") long timeStamp, @DefaultValue(SynchConstants.DATA_CLASS_ALL) @HeaderParam(SynchConstants.HEADER_DATATYPE) String dataClass, @DefaultValue(SynchConstants.CLIENT_SYNCH_REQUEST) @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) throws Exception {
		AccountEntity user = accountService.getUser4Session();
		String[] dataTypes = DataCacheTool.getDatasSort();
		if(dataClass.equals(DataType.FILE.toString())){ // 请求下载文件
			String downloadData = DataSynchizeUtil.queryDownloadFile(user.getId(), clientId, DataSynchAction.REQUEST.toString(), synchLogService, res);
			return downloadData;
		}
		boolean isDeleteFilter = true;
		List<SynchLogEntity> result = synchLogService.findSynchLogsByTarget(clientId, user.getId(), timeStamp, dataClass, isDeleteFilter);
		// 如果存在需要同步的日志
		if(result != null && !result.isEmpty()){
			if(result.size() == 1){
				SynchLogEntity theLog = result.get(0);
				Map<String, Object> map = DataSynchizeUtil.parseLog(theLog);
				
				String nextDataType = DataSynchizeUtil.queryNextDataType(clientId, user.getId(), timeStamp, theLog.getClassName(), dataTypes, synchLogService);
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
				return JsonUtil.map2json(map);
			}else{
				SynchLogEntity theLog = result.get(0);
				Map<String, Object> map = DataSynchizeUtil.parseLog(theLog);
				SynchLogEntity nextLog = result.get(1);
				res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.REQUEST.toString());
				res.setHeader(HeaderName.NEXT_DATATYPE.toString(), nextLog.getClassName());
				
				res.setHeader(HeaderName.ACTION.toString(), theLog.getAction());
				res.setHeader(HeaderName.DATATYPE.toString(), theLog.getClassName());
				return JsonUtil.map2json(map);
			}
		}else{
			//已经到了最后一个数据类型
			if(dataClass.equals(dataTypes[dataTypes.length - 1]) || dataClass.equals(DataType.ALL.toString())){
				String downloadData = DataSynchizeUtil.queryDownloadFile(user.getId(), clientId, DataSynchAction.REQUEST.toString(), synchLogService, res);
				return downloadData;
			}else if(dataClass.equals(DataType.ALL.toString())){    //所有数据类型均查询不到需要同步的日志
				res.setHeader(SynchConstants.HEADER_ACTION, DataSynchAction.FINISH.toString());
				res.setHeader(SynchConstants.HEADER_DATATYPE, DataType.ALL.toString());
				
				res.setHeader(SynchConstants.HEADER_NEXT_ACTION, DataSynchAction.FINISH.toString());
				res.setHeader(SynchConstants.HEADER_NEXT_DATATYPE, DataType.ALL.toString());
				return "";
			}else{
				String nextDataType = DataSynchizeUtil.queryNextDataType(clientId, user.getId(), timeStamp, dataClass, dataTypes, synchLogService);
				
				res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
				res.setHeader(HeaderName.DATATYPE.toString(), dataClass);
				
				res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.REQUEST.toString());
				res.setHeader(HeaderName.NEXT_DATATYPE.toString(), nextDataType);
				return "";
			}
		}
	}
	
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
	@POST
	@Path("/send/client/a/{clientType}")
	public String registerClient(@PathParam("clientType") String clientType, @Context HttpServletResponse res) {
		String clientId = dataInitService.registerClient(clientType);
		res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
		return clientId;
	}
	
	@Override
	@POST
	@Path("/checkcfg")
	public String checkConfig(@HeaderParam(SynchConstants.HEADER_CLIENT_ID) String clientId, @FormParam("data") String data, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res){
		SynchResult sr = (SynchResult) JsonUtil.getObject4JsonString(data, SynchResult.class);
		if(action.equals(DataSynchAction.FINISH.toString())){
			logger.info("同步完成 ！！！");
			AccountEntity user = accountService.getUser4Session();
			long synchTimeStamp = synchLogService.deleteSynchedLogs(clientId, user.getId());
			sr = synchResultService.findSynchResults(clientId, user.getId());
			if(sr == null){
				sr = new SynchResult();
				sr.setClientId(clientId);
				sr.setUserId(user.getId());
				sr.setLastSynTimestamp(synchTimeStamp);
				synchResultService.addSynResult(sr);
			}else{
				if(synchTimeStamp != 0){
					sr.setLastSynTimestamp(synchTimeStamp);
					synchResultService.updateSynResult(sr);
				}
			}
			return JsonUtil.bean2json(sr);
		}else{
			logger.info("同步开始，检查时间戳：" + sr.getLastSynTimestamp());
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.REQUEST.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.COMMENT.toString());
			return DataSynchAction.SUCCESS.toString();
		}
	}
	
	@Override
	@DELETE
	@Path("/send/client/d/{clientId}")
	public String deleteClient(@PathParam("clientId") String clientId) {
		dataInitService.deleteClient(clientId);
		return "true";
	}

	@Override
	@POST
	@Path("/send/subject/a")
	public String addSubject(@FormParam("data") String data, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) {
		logger.info("添加专题信息 ！！！");
		AccountEntity user = accountService.getUser4Session();
		if(!action.equals(DataSynchAction.FINISH.toString())){
			SubjectEntity subject = (SubjectEntity) JsonUtil.getObject4JsonString(data, SubjectEntity.class);
			
			subject.setCreateUser(user.getId());
			subjectService.addSubject(subject);
			
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.SUBJECT.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.SUBJECT.toString());
		}else{
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.DIRECTORY.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.SUBJECT.toString());
		}
		res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), "" + System.currentTimeMillis());
		ResponseStatus rs = new ResponseStatus();
		
		return rs.toString();
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
			SubjectEntity sub = subjectService.getSubject(subject.getId());
			SynchLogEntity log = synchLogService.findLogByData(user.getClientId(), user.getId(), subject.getUpdateTimeStamp() + 1, SynchConstants.DATA_CLASS_SUBJECT, sub.getId());
			if(sub != null && sub.getDeleted() == Constants.DATA_NOT_DELETED){  // 服务器存在此专题，并且不是删除状态
				//服务器上不存在该数据其它操作日志，或者有修改操作日志并且操作早于此次操作，才更新数据库数据
				if(log == null || (log.getAction().equals(SynchConstants.DATA_OPERATE_UPDATE) && log.getOperateTime() < subject.getUpdateTimeStamp())){
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
				
				res.setHeader(HeaderName.ACTION.toString(), SynchConstants.DATA_OPERATE_DELETE);
				res.setHeader(HeaderName.DATATYPE.toString(), DataType.SUBJECT.toString());
				
				res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), "" + System.currentTimeMillis());
				if(sub != null && sub.getDeleted() == Constants.DATA_DELETED){
					return sub.getUpdateTime() + "";
				}else{
					return log.getOperateTime() + "";
				}
			}
			if(log != null){
				if(log.getAction().equals(SynchConstants.DATA_OPERATE_DELETE)){
					rs.setResponse(ResponseCode.DELETE);
					
					//header中添加控制位
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.SUBJECT.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), SynchConstants.DATA_OPERATE_DELETE);
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.SUBJECT.toString());
					
					res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
					return log.getOperateTime() + "";
				}else if(log.getAction().equals(SynchConstants.DATA_OPERATE_UPDATE) && log.getOperateTime() >= subject.getUpdateTimeStamp()){
					//header中添加控制位
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.SUBJECT.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), SynchConstants.DATA_OPERATE_UPDATE);
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.SUBJECT.toString());
					
					res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
					return JsonUtil.bean2json(sub);
				}else {
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.SUBJECT.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.SUBJECT.toString());
				}
			}
		}else{
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.DIRECTORY.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.SUBJECT.toString());
		}
		res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
		return rs.toString();
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
				subject.setUpdateUser(user.getId());
				subjectService.markDelSubject(subject);
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
	public String addDirectory(@FormParam("data") String data, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) {
		logger.info("添加目录信息 :" + data);
		if(!action.equals(DataSynchAction.FINISH.toString())){
			DirectoryEntity dir = (DirectoryEntity) JsonUtil.getObject4JsonString(data, DirectoryEntity.class);
			AccountEntity user = accountService.getUser4Session();
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
		res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
		ResponseStatus rs = new ResponseStatus(ResponseCode.NEXT);
		return rs.toString();
	}

	@Override
	@POST
	@Path("/send/directory/u/{timeStamp}")
	public String updateDirectory(@FormParam("data") String data, @PathParam("timeStamp") long timeStamp, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) throws Exception {
		logger.info("更新目录信息 ！！！");
		ResponseStatus rs = new ResponseStatus();
		if(!action.equals(DataSynchAction.FINISH.toString())){
			DirectoryEntity directory = (DirectoryEntity) JsonUtil.getObject4JsonString(data, DirectoryEntity.class);
			DirectoryEntity dir = directoryService.getDirectory(directory.getId());
			AccountEntity user = accountService.getUser4Session();
			SynchLogEntity log = synchLogService.findLogByData(user.getClientId(), user.getId(), timeStamp, SynchConstants.DATA_CLASS_DIRECTORY, directory.getId());
			if (dir != null && dir.getDeleted() == Constants.DATA_NOT_DELETED) {
				//服务器上不存在该数据其它操作日志，或者有修改操作日志并且操作早于此次操作，才更新数据库数据
				if(log == null || (log.getAction().equals(SynchConstants.DATA_OPERATE_UPDATE) && log.getOperateTime() < directory.getUpdateTimeStamp())){
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
				rs.setResponse(ResponseCode.DELETE);
				
				//header中添加控制位
				res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
				res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.DIRECTORY.toString());
				
				res.setHeader(HeaderName.ACTION.toString(), SynchConstants.DATA_OPERATE_DELETE);
				res.setHeader(HeaderName.DATATYPE.toString(), DataType.DIRECTORY.toString());
				
				res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
				if(dir != null && dir.getDeleted() == Constants.DATA_DELETED){
					return dir.getUpdateTime() + "";
				}else{
					return log.getOperateTime() + "";
				}
			}
			
			if(log != null){
				if(log.getAction().equals(SynchConstants.DATA_OPERATE_DELETE)){
					rs.setResponse(ResponseCode.DELETE);
					//header中添加控制位
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.DIRECTORY.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), SynchConstants.DATA_OPERATE_DELETE);
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.DIRECTORY.toString());
					
					res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
					return log.getOperateTime() + "";
				}else if(log.getAction().equals(SynchConstants.DATA_OPERATE_UPDATE) && log.getOperateTime() > directory.getUpdateTimeStamp()){
					rs.setResponse(ResponseCode.UPDATE);
					rs.setData(log.getId());
					
					//header中添加控制位
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.DIRECTORY.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), SynchConstants.DATA_OPERATE_UPDATE);
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.DIRECTORY.toString());
					
					res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
					return JsonUtil.bean2json(dir);
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
		res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
		return rs.toString();
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
		directoryService.blacklistedUser(userId, directoryId);
		return new ResponseStatus().toString();
	}

	@Override
	@DELETE
	@Path("/send/directoryblack/d/{directoryId}/{userId}")
	public String deleteDirectoryBlack(@PathParam("directoryId") String directoryId, @PathParam("userId") String userId, @Context HttpServletResponse res) {
		directoryService.removeUser4lacklist(userId, directoryId);
		return new ResponseStatus().toString();
	}

	@Override
	@POST
	@Path("/send/note/a")
	public String addNote(@FormParam("data") String data, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) {
		logger.info("添加条目信息 :" + data);
		System.out.println(data);
		ResponseStatus rs = new ResponseStatus(ResponseCode.NEXT);
		if(!action.equals(DataSynchAction.FINISH.toString())){
			NoteEntity note = (NoteEntity) JsonUtil.getObject4JsonString(data, NoteEntity.class);
			AccountEntity user = accountService.getUser4Session();
			if(!StringUtil.isEmpty(note.getContent())){
				note.setMd5(MD5FileUtil.getMD5String(note.getContent()));
			}
			note.setCreateUser(user.getId());
			note.setDeleted(Constants.DATA_NOT_DELETED);
			noteService.addNote(note);
			
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.NOTE.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.NOTE.toString());
		}else{
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.ATTACHMENT.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.NOTE.toString());
		}
		res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
		return rs.toString();
	}

	@Override
	@POST
	@Path("/send/note/u/{timeStamp}")
	public String updateNote(@FormParam("data") String data, @PathParam("timeStamp") long timeStamp, @DefaultValue("true") @QueryParam("updateContent") boolean updateContent, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) throws Exception {
		logger.info("更新条目基本信息 ！！！");
		ResponseStatus rs = new ResponseStatus();
		if(!action.equals(DataSynchAction.FINISH.toString())){
			NoteEntity note = (NoteEntity) JsonUtil.getObject4JsonString(data, NoteEntity.class);
			NoteEntity n = noteService.getNote(note.getId());
			AccountEntity user = accountService.getUser4Session();
			SynchLogEntity log = synchLogService.findLogByData(user.getClientId(), user.getId(), timeStamp, SynchConstants.DATA_CLASS_NOTE, note.getId());
			if (n != null && n.getDeleted() == Constants.DATA_NOT_DELETED){
				//服务器上不存在该数据其它操作日志，或者有修改操作日志并且操作早于此次操作，才更新数据库数据
				if(log == null || (log.getAction().equals(SynchConstants.DATA_OPERATE_UPDATE) && log.getOperateTime() < note.getUpdateTimeStamp())){
					note.setUpdateUser(user.getId());
					note.setUpdateUserId(user.getId());
					note.setDeleted(Constants.DATA_NOT_DELETED);
					ReflectionUtils.copyBeanProperties(note, n);
					noteService.updateNote(n, updateContent);
					
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.NOTE.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.NOTE.toString());
				}
			}else{
				rs.setResponse(ResponseCode.DELETE);
				
				//header中添加控制位
				res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
				res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.NOTE.toString());
				
				res.setHeader(HeaderName.ACTION.toString(), SynchConstants.DATA_OPERATE_DELETE);
				res.setHeader(HeaderName.DATATYPE.toString(), DataType.NOTE.toString());
				
				res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
				if(n != null && n.getDeleted() == Constants.DATA_DELETED){
					return n.getUpdateTime() + "";
				}else{
					return log.getOperateTime() + "";
				}
			}
	
			if(log != null){
				if(log.getAction().equals(SynchConstants.DATA_OPERATE_DELETE)){
					rs.setResponse(ResponseCode.DELETE);
					
					//header中添加控制位
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.NOTE.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), SynchConstants.DATA_OPERATE_DELETE);
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.NOTE.toString());
					
					res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
					return log.getOperateTime() + "";
				}else if(log.getAction().equals(SynchConstants.DATA_OPERATE_UPDATE) && log.getOperateTime() >= note.getUpdateTimeStamp()){
					rs.setResponse(ResponseCode.UPDATE);
					rs.setData(log.getId());
					
					//header中添加控制位
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.NOTE.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), SynchConstants.DATA_OPERATE_UPDATE);
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.NOTE.toString());
					
					noteService.saveNoteHistory(note, user.getId());   //保存为历史版本
					
					res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
					return JsonUtil.bean2json(note);
				}else {
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.NOTE.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.NOTE.toString());
				}
			}
		}else{
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.ATTACHMENT.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.NOTE.toString());
		}
		
		res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
		return rs.toString();
	}

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

	@Override
	@POST
	@Path("/send/noteblack/a/{noteId}/{userId}")
	public String addNoteBlack(@PathParam("noteId") String noteId, @PathParam("userId") String userId, @Context HttpServletResponse res) {
		noteService.blacklistedUser(userId, noteId);
		return new ResponseStatus().toString();
	}

	@Override
	@DELETE
	@Path("/send/noteblack/d/{noteId}/{userId}")
	public String deleteNoteBlack(@PathParam("noteId") String noteId, @PathParam("userId") String userId, @Context HttpServletResponse res) {
		noteService.removeUser4blacklist(userId, noteId);
		return new ResponseStatus().toString();
	}

	@Override
	@POST
	@Path("/send/tag/a")
	public String addTag(@FormParam("data") String data, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) {
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
		res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
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
			
			SynchLogEntity log = synchLogService.findLogByData(user.getClientId(), user.getId(), timeStamp, SynchConstants.DATA_CLASS_DIRECTORY, tag.getId());
			if (t != null) {
				//服务器上不存在该数据其它操作日志，或者有修改操作日志并且操作早于此次操作，才更新数据库数据
				if(log == null || (log.getAction().equals(SynchConstants.DATA_OPERATE_UPDATE) && log.getOperateTime() < tag.getUpdateTimeStamp())){
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
				rs.setResponse(ResponseCode.DELETE);
				
				//header中添加控制位
				res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
				res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.TAG.toString());
				
				res.setHeader(HeaderName.ACTION.toString(), SynchConstants.DATA_OPERATE_DELETE);
				res.setHeader(HeaderName.DATATYPE.toString(), DataType.TAG.toString());
				
				res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
				return log.getOperateTime() + "";
			}
			
			if(log != null){
				if(log.getAction().equals(SynchConstants.DATA_OPERATE_DELETE)){
					rs.setResponse(ResponseCode.DELETE);
					//header中添加控制位
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.TAG.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), SynchConstants.DATA_OPERATE_DELETE);
					res.setHeader(HeaderName.DATATYPE.toString(), DataType.TAG.toString());
					
					res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
					return log.getOperateTime() + "";
				}else if(log.getAction().equals(SynchConstants.DATA_OPERATE_UPDATE) && log.getOperateTime() >= tag.getUpdateTimeStamp()){
					rs.setResponse(ResponseCode.UPDATE);
					rs.setData(log.getId());
					
					res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
					res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.TAG.toString());
					
					res.setHeader(HeaderName.ACTION.toString(), SynchConstants.DATA_OPERATE_UPDATE);
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
		res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
		return rs.toString();
	}

	@Override
	@DELETE
	@Path("/send/tag/d/{id}")
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
		res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
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
		AccountEntity user = accountService.getUser4Session();
		if(!action.equals(DataSynchAction.FINISH.toString())){
			CommentEntity comment = (CommentEntity) JsonUtil.getObject4JsonString(data, CommentEntity.class);
			comment.setCreateUser(user.getId());
			commentService.addComment(comment);
			
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.COMMENT.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.SUCCESS.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.COMMENT.toString());
		}else{
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.FINISH.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.FILE.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.COMMENT.toString());
			
			//准备上传文件
			return DataSynchizeUtil.queryUploadFile(user.getId(), attachmentService, res);
		}
		res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
		return "";
	}

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
	@Path("/send/subjectuser/a/{subjectId}/{userId}/{roleId}")
	public String addSubjectMember(@PathParam("subjectId") String subjectId, @PathParam("userId") String userId, @PathParam("roleId") String roleId, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) {
		if (StringUtil.isEmpty(roleId)) {
			Role role = roleService.findRoleByName(RoleName.READER);
			roleId = role.getId();
		}
		roleService.addRoleUser(subjectId, userId, roleId);
		return new ResponseStatus().toString();
	}

	@Override
	@DELETE
	@Path("/send/subjectuser/d/{subjectId}/{userId}")
	public String deleteSubjectMember(@PathParam("subjectId") String subjectId, @PathParam("userId") String userId, @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res) {
		roleService.removeRoleUser(subjectId, userId);
		return new ResponseStatus().toString();
	}
	
	@Override
	@POST
	@Path("/send/batchdata")
	public String deleteBatchData(@FormParam("data") String data, @DefaultValue(SynchConstants.CLIENT_SYNCH_SEND) @HeaderParam(SynchConstants.HEADER_ACTION) String action, @Context HttpServletResponse res){
		if(!StringUtil.isEmpty(data)){
			String[] dataTypes = DataCacheTool.getReverseDatasSort();
			logger.info("批量删除数据：" + data);
			Map map = JsonUtil.getMap4Json(data);
			String dataType = map.get("dataType").toString();
			
			if(!action.equals(DataSynchAction.FINISH.toString())){
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
						subjectService.markDelSubject(subject);
					}
				}else if(dataType.equals(DataType.DIRECTORY.toString())){
					for(Object obj : list){
						Map<String, String> objMap = (Map<String, String>) obj;
						String id = objMap.get("id");
						DirectoryEntity dir = directoryService.getDirectory(id);
						directoryService.markDelDirectory(dir);
					}
				}else if(dataType.equals(DataType.TAG.toString())){
					for(Object obj : list){
						Map<String, String> objMap = (Map<String, String>) obj;
						String id = objMap.get("id");
						tagService.deleteTagById(id.toString());
					}
				}else if(dataType.equals(DataType.NOTE.toString())){
					for(Object obj : list){
						Map<String, String> objMap = (Map<String, String>) obj;
						String id = objMap.get("id");
						NoteEntity note = noteService.getNote(id.toString());
						noteService.markDelNote(note);
					}
				}else if(dataType.equals(DataType.COMMENT.toString())){
					for(Object obj : list){
						Map<String, String> objMap = (Map<String, String>) obj;
						String id = objMap.get("id");
						commentService.deleteComment(id.toString());
					}
				}
				res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
				res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
				
				res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.DELETE.toString());
				res.setHeader(HeaderName.DATATYPE.toString(), DataType.BATCHDATA.toString());
				
			}else{
				res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
				res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.BATCHDATA.toString());
				
				res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
				res.setHeader(HeaderName.DATATYPE.toString(), DataType.BATCHDATA.toString());
			}
			BatchDataBean bean = new BatchDataBean();
			String nextDataType = DataSynchizeUtil.getNextDataType(dataType, dataTypes);
			if(nextDataType != null){
				bean.setDataType(dataType);
				bean.setDatas(null);
				
				String str = JsonUtil.bean2json(bean);
				Map<String, String> delMap = new HashMap<String, String>();
				delMap.put("dataType", nextDataType);
				delMap.put("action", DataSynchAction.DELETE.toString());
				String returnData = JsonUtil.map2json(delMap);
				
				Map<String, String> resultMap = new HashMap<String, String>();
				resultMap.put("data", str);
				resultMap.put("nextData", returnData);
				
				String returnVal = JsonUtil.map2json(resultMap);
				return returnVal;
			}else{  //nextDataType == null 批量删除操作完成了
				res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
				res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.SUBJECT.toString());
				
				res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
				res.setHeader(HeaderName.DATATYPE.toString(), DataType.BATCHDATA.toString());
			}
		}else{
			// 这段代码应该走不到
			res.setHeader(HeaderName.NEXT_ACTION.toString(), DataSynchAction.SEND.toString());
			res.setHeader(HeaderName.NEXT_DATATYPE.toString(), DataType.SUBJECT.toString());
			
			res.setHeader(HeaderName.ACTION.toString(), DataSynchAction.NEXT.toString());
			res.setHeader(HeaderName.DATATYPE.toString(), DataType.BATCHDATA.toString());
		}
		return "";
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
			subjectService.addSubject(subject);
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

}