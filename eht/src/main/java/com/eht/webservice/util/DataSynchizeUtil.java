package com.eht.webservice.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.jeecgframework.core.util.StringUtil;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;
import org.springframework.util.StringUtils;

import com.eht.common.bean.ResponseStatus;
import com.eht.common.constant.Constants;
import com.eht.common.constant.SynchConstants;
import com.eht.common.enumeration.DataSynchAction;
import com.eht.common.enumeration.DataType;
import com.eht.common.enumeration.HeaderName;
import com.eht.common.enumeration.ResponseCode;
import com.eht.common.util.AppContextUtils;
import com.eht.common.util.FilePathUtil;
import com.eht.common.util.FileToolkit;
import com.eht.common.util.HtmlParser;
import com.eht.common.util.JsonUtil;
import com.eht.common.util.UUIDGenerator;
import com.eht.common.util.XmlUtil;
import com.eht.log.entity.SynchLogEntity;
import com.eht.log.entity.SynchronizedLogEntity;
import com.eht.log.service.SynchLogServiceI;
import com.eht.note.entity.AttachmentEntity;
import com.eht.note.entity.NoteEntity;
import com.eht.note.service.AttachmentServiceI;
import com.eht.note.service.NoteServiceI;
import com.eht.resource.service.ResourcePermissionService;
import com.eht.role.entity.RoleUser;
import com.eht.subject.entity.SubjectEntity;
import com.eht.subject.service.SubjectServiceI;
import com.eht.user.entity.AccountEntity;
import com.eht.webservice.bean.Step;

public class DataSynchizeUtil {
	
	private static Logger logger = Logger.getLogger(DataSynchizeUtil.class);
	
	private static final String DELIMITER = ":";
	/**
	 * 服务器附件复制
	 * @param attaServer
	 * @param attachment
	 * @return
	 */
	public static ResponseStatus copyServerFile(AttachmentEntity attaServer, AttachmentEntity attachment){
		ResponseStatus res = new ResponseStatus(); //上传文件操作结果
		String zipName = attaServer.getFileName().substring(0, attaServer.getFileName().lastIndexOf('.')) + ".zip";
		try {
			FileToolkit.copyFile(attaServer.getFilePath() + File.separator + zipName, attachment.getFilePath());
		} catch (IOException e) {
			logger.error("服务器复制文件失败！！！");
			e.printStackTrace();
			res = new ResponseStatus(ResponseCode.SERVER_ERROR);
			e.printStackTrace();
			return res;
		}
		if(!attaServer.getFileName().equals(attachment.getFileName())){
			String newZipName = attachment.getFileName().substring(0, attachment.getFileName().lastIndexOf('.')) + ".zip";
			FileToolkit.renameFile(attachment.getFilePath() + File.separator + zipName, attachment.getFilePath() + File.separator + newZipName);
		}
		
		attachment.setStatus(Constants.FILE_TRANS_COMPLETED);
		attachment.setTranSfer(attaServer.getTranSfer());
		
		return res;
	}
	
	/**
	 * 组织日志格式，返回客户端
	 * @param log
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map<String, Object> parseLog(SynchLogEntity log){
		Serializable data = null;
		Map<String, Object> map = new HashMap<String, Object>();
		SynchLogServiceI synchLogService = AppContextUtils.getBean("synchLogService");
		if (!log.getAction().equals(DataSynchAction.DELETE) && !log.getClassName().equals(DataType.DIRECTORYBLACK.toString()) && !log.getClassName().equals(DataType.NOTEBLACK.toString())) {
			Class c = SynchDataCache.getDataClass(log.getClassName());
			data = synchLogService.getEntity(c, log.getClassPK());
			
			if(log.getClassName().equals(DataType.SUBJECTUSER.toString())){
				String dataJson = JsonUtil.bean2json(data);
				map = JsonUtil.getMap4Json(dataJson);
				RoleUser ru = (RoleUser) data;
				map.put("roleId", ru.getRoleName());
				
				Object updateUserId = map.get("updateUserId");
				Object updateTimeStamp = map.get("updateTimeStamp");
				if(updateUserId == null || "".equals(updateUserId.toString())){
					map.put("updateUserId", map.get("createUserId"));
				}
				if(updateTimeStamp == null || "".equals(updateTimeStamp.toString())){
					map.put("updateTimeStamp", map.get("createTimeStamp"));
				}
			}else if(log.getClassName().equals(DataType.USER.toString())){
				AccountEntity user = (AccountEntity) data;
				map.put("id", user.getId());
				map.put("userName", user.getUserName());
				map.put("email", user.getEmail());
				map.put("photo", user.getPhoto());
				map.put("createTimeStamp", user.getCreateTimeStamp());
				map.put("updateTimeStamp", user.getUpdateTimeStamp());
				map.put("createUserId", user.getCreateUserId());
				map.put("updateUserId", user.getUpdateUserId());
			}else{
				String dataJson = JsonUtil.bean2json(data);
				map = JsonUtil.getMap4Json(dataJson);
				
				Object updateUserId = map.get("updateUserId");
				Object updateTimeStamp = map.get("updateTimeStamp");
				if(updateUserId == null || "".equals(updateUserId.toString())){
					map.put("updateUserId", map.get("createUserId"));
				}
				if(updateTimeStamp == null || "".equals(updateTimeStamp.toString())){
					map.put("updateTimeStamp", map.get("createTimeStamp"));
				}
			}
		}
		
		map.put("operation", log.getAction());
		map.put("className", log.getClassName());
		map.put("classPK", log.getClassPK());
		map.put("operateTime", log.getOperateTime());
		map.put("synchTime", log.getSynchTime());
		
		return map;
	}
	
	/**
	 * 组织删除类型日志格式，返回客户端
	 * @param log
	 * @return
	 */
	public static Map<String, Object> parseDeleteLog(SynchLogEntity log){
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("id", log.getClassPK());
		map.put("operation", log.getAction());
		map.put("updateUserId", log.getOperateUser());
		map.put("updateTimeStamp", log.getOperateTime());
		map.put("synchTime", log.getSynchTime());
		
		return map;
	}
	
	public static String queryUploadFile(String userId, HttpServletResponse res){
		AttachmentServiceI attachmentService = AppContextUtils.getBean("attachmentService");
		//查询要上传的文件
		List<AttachmentEntity> list = attachmentService.findNeedUploadAttachmentByUser(userId, new Integer[]{Constants.FILE_TYPE_NORMAL, Constants.FILE_TYPE_NOTEHTML});
		if(list != null && !list.isEmpty()){
			res.setHeader(SynchConstants.HEADER_NEXT_ACTION, DataSynchAction.UPLOAD.toString());
			res.setHeader(SynchConstants.HEADER_NEXT_DATATYPE, DataType.FILE.toString());
			AttachmentEntity attachment = list.remove(0);
			Map<String, String> map = new HashMap<String, String>();
			if(attachment.getFileType().intValue() == Constants.FILE_TYPE_NORMAL){
				map.put(SynchConstants.HEADER_HOST_DATATYPE, DataType.ATTACHMENT.toString());
				map.put(SynchConstants.HEADER_HOST_UUID, attachment.getId());
			}else{
				map.put(SynchConstants.HEADER_HOST_DATATYPE, DataType.NOTE.toString());
				map.put(SynchConstants.HEADER_HOST_UUID, attachment.getNoteId());
			}
			return JsonUtil.map2json(map);
		}else{
			res.setHeader(SynchConstants.HEADER_NEXT_ACTION, DataSynchAction.REQUEST.toString());
			res.setHeader(SynchConstants.HEADER_NEXT_DATATYPE, DataType.ALL.toString());
			return "";
		}
	}
	
	public static String queryDownloadFile(String userId, String clientId, String nextAction, HttpServletResponse res) throws Exception{
		SynchLogServiceI synchLogService = AppContextUtils.getBean("synchLogService");
		//查询要下载的文件
		List<SynchronizedLogEntity>	list = synchLogService.findNeedDownloadFile(userId, clientId);
		res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
		if(list != null && !list.isEmpty()){
			res.setHeader(SynchConstants.HEADER_ACTION, DataSynchAction.DOWNLOAD.toString());
			res.setHeader(SynchConstants.HEADER_DATATYPE, DataType.FILE.toString());
			
			res.setHeader(SynchConstants.HEADER_NEXT_ACTION, nextAction);
			res.setHeader(SynchConstants.HEADER_NEXT_DATATYPE, DataType.FILE.toString());
			
			SynchronizedLogEntity attachmentLog = list.remove(0);
			Map<String, String> map = new HashMap<String, String>();
			map.put(SynchConstants.HEADER_HOST_DATATYPE, attachmentLog.getClassName());
			map.put(SynchConstants.HEADER_HOST_UUID, attachmentLog.getClassPK());
			attachmentLog.setStatus(SynchConstants.LOG_SYNCHRONIZED);
			synchLogService.updateEntitie(attachmentLog);
			return JsonUtil.map2json(map);
		}else{
			res.setHeader(SynchConstants.HEADER_ACTION, DataSynchAction.FINISH.toString());
			res.setHeader(SynchConstants.HEADER_DATATYPE, DataType.FILE.toString());
			
			res.setHeader(SynchConstants.HEADER_NEXT_ACTION, DataSynchAction.FINISH.toString());
			res.setHeader(SynchConstants.HEADER_NEXT_DATATYPE, DataType.FILE.toString());
			
			return "";
		}
	}
	
	/**
	 * 查询应返回客户端的下一数据类型
	 * @param clientId
	 * @param userId
	 * @param timeStamp
	 * @param currentDataType
	 * @param dataTypes
	 * @param synchLogService
	 * @return
	 */
	public static String queryNextDataType(String clientId, String userId, long timeStamp, String action, String currentDataType, String[] dataTypes, SynchLogServiceI synchLogService){
		// 查询下一有同步日志的数据类型
		int index = -1;
		String nextDataType = null;
		// 当前数据类型，日志合并后只有一条日志需同步到客户端，在剩下的数据类型中查询是否有日志需同步
		for(int i = 0; i < dataTypes.length; i++){
			if(dataTypes[i].equals(currentDataType)){
				index = i;
			}
			if(index != -1 && i > index){
				int count = synchLogService.countSynchLogsByTarget(clientId, userId, timeStamp, dataTypes[i], action);
				// 找到有同步日志的数据类型，类型返给客户端
				if(count > 0){
					nextDataType = dataTypes[i];
					break;
				}
			}
		}
		return nextDataType;
	}
	
	/**
	 * 返回下一要同步的数据类型
	 * @param currentDataType
	 * @param dataTypes
	 * @return
	 */
	public static String getNextDataType(String currentDataType, String[] dataTypes){
		for(int i = 0; i < dataTypes.length; i++){
			if(dataTypes[i].equals(currentDataType)){
				if(i < dataTypes.length - 1){
					return dataTypes[i + 1];
				}else{
					return null;
				}
			}
		}
		return null;
	}
	
	public static void readSynConfig(){
		String filePath = FilePathUtil.getClassPath() + File.separator + "synch_config.xml";
		Document document = XmlUtil.readXmlFile(filePath);
		Element root = document.getRootElement();
		Element dataTypeElement = XmlUtil.getUniqueElement(root, "datatypes");
		
		// 添加、更新时数据类型的同步顺序
		Element datasSortEle = XmlUtil.getUniqueElement(dataTypeElement, "datasSort");
		String[] datasSort = datasSortEle.getTextTrim().split(",");
		SynchDataCache.setDatasSort(datasSort);
		
		// 删除时数据类型的同步顺序
		Element datasDelSortEle = XmlUtil.getUniqueElement(dataTypeElement, "datasDeleteSort");
		String[] datasDelSort = datasDelSortEle.getTextTrim().split(",");
		SynchDataCache.setDatasDeleteSort(datasDelSort);
		
		Element stepsElement = XmlUtil.getUniqueElement(root, "steps");
		List<?> stepEleList = XmlUtil.getElementsByNodeName(stepsElement, "step");
		List<Step> stepList = new ArrayList<Step>();
		for(Object obj : stepEleList){
			Element stepEle = (Element) obj;
			String name = stepEle.attributeValue("name");
			String description = stepEle.attributeValue("description");
			
			Step step = new Step();
			step.setDescription(description);
			step.setName(name);
			
			Element redirectELe = XmlUtil.getUniqueElement(stepEle, "redirect_requirement");
			List<?> headList = XmlUtil.getElementsByNodeName(redirectELe, "header");
			for(Object o : headList){
				Element headEle = (Element) o;
				if(headEle.attributeValue("name").equals(HeaderName.NEXT_ACTION.toString())){
					step.setRequireNextAction(headEle.getTextTrim());
				}
				if(headEle.attributeValue("name").equals(HeaderName.NEXT_DATATYPE.toString())){
					step.setRequireNextDataType(headEle.getTextTrim());
				}
			}
			stepList.add(step);
		}
		SynchDataCache.setStepList(stepList);
	}
	
	/**
	 * 解压条目压缩包
	 * @param zipFile
	 * @param dstFolder
	 * @throws IOException
	 */
	public static String unZipNoteHtml(File zipFile, String dstFolder) throws IOException{
		ZipInputStream zins = new ZipInputStream(new FileInputStream(zipFile));
		ZipEntry entry = null;
		StringBuilder sb = new StringBuilder();
		while((entry = zins.getNextEntry()) != null){
			String name = entry.getName();
			if(entry.isDirectory()){
				name = name.substring(0, name.length() - 1);
				File folder = new File(dstFolder, name);
				if(!folder.exists()){
					folder.mkdirs();
				}
			}else{
				File file = new File(dstFolder + File.separator + name);
				if(!file.exists()){
					File parentFile = file.getParentFile();
					if(!parentFile.exists()){
						parentFile.mkdirs();
					}
					file.createNewFile();
				}
				
				FileOutputStream fos = new FileOutputStream(file);
				int len = 0;
				byte[] buffer = new byte[1024];
				while((len = zins.read(buffer)) != -1){
					fos.write(buffer, 0, len);
					if(name.endsWith(".html")){
						sb.append(new String(buffer, "UTF-8"));
					}
					fos.flush();
				}
				fos.close();
			}
			
		}
		zins.close();
		return sb.toString();
	}
	
	/**
	 * 压缩条目文件
	 * @param zipFile
	 * @param dstFolder
	 * @throws IOException
	 */
	public static void zipNoteHtml(File zipFile, File htmlFile) throws IOException{
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
		
		ZipEntry entry = new ZipEntry(htmlFile.getName());
		FileInputStream fis = new FileInputStream(htmlFile);
		zos.putNextEntry(entry);
		if(htmlFile.isFile()){
			int len = 0;
			byte[] buffer = new byte[1024];
			while((len = fis.read(buffer)) != -1){
				zos.write(buffer, 0, len);
			}
			zos.closeEntry();
			fis.close();
		}
		
		// 压缩files文件夹
		File folder = new File(htmlFile.getParent(), "files");
		if(folder.exists() && folder.isDirectory()){
			zipNoteFolder(zos, folder, folder.getName());
		}
		zos.finish();
		zos.close();
	}
	
	private static void zipNoteFolder(ZipOutputStream zos, File folder, String base) throws IOException{
		if(!base.equals("")){
			base = base + File.separator;
		}
		ZipEntry dirEntry = new ZipEntry(base);
		zos.putNextEntry(dirEntry);
		
		File[] files = folder.listFiles();
		if(files.length <= 0){
			zos.closeEntry();
		}
		
		for(File file : files){
			if(file.isFile()){
				ZipEntry entry = new ZipEntry(base + file.getName());
				FileInputStream fis = new FileInputStream(file);
				zos.putNextEntry(entry);
				int len = 0;
				byte[] buffer = new byte[1024];
				while((len = fis.read(buffer)) != -1){
					zos.write(buffer, 0, len);
				}
				zos.closeEntry();
				fis.close();
			}else{
				zipNoteFolder(zos, file, base + file.getName());
			}
		}
		
	}
	
	/**
	 * 将客户端上传的HTML内容同步到数据库中
	 * @param noteId
	 * @param zipName
	 */
	public static void synchNoteContent(String noteId, File zipFile){
		NoteServiceI noteService = AppContextUtils.getBean("noteService");
		NoteEntity note = noteService.getNote(noteId);
		String htmlPath = FilePathUtil.getNoteHtmlPath(note);
		try {
			String content = unZipNoteHtml(zipFile, htmlPath);
			content = HtmlParser.replaceClientHtmlImg(content, "../../notes/"+note.getSubjectId()+"/"+note.getId()+"/");
			note.setContent(content);
			
			//noteService.updateEntitie(note);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void addNoteHtmlFile(NoteEntity note){
		AttachmentServiceI attachmentService = AppContextUtils.getBean("attachmentService");
		List<AttachmentEntity> list = attachmentService.findAttachmentByNote(note.getId(), Constants.FILE_TYPE_NOTEHTML);
		if(list == null || list.isEmpty()){
			AttachmentEntity atta = new AttachmentEntity();
			atta.setId(UUIDGenerator.uuid());
			atta.setCreateTime(note.getCreateTime());
			atta.setCreateTimeStamp(note.getCreateTimeStamp());
			atta.setCreateUser(StringUtil.isEmpty(note.getUpdateUser()) ? note.getCreateUser() : note.getUpdateUser());
			atta.setCreateUserId(note.getCreateUserId());
			atta.setDeleted(note.getDeleted());
			atta.setDirectoryId(note.getDirId());
			atta.setFileName(FilePathUtil.getNoteZipFileName(note.getId(), null));
			atta.setFilePath(FilePathUtil.getNoteHtmlPath(note));
			atta.setFileType(Constants.FILE_TYPE_NOTEHTML);
			atta.setNoteId(note.getId());
			atta.setStatus(Constants.FILE_TRANS_NOT_COMPLETED);
			atta.setSuffix(FilenameUtils.getExtension(atta.getFileName()));
			atta.setTranSfer(0L);
			atta.setUpdateTime(note.getUpdateTime());
			atta.setUpdateTimeStamp(note.getUpdateTimeStamp());
			atta.setUpdateUser(note.getUpdateUser());
			atta.setUpdateTimeStamp(note.getUpdateTimeStamp());
			attachmentService.save(atta);
		}
	}
	
	public static void updateNoteHtmlFile(NoteEntity note){
		AttachmentServiceI attachmentService = AppContextUtils.getBean("attachmentService");
		
		List<AttachmentEntity> list = attachmentService.findAttachmentByNote(note.getId(), Constants.FILE_TYPE_NOTEHTML);
		if(list != null && !list.isEmpty()){
			AttachmentEntity atta = list.get(0);
			atta.setTranSfer(0L);
			atta.setStatus(Constants.FILE_TRANS_NOT_COMPLETED);
			
			atta.setUpdateTime(note.getUpdateTime());
			atta.setUpdateTimeStamp(note.getUpdateTimeStamp());
			atta.setUpdateUser(note.getUpdateUser());
			atta.setUpdateTimeStamp(note.getUpdateTimeStamp());
			atta.setDeleted(note.getDeleted());
			
			attachmentService.save(atta);
		}else{
			//现在没用
			AttachmentEntity atta = new AttachmentEntity();
			atta.setId(UUIDGenerator.uuid());
			atta.setCreateTime(note.getCreateTime());
			atta.setCreateTimeStamp(note.getCreateTimeStamp());
			atta.setCreateUser(StringUtil.isEmpty(note.getUpdateUser()) ? note.getCreateUser() : note.getUpdateUser());
			atta.setCreateUserId(note.getCreateUserId());
			atta.setDeleted(note.getDeleted());
			atta.setDirectoryId(note.getDirId());
			atta.setFileName(FilePathUtil.getNoteZipFileName(note.getId(), null));
			atta.setFilePath(FilePathUtil.getNoteHtmlPath(note));
			atta.setFileType(Constants.FILE_TYPE_NOTEHTML);
			atta.setNoteId(note.getId());
			atta.setStatus(Constants.FILE_TRANS_NOT_COMPLETED);
			atta.setSuffix(FilenameUtils.getExtension(atta.getFileName()));
			atta.setTranSfer(0L);
			atta.setUpdateTime(note.getUpdateTime());
			atta.setUpdateTimeStamp(note.getUpdateTimeStamp());
			atta.setUpdateUser(note.getUpdateUser());
			atta.setUpdateTimeStamp(note.getUpdateTimeStamp());
		}
		
	}
	
	public static boolean hasPermission(String userId, String subjectId, String actionName){
		SubjectServiceI subjectService = AppContextUtils.getBean("subjectService");
		SubjectEntity subject = subjectService.getSubject(subjectId);
		if(subject.getSubjectType().intValue() == Constants.SUBJECT_TYPE_P){
			return true;
		}
		
		ResourcePermissionService resourcePermissionService = AppContextUtils.getBean("resourcePermissionService");
		Map<String, String> map = resourcePermissionService.findSubjectPermissionsByUser(userId, subjectId);
		if(map.get(actionName).equals("true")){
			return true;
		}else{
			return false;
		}
	}
	
	public static void saveBanLog(String dataType, String action, String primaryKey, String operateUser, String targetUser, long operateTime, long synchTime){
		SynchLogEntity log = new SynchLogEntity();
		log.setId(UUIDGenerator.uuid());
		log.setClassName(dataType);
		log.setAction(action);
		log.setClassPK(primaryKey);
		log.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
		log.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
		log.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
		log.setOperateUser(operateUser);
		log.setOperateTime(operateTime);
		log.setTargetUser(targetUser);
		log.setSynchTime(synchTime);
		
		SynchLogServiceI synchLogService = AppContextUtils.getBean("synchLogService");
		synchLogService.saveSynchLog(log);
	}
	
	public static String[] decodeCookie(String cookieValue) throws InvalidCookieException {
        for (int j = 0; j < cookieValue.length() % 4; j++) {
            cookieValue = cookieValue + "=";
        }

        if (!Base64.isBase64(cookieValue.getBytes())) {
            throw new InvalidCookieException( "Cookie token was not Base64 encoded; value was '" + cookieValue + "'");
        }

        String cookieAsPlainText = new String(Base64.decode(cookieValue.getBytes()));

        String[] tokens = StringUtils.delimitedListToStringArray(cookieAsPlainText, DELIMITER);

        if ((tokens[0].equalsIgnoreCase("http") || tokens[0].equalsIgnoreCase("https")) && tokens[1].startsWith("//")) {
            // Assume we've accidentally split a URL (OpenID identifier)
            String[] newTokens = new String[tokens.length - 1];
            newTokens[0] = tokens[0] + ":" + tokens[1];
            System.arraycopy(tokens, 2, newTokens, 1, newTokens.length - 1);
            tokens = newTokens;
        }

        return tokens;
    }
	
	public static void main(String[] args){
		File file = new File("E:\\webroot\\wtpwebapps\\eht\\notes\\621c09c19f4c4f45a1b2ffbc12702874\\dbd23cff97e944c894388a6eba71a306\\dbd23cff97e944c894388a6eba71a306.zip");
		System.out.println(file.exists());
		System.out.println(file.canRead());
		try {
			unZipNoteHtml(file, "e:\\");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
