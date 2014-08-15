package com.eht.common.util;

import java.io.File;

import org.jeecgframework.core.util.StringUtil;

import com.eht.note.entity.AttachmentEntity;
import com.eht.note.entity.NoteEntity;

/**
 * 获取系统路径
 * @author chenlong
 *
 */
public class FilePathUtil {
	private static String webRootPath = null;
	static{
		webRootPath = Thread.currentThread().getContextClassLoader().getResource("../../").getPath();
		if(webRootPath.startsWith("/")){
			webRootPath.substring(1);
		}
	}
	
	public static String getClassPath(){
		return getWebInfPath() + File.separator + "classes";
	}
	
	public static String getWebInfPath(){
		return webRootPath + "WEB-INF";
	}
	
	public static String getWebRootPath(){
		return webRootPath;
	}
	
	public static String getFileUploadPath(NoteEntity note, String dirId){
		StringBuilder sb = new StringBuilder(webRootPath + "upload");
		
		String subjectId = null;
		if(note != null){
			subjectId = note.getSubjectId();
			sb.append(File.separator).append(subjectId);
		}
		
		if(!StringUtil.isEmpty(dirId)){
			sb.append(File.separator).append(dirId);
		}
		
		if(note != null){
			sb.append(File.separator).append(note.getId());
		}
		
		return sb.toString();
	}
	
	public static String getNoteHtmlPath(NoteEntity note){
		StringBuffer savePath=new StringBuffer(webRootPath);
		savePath.append("notes").append(File.separator);
		savePath.append(note.getSubjectId());
		savePath.append(File.separator);
		savePath.append(note.getId());
		savePath.append(File.separator);
		return savePath.toString();
	}
	
	/**
	 * 条目附件上传路径
	 * @param subjectId
	 * @param dirId
	 * @param nodeId
	 * @return
	 */
	public static String getFileUploadPath(String  subjectId, String dirId,String nodeId){
		StringBuilder sb = new StringBuilder(webRootPath + "upload");
		
		if(subjectId != null){
			sb.append(File.separator).append(subjectId);
		}
		
		if(!StringUtil.isEmpty(dirId)){
			sb.append(File.separator).append(dirId);
		}
		
		if(nodeId != null){
			sb.append(File.separator).append(nodeId);
		}
		
		return sb.toString();
	}
	
	public static String getImageUploadPath(NoteEntity note){
		StringBuilder sb = new StringBuilder(webRootPath + "upload");
		
		String subjectId = null;
		if(note != null){
			subjectId = note.getSubjectId();
			sb.append(File.separator).append(subjectId);
		}
		
		if(note != null){
			sb.append(File.separator).append(note.getId());
		}
		sb.append(File.separator).append("images");
		return sb.toString();
	}
	
	public static String getImageUrl(NoteEntity note){
		StringBuilder sb = new StringBuilder(AppContextUtils.getContextPath() + "/upload");
		
		String subjectId = null;
		if(note != null){
			subjectId = note.getSubjectId();
			sb.append("/").append(subjectId);
		}
		
		if(note != null){
			sb.append("/").append(note.getId());
		}
		sb.append("/").append("images");
		return sb.toString();
	}
	
	public static void main(String[] argc) {
		System.out.println(getWebRootPath());
		System.out.println(getWebInfPath());
	}
}
