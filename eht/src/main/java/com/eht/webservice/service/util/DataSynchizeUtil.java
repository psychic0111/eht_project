package com.eht.webservice.service.util;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.eht.common.bean.ResponseStatus;
import com.eht.common.cache.DataCacheTool;
import com.eht.common.constant.Constants;
import com.eht.common.constant.SynchConstants;
import com.eht.common.enumeration.DataSynchAction;
import com.eht.common.enumeration.DataType;
import com.eht.common.enumeration.HeaderName;
import com.eht.common.enumeration.ResponseCode;
import com.eht.common.util.AppContextUtils;
import com.eht.common.util.FileToolkit;
import com.eht.common.util.JsonUtil;
import com.eht.log.entity.SynchLogEntity;
import com.eht.log.entity.SynchronizedLogEntity;
import com.eht.log.service.SynchLogServiceI;
import com.eht.note.entity.AttachmentEntity;
import com.eht.note.service.AttachmentServiceI;

public class DataSynchizeUtil {
	
	private static Logger logger = Logger.getLogger(DataSynchizeUtil.class);
	
	/**
	 * 服务器附件复制
	 * @param attaServer
	 * @param attachment
	 * @return
	 */
	public static ResponseStatus copyServerFile(AttachmentEntity attaServer, AttachmentEntity attachment){
		ResponseStatus res = new ResponseStatus(); //上传文件操作结果
		try {
			FileToolkit.copyFile(attaServer.getFilePath() + File.separator + attaServer.getFileName(), attachment.getFilePath());
		} catch (IOException e) {
			logger.error("服务器复制文件失败！！！");
			e.printStackTrace();
			res = new ResponseStatus(ResponseCode.SERVER_ERROR);
			e.printStackTrace();
			return res;
		}
		if(!attaServer.getFileName().equals(attachment.getFileName())){
			FileToolkit.renameFile(attachment.getFilePath() + File.separator + attaServer.getFileName(), attachment.getFilePath() + attachment.getFileName());
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
		if (!log.getAction().equals(DataSynchAction.DELETE) && !log.getClassName().equals(DataType.DIRECTORYBLACK) && !log.getClassName().equals(DataType.NOTEBLACK)
				&& !log.getClassName().equals(DataType.SUBJECTUSER)) {
			Class c = DataCacheTool.getDataClass(log.getClassName());
			data = synchLogService.getEntity(c, log.getClassPK());
			String dataJson = JsonUtil.bean2json(data);
			map = JsonUtil.getMap4Json(dataJson);
		}
		Object updateUserId = map.get("updateUserId");
		Object updateTimeStamp = map.get("updateTimeStamp");
		if(updateUserId == null || "".equals(updateUserId.toString())){
			map.put("updateUserId", map.get("createUserId"));
		}
		if(updateTimeStamp == null || "".equals(updateTimeStamp.toString())){
			map.put("updateTimeStamp", map.get("createTimeStamp"));
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
		
		map.put("UUID", log.getClassPK());
		map.put("operateUser", log.getOperateUser());
		map.put("operateTime", log.getOperateTime());
		map.put("synchTime", log.getSynchTime());
		
		return map;
	}
	
	public static String queryUploadFile(String userId, AttachmentServiceI attachmentService, HttpServletResponse res){
		//查询要上传的文件
		List<AttachmentEntity> list = attachmentService.findNeedUploadAttachmentByUser(userId, Constants.FILE_TYPE_NORMAL);
		res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
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
	
	public static String queryDownloadFile(String userId, String clientId, String nextAction, SynchLogServiceI synchLogService, HttpServletResponse res){
		//查询要下载的文件
		List<SynchronizedLogEntity> list = synchLogService.findNeedDownloadAttachment(userId, clientId);
		res.setHeader(HeaderName.SERVER_TIMESTAMP.toString(), System.currentTimeMillis() + "");
		if(list != null && !list.isEmpty()){
			res.setHeader(SynchConstants.HEADER_ACTION, DataSynchAction.DOWNLOAD.toString());
			res.setHeader(SynchConstants.HEADER_DATATYPE, DataType.FILE.toString());
			
			res.setHeader(SynchConstants.HEADER_NEXT_ACTION, nextAction);
			res.setHeader(SynchConstants.HEADER_NEXT_DATATYPE, DataType.FILE.toString());
			
			SynchronizedLogEntity attachmentLog = list.remove(0);
			Map<String, String> map = new HashMap<String, String>();
			map.put(SynchConstants.HEADER_HOST_DATATYPE, DataType.ATTACHMENT.toString());
			map.put(SynchConstants.HEADER_HOST_UUID, attachmentLog.getClassPk());
			synchLogService.delete(attachmentLog);
			return JsonUtil.map2json(map);
		}else{
			res.setHeader(SynchConstants.HEADER_ACTION, DataSynchAction.FINISH.toString());
			res.setHeader(SynchConstants.HEADER_DATATYPE, DataType.FILE.toString());
			
			res.setHeader(SynchConstants.HEADER_NEXT_ACTION, DataSynchAction.FINISH.toString());
			res.setHeader(SynchConstants.HEADER_NEXT_DATATYPE, DataType.FILE.toString());
			// 日志已经同步完成,删除中间表相关记录
			synchLogService.deleteSynchedLogs(clientId, userId);
			return "";
		}
	}
	
	/**
	 * 解析日志action
	 * @param log
	 * @return
	 */
	public static String queryNextDataType(String clientId, String userId, long timeStamp, String currentDataType, String[] dataTypes, SynchLogServiceI synchLogService){
		// 查询下一有同步日志的数据类型
		int index = -1;
		String nextDataType = null;
		// 当前数据类型，日志合并后只有一条日志需同步到客户端，在剩下的数据类型中查询是否有日志需同步
		for(int i = 0; i < dataTypes.length; i++){
			if(dataTypes[i].equals(currentDataType)){
				index = i;
			}
			if(index != -1 && i > index){
				int count = synchLogService.countSynchLogsByTarget(clientId, userId, timeStamp, dataTypes[i]);
				// 找到有同步日志的数据类型，类型返给客户端
				if(count > 0){
					nextDataType = dataTypes[i];
					break;
				}
			}
		}
		return nextDataType;
	}
}
