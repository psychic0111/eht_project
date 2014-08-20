package com.eht.note.service.impl;

import java.io.Serializable;
import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.jeecgframework.core.common.service.impl.CommonServiceImpl;
import org.jeecgframework.core.util.StringUtil;
import org.jeecgframework.core.util.oConvertUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eht.common.annotation.RecordOperate;
import com.eht.common.constant.Constants;
import com.eht.common.enumeration.DataSynchAction;
import com.eht.common.enumeration.DataType;
import com.eht.common.util.UUIDGenerator;
import com.eht.note.entity.AttachmentEntity;
import com.eht.note.service.AttachmentServiceI;

@Service("attachmentService")
@Transactional
public class AttachmentServiceImpl extends CommonServiceImpl implements AttachmentServiceI {
	
	@Override
	public String uploadAttachment(AttachmentEntity attachment) {
		return null;
	}

	@Override
	public AttachmentEntity findAttachmentByMd5(String md5) {
		List<AttachmentEntity> list = findByProperty(AttachmentEntity.class, "md5", md5);
		if(list != null && !list.isEmpty()){
			for(AttachmentEntity atta : list){
				if(atta.getStatus() == Constants.FILE_TRANS_COMPLETED){
					return atta;
				}
			}
		}
		return null;
	}

	@Override
	@RecordOperate(dataClass=DataType.ATTACHMENT, action=DataSynchAction.ADD, keyIndex=0, keyMethod="getId", timeStamp="createTime")
	public String addAttachment(AttachmentEntity attachment) {
		if(StringUtil.isEmpty(attachment.getId())){
			attachment.setId(UUIDGenerator.uuid());
		}
		save(attachment);
		return attachment.getId();
	}

	@Override
	public void updateAttachment(AttachmentEntity attachment) {
		updateEntitie(attachment);
	}
	
	@Override
	@RecordOperate(dataClass=DataType.ATTACHMENT, action=DataSynchAction.DELETE, keyIndex=0, keyMethod="getId", timeStamp="updateTime")
	public void markDelAttachment(AttachmentEntity attachment) {
		attachment.setDeleted(Constants.DATA_DELETED);
		updateEntitie(attachment);
	}
	
	@Override
	public void deleteAttachment(AttachmentEntity attachment) {
		delete(attachment);
	}

	@Override
	public void deleteAttachment(Serializable id) {
		AttachmentEntity attachment = getAttachment(id);
		deleteAttachment(attachment);
	}

	public void deleteAttachment(String noteId){
		commonDao.executeHql("delete from AttachmentEntity where noteId = '"+noteId+"'");
	}
	
	@Override
	public AttachmentEntity getAttachment(Serializable id) {
		return get(AttachmentEntity.class, id);
	}

	@Override
	public List<AttachmentEntity> findAttachmentByNote(String noteId, Integer fileType) {
		DetachedCriteria dc = DetachedCriteria.forClass(AttachmentEntity.class);
		dc.add(Restrictions.eq("noteId", noteId));
		dc.add(Restrictions.eq("deleted", Constants.DATA_NOT_DELETED));
		dc.add(Restrictions.eq("status", Constants.FILE_TRANS_COMPLETED));
		if(fileType != null && fileType > 0){
			dc.add(Restrictions.eq("fileType", fileType));
		}
		List<AttachmentEntity> list = findByDetached(dc);
		return list;
	}
	
	@Override
	public List<AttachmentEntity> findAttachmentByNote(String noteId, Integer fileType,String searchType) {
		DetachedCriteria dc = DetachedCriteria.forClass(AttachmentEntity.class);
		dc.add(Restrictions.eq("noteId", noteId));
		dc.add(Restrictions.eq("deleted", Constants.DATA_NOT_DELETED));
		dc.add(Restrictions.eq("status", Constants.FILE_TRANS_COMPLETED));
		if(fileType != null && fileType > 0){
			dc.add(Restrictions.eq("fileType", fileType));
		}
		dc.addOrder(Order.desc("createTimeStamp"));
		List<AttachmentEntity> list = null;
		if(searchType!=null&&searchType.equals("current")){
			//取前几条
			list = pageList(dc,0,8);
		}else{
			//取后其它条 
			list = pageList(dc,8,1000);
		}
		return list;
	}
	
	@Override
	public AttachmentEntity findNeedUploadAttachmentByNote(String noteId, String fileName) {
		DetachedCriteria dc = DetachedCriteria.forClass(AttachmentEntity.class);
		dc.add(Restrictions.eq("fileName", fileName));
		dc.add(Restrictions.eq("noteId", noteId));
		dc.add(Restrictions.eq("deleted", Constants.DATA_NOT_DELETED));
		dc.add(Restrictions.eq("status", Constants.FILE_TRANS_NOT_COMPLETED));
		
		List<AttachmentEntity> list = findByDetached(dc);
		if(list != null && !list.isEmpty()){
			return list.get(0);
		}else{
			return null;
		}
	
	}
	
	@Override
	public List<String> findAttaIdsByNote(String noteId, Integer fileType) {
		String sql = "select id from AttachmentEntity where noteId=? and deleted="+Constants.DATA_NOT_DELETED+" and status=" + Constants.FILE_TRANS_COMPLETED;
		if(fileType != null && fileType > 0){
			sql += " and fileType=" + fileType;
		}
		List<String> list = findHql(sql, new Object[]{noteId});
		return list;
	}

	@Override
	public int countAttachmentByNote(String noteId) {
		DetachedCriteria dc = DetachedCriteria.forClass(AttachmentEntity.class);
		dc.add(Restrictions.eq("noteId", noteId));
		dc.add(Restrictions.eq("deleted", Constants.DATA_NOT_DELETED));
		dc.add(Restrictions.eq("status", Constants.FILE_TRANS_COMPLETED));
		dc.add(Restrictions.eq("fileType", Constants.FILE_TYPE_NORMAL));
		int count = oConvertUtils.getInt((dc.getExecutableCriteria(getSession())
				.setProjection(Projections.rowCount())).uniqueResult(), 0);
		return count;
	}

	@Override
	public List<AttachmentEntity> findAttachmentByDir(String subjectId) {
		DetachedCriteria dc = DetachedCriteria.forClass(AttachmentEntity.class);
		dc.add(Restrictions.eq("directoryId", subjectId));
		dc.add(Restrictions.eq("deleted", Constants.DATA_NOT_DELETED));
		dc.add(Restrictions.eq("status", Constants.FILE_TRANS_COMPLETED));
		List<AttachmentEntity> list = findByDetached(dc);
		return list;
	}
	@Override
	public List<AttachmentEntity> findAttachmentByFileName(String fileName,String noteid) {
		DetachedCriteria dc = DetachedCriteria.forClass(AttachmentEntity.class);
		dc.add(Restrictions.eq("fileName", fileName));
		dc.add(Restrictions.eq("noteId", noteid));
		dc.add(Restrictions.eq("deleted", Constants.DATA_NOT_DELETED));
		dc.add(Restrictions.eq("status", Constants.FILE_TRANS_COMPLETED));
		List<AttachmentEntity> list = findByDetached(dc);
		return list;
	}
	@Override
	public List<AttachmentEntity> findAttachmentsByDir(String subjectId, String directoryId,int firstResult,int maxResult) {
		DetachedCriteria dc = DetachedCriteria.forClass(AttachmentEntity.class, "a");
		dc.createCriteria("noteEntity", "n", JoinType.LEFT_OUTER_JOIN);
		Criterion criterion = Restrictions.and(Restrictions.isNotNull("a.noteId"), Restrictions.eq("n.subjectId", subjectId));
		Criterion dirCriterion = Restrictions.eq("a.directoryId", directoryId);
		dc.add(Restrictions.or(criterion, dirCriterion));
		
		dc.add(Restrictions.eq("a.deleted", Constants.DATA_NOT_DELETED));
		dc.add(Restrictions.eq("a.status", Constants.FILE_TRANS_COMPLETED));
		dc.add(Restrictions.eq("a.fileType", Constants.FILE_TYPE_NORMAL));
		/*ProjectionList pList = Projections.projectionList();
		pList.add(Projections.alias(Projections.property("a.id"),"id"));
		pList.add(Projections.property("a.fileName").as("fileName"));
		pList.add(Projections.property("a.suffix").as("suffix"));
		pList.add(Projections.property("a.noteId").as("noteId"));
		pList.add(Projections.property("a.filePath").as("filePath"));
		pList.add(Projections.property("a.createUser").as("createUser"));
		pList.add(Projections.property("a.createTime").as("createTime"));
		pList.add(Projections.property("a.directoryId").as("directoryId"));
		dc.setProjection(pList);
		dc.setResultTransformer(Transformers.aliasToBean(AttachmentEntity.class));*/
		
		//dc.addOrder(Order.desc("a.directoryId"));
		dc.addOrder(Order.desc("a.createTime"));
		//分页查询
		int firstRow = (firstResult - 1) * maxResult;
		List<AttachmentEntity> list = pageList(dc, firstRow, maxResult);
		return list;
	}
	@Override
	public Long findAttachmentsByDirCount(String subjectId, String directoryId) {
		DetachedCriteria dc = DetachedCriteria.forClass(AttachmentEntity.class);
		dc.createCriteria("noteEntity", "n", JoinType.LEFT_OUTER_JOIN);
		Criterion criterion = Restrictions.and(Restrictions.isNull("directoryId"), Restrictions.eq("n.subjectId", subjectId));
		Criterion dirCriterion = Restrictions.eq("directoryId", directoryId); 
		
		dc.add(Restrictions.or(criterion, dirCriterion));
		dc.add(Restrictions.eq("deleted", Constants.DATA_NOT_DELETED));
		dc.add(Restrictions.eq("status", Constants.FILE_TRANS_COMPLETED));
		dc.add(Restrictions.eq("fileType", Constants.FILE_TYPE_NORMAL));
		
		long count = 
				oConvertUtils.getInt((dc.getExecutableCriteria(getSession()).setProjection(Projections.rowCount())).uniqueResult(), 0);
		return count;
	}

	/**
	 * 查询用户附件
	 * @param userId
	 * @param fileType
	 * @return
	 */
	@Override
	public List<AttachmentEntity> findAttachmentByUser(String userId, int fileType) {
		DetachedCriteria dc = DetachedCriteria.forClass(AttachmentEntity.class);
		dc.add(Restrictions.eq("deleted", Constants.DATA_NOT_DELETED));
		dc.add(Restrictions.eq("status", Constants.FILE_TRANS_COMPLETED));
		dc.add(Restrictions.eq("fileType", fileType));
		dc.add(Restrictions.eq("createUser", userId));
		List<AttachmentEntity> list = findByDetached(dc);
		return list;
	} 
	
	/**
	 * 查询用户需上传的附件
	 * @param userId
	 * @param fileType
	 * @return
	 */
	@Override
	public List<AttachmentEntity> findNeedUploadAttachmentByUser(String userId, Integer fileType) {
		DetachedCriteria dc = DetachedCriteria.forClass(AttachmentEntity.class);
		dc.add(Restrictions.eq("deleted", Constants.DATA_NOT_DELETED));
		dc.add(Restrictions.eq("status", Constants.FILE_TRANS_NOT_COMPLETED));
		if(fileType != null){
			dc.add(Restrictions.eq("fileType", fileType.intValue()));
		}
		dc.add(Restrictions.eq("createUser", userId));
		dc.addOrder(Order.asc("fileType"));
		List<AttachmentEntity> list = findByDetached(dc);
		return list;
	}
}