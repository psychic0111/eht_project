package com.eht.note.service;

import java.io.Serializable;
import java.util.List;

import org.jeecgframework.core.common.service.CommonService;

import com.eht.note.entity.AttachmentEntity;

public interface AttachmentServiceI extends CommonService{

	/**
	 * 根据文件名获取实体
	 * @param 
	 * @return
	 */
	public List<AttachmentEntity> findAttachmentByFileName(String fileName,String noteid);
	/**
	 * 目录下上传文件总数总数,包括专题中条目中的附件
	 * @param subjectId
	 * @return
	 */
	public Long findAttachmentsByDirCount(String subjectId, String dirId);
	/** 
	 * 目录下上传文件,包括专题中条目中的附件
	 * @param dirid  目录id
	 * @param firstResult 当前页面
	 * @param maxResult 返回条数
	 * @return
	 */
	public List<AttachmentEntity> findAttachmentsByDir(String subjectId, String dirId,int firstResult,int maxResult);
	/**
	 * 添加附件元数据
	 * @param attachment
	 * @return
	 */
	public String addAttachment(AttachmentEntity attachment);
	
	/**
	 * 更新附件元数据
	 * @param attachment
	 * @return
	 */
	public void updateAttachment(AttachmentEntity attachment);
	
	/**
	 * 删除附件
	 * @param attachment
	 * @return
	 */
	public void deleteAttachment(AttachmentEntity attachment);
	
	/**
	 * 删除附件
	 * @param id
	 * @return
	 */
	public void deleteAttachment(Serializable id);
	
	/**
	 * 删除某个条目的所有附件
	 * @param noteId
	 */
	public void deleteAttachment(String noteId); 
	
	/**
	 * 上传附件
	 * @param attachment
	 * @return
	 */
	public String uploadAttachment(AttachmentEntity attachment);
	
	
	/**
	 * 根据id查询附件元数据
	 * @param attachment
	 * @return
	 */
	public AttachmentEntity getAttachment(Serializable id);
	
	/**
	 * 根据md5查询附件元数据
	 * @param md5
	 * @return
	 */
	public AttachmentEntity findAttachmentByMd5(String md5);
	
	/**
	 * 查询条目附件
	 * @param noteId
	 * @return
	 */
	public List<AttachmentEntity> findAttachmentByNote(String noteId, Integer fileType);
	
	/**
	 * 查询条目附件
	 * @param noteId
	 * @return
	 */
	public List<AttachmentEntity> findAttachmentByNote(String noteId, Integer fileType,String searchType);
	
	/**
	 * 查询目录附件
	 * @param dirId
	 * @return
	 */
	public List<AttachmentEntity> findAttachmentByDir(String dirId);
	
	/**
	 * 查询用户附件
	 * @param userId
	 * @param fileType
	 * @return
	 */
	public List<AttachmentEntity> findAttachmentByUser(String userId, int fileType);
	
	/**
	 * 查询条目附件数量
	 * @param noteId
	 * @return
	 */
	public int countAttachmentByNote(String noteId);
	
	/**
	 * 标识删除附件
	 * @param attachment
	 */
	public void markDelAttachment(AttachmentEntity attachment);
	
	/**
	 * 查询条目下附件ID
	 * @param noteId
	 * @param fileType
	 * @return
	 */
	public List<String> findAttaIdsByNote(String noteId, Integer fileType);
	
	/**
	 * 查询用户需上传的附件
	 * @param userId
	 * @param fileType
	 * @return
	 */
	public List<AttachmentEntity> findNeedUploadAttachmentByUser(String userId,	Integer fileType);
	
	/**
	 * 根据条目ID，文件名查询要上传的附件
	 * @param noteId
	 * @param fileName
	 * @return
	 */
	public AttachmentEntity findNeedUploadAttachmentByNote(String noteId, String fileName);
}
