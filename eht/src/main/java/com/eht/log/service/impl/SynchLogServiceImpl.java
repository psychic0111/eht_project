package com.eht.log.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.log4j.Logger;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jeecgframework.core.common.service.impl.CommonServiceImpl;
import org.jeecgframework.core.util.ContextHolderUtils;
import org.jeecgframework.core.util.StringUtil;
import org.jeecgframework.core.util.oConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eht.common.bean.ResponseStatus;
import com.eht.common.constant.Constants;
import com.eht.common.constant.RoleName;
import com.eht.common.constant.SynchConstants;
import com.eht.common.enumeration.DataSynchAction;
import com.eht.common.enumeration.DataType;
import com.eht.common.util.ReflectionUtils;
import com.eht.common.util.UUIDGenerator;
import com.eht.group.entity.Group;
import com.eht.group.service.GroupService;
import com.eht.log.entity.SynchLogEntity;
import com.eht.log.entity.SynchronizedLogEntity;
import com.eht.log.service.SynchLogServiceI;
import com.eht.message.entity.MessageEntity;
import com.eht.message.service.MessageServiceI;
import com.eht.note.entity.AttachmentEntity;
import com.eht.note.entity.NoteEntity;
import com.eht.note.entity.NoteTag;
import com.eht.note.service.AttachmentServiceI;
import com.eht.note.service.NoteServiceI;
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
import com.eht.user.entity.AccountEntity;
import com.eht.user.service.AccountServiceI;
import com.eht.webservice.util.DataSynchizeUtil;
import com.eht.webservice.util.SynchDataCache;

@Service("synchLogService")
@Transactional
public class SynchLogServiceImpl extends CommonServiceImpl implements
		SynchLogServiceI {

	private Logger logger = Logger.getLogger(SynchLogServiceImpl.class);

	@Autowired
	private SubjectServiceI subjectService;

	@Autowired
	private DirectoryServiceI directoryService;

	@Autowired
	private NoteServiceI noteService;

	@Autowired
	private TagServiceI tagService;

	@Autowired
	private RoleService roleService;

	@Autowired
	private AccountServiceI accountService;

	@Autowired
	private AttachmentServiceI attachmentService;

	@Autowired
	private GroupService groupService;

	@Autowired
	private DataInitService dataInitService;
	
	@Autowired
	private MessageServiceI messageService;

	@Override
	public SynchLogEntity getSynchLog(String logId) {
		return get(SynchLogEntity.class, logId);
	}

	@Override
	public String saveSynchLog(SynchLogEntity log) {
		logger.info("保存操作日志; 数据类型：" + log.getClassName() + ", 数据主键："
				+ log.getClassPK() + ", 数据操作：" + log.getAction());
		
		//针对系统在用户未登录状态下生成的日志
		if(StringUtil.isEmpty(log.getOperateUser())){
			log.setOperateUser(log.getTargetUser());
		}
		
		SynchLogEntity oldLog = findLogByData(log.getClassName(),
				log.getClassPK(), log.getTargetUser(), log.getAction());
		if (oldLog != null) {
			logger.info("发现已存在日志; 数据类型：" + oldLog.getClassName() + ", 数据主键："
					+ oldLog.getClassPK() + ", 数据操作：" + oldLog.getAction());
			if (oldLog.getOperateTime() > log.getOperateTime()) {
				logger.info("已有数据操作时间比此次操作日志时间新, 忽略此次日志记录!");
			} else {
				if(log.getClassName().equals(DataType.USER.toString()) && log.getAction().equals(DataSynchAction.ADD.toString())){
					//专题成员的用户信息在客户端只新增一次
				}else{
					oldLog.setOperateUser(log.getOperateUser());
					oldLog.setOperateTime(log.getOperateTime());
					oldLog.setSynchTime(System.currentTimeMillis());
					oldLog.setClientType(log.getClientType());
					oldLog.setClientId(log.getClientId());
					updateEntitie(oldLog);
				}
			}
		} else {
			save(log);
		}
		return new ResponseStatus().toString();
	}

	/**
	 * 为新加入专题的成员，生成同步日志
	 * 
	 * @param subjectId
	 * @param userId
	 * @param action
	 * @return
	 */
	@Override
	public void generateUserSubjectAddLog(String subjectId, String userId,
			String roleId) {
		Role role = roleService.getRole(roleId);
		// 生成专题日志
		long timestamp = System.currentTimeMillis();
		SynchLogEntity log = new SynchLogEntity();
		log.setId(UUIDGenerator.uuid());
		log.setAction(DataSynchAction.ADD.toString());
		log.setClassName(DataType.SUBJECT.toString());
		log.setClassPK(subjectId);
		log.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
		log.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
		log.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
		log.setOperateTime(timestamp);
		log.setOperateUser(userId);
		log.setSynchTime(timestamp);
		log.setTargetUser(userId);
		saveSynchLog(log);

		// 生成专题下目录日志
		List<DirectoryEntity> dirList = directoryService
				.findDirsBySubjectOderByTime(subjectId, true, true);
		for (int i = 0; i < dirList.size(); i++) {
			DirectoryEntity dir = dirList.get(i);
			if (dir.getDeleted().intValue() == Constants.DATA_DELETED) {
				if (role.getRoleName().equals(RoleName.READER)) {
					continue;
				}
			}
			timestamp++;
			SynchLogEntity dirLog = new SynchLogEntity();
			dirLog.setId(UUIDGenerator.uuid());
			dirLog.setAction(DataSynchAction.ADD.toString());
			dirLog.setClassName(DataType.DIRECTORY.toString());
			dirLog.setClassPK(dir.getId());
			dirLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
			dirLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
			dirLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
			dirLog.setOperateTime(timestamp);
			dirLog.setOperateUser(userId);
			dirLog.setSynchTime(timestamp);
			dirLog.setTargetUser(userId);
			saveSynchLog(dirLog);
		}

		// 生成标签日志
		List<TagEntity> tagList = tagService.findTagBySubject(subjectId);
		for (int i = 0; i < tagList.size(); i++) {
			TagEntity tag = tagList.get(i);
			timestamp++;
			SynchLogEntity synchLog = new SynchLogEntity();
			synchLog.setId(UUIDGenerator.uuid());
			synchLog.setAction(DataSynchAction.ADD.toString());
			synchLog.setClassName(DataType.TAG.toString());
			synchLog.setClassPK(tag.getId());
			synchLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
			synchLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
			synchLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
			synchLog.setOperateTime(timestamp);
			synchLog.setOperateUser(userId);
			synchLog.setSynchTime(timestamp);
			synchLog.setTargetUser(userId);
			saveSynchLog(synchLog);
		}

		// 生成条目日志
		List<NoteEntity> noteList = noteService.findNotesBySubject(subjectId,
				true);
		for (int i = 0; i < noteList.size(); i++) {
			NoteEntity note = noteList.get(i);
			if (note.getDeleted().intValue() == Constants.DATA_DELETED) {
				// 读者不需要同步回收站中的条目
				if (role.getRoleName().equals(RoleName.READER)) {
					continue;
				}
				// 作者只同步回收站中自己的条目
				if (role.getRoleName().equals(RoleName.AUTHOR)
						&& !note.getCreateUser().equals(userId)) {
					continue;
				}
			}

			timestamp++;
			SynchLogEntity synchLog = new SynchLogEntity();
			synchLog.setId(UUIDGenerator.uuid());
			synchLog.setAction(DataSynchAction.ADD.toString());
			synchLog.setClassName(DataType.NOTE.toString());
			synchLog.setClassPK(note.getId());
			synchLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
			synchLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
			synchLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
			synchLog.setOperateTime(timestamp);
			synchLog.setOperateUser(userId);
			synchLog.setSynchTime(timestamp);
			synchLog.setTargetUser(userId);
			saveSynchLog(synchLog);

			// 条目标签日志
			List<NoteTag> ntList = tagService.findNoteTagsByNote(note.getId());
			for (int j = 0; j < ntList.size(); j++) {
				NoteTag nt = ntList.get(j);
				timestamp++;
				SynchLogEntity nLog = new SynchLogEntity();
				nLog.setId(UUIDGenerator.uuid());
				nLog.setAction(DataSynchAction.ADD.toString());
				nLog.setClassName(DataType.NOTETAG.toString());
				nLog.setClassPK(nt.getId());
				nLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
				nLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
				nLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
				nLog.setOperateTime(timestamp);
				nLog.setOperateUser(userId);
				nLog.setSynchTime(timestamp);
				nLog.setTargetUser(userId);
				saveSynchLog(nLog);
			}

			List<AttachmentEntity> attaList = attachmentService
					.findAttachmentByNote(note.getId(), Constants.FILE_TRANS_COMPLETED, Constants.DATA_NOT_DELETED, new Integer[]{Constants.FILE_TYPE_NORMAL});
			for (int j = 0; j < attaList.size(); j++) {
				AttachmentEntity atta = attaList.get(j);
				timestamp++;
				SynchLogEntity nLog = new SynchLogEntity();
				nLog.setId(UUIDGenerator.uuid());
				nLog.setAction(DataSynchAction.ADD.toString());
				nLog.setClassName(DataType.ATTACHMENT.toString());
				nLog.setClassPK(atta.getId());
				nLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
				nLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
				nLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
				nLog.setOperateTime(timestamp);
				nLog.setOperateUser(userId);
				nLog.setSynchTime(timestamp);
				nLog.setTargetUser(userId);
				saveSynchLog(nLog);
			}
		}
	}

	/**
	 * 删除专题的成员，生成同步日志
	 * 
	 * @param subjectId
	 * @param userId
	 * @param action
	 * @return
	 */
	@Override
	public void generateUserSubjectDelLog(String subjectId, String userId) {
		// 生成专题日志
		long timestamp = System.currentTimeMillis();
		SynchLogEntity log = new SynchLogEntity();
		log.setId(UUIDGenerator.uuid());
		log.setAction(DataSynchAction.TRUNCATE.toString());
		log.setClassName(DataType.SUBJECT.toString());
		log.setClassPK(subjectId);
		log.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
		log.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
		log.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
		log.setOperateTime(timestamp);
		log.setOperateUser(userId);
		log.setSynchTime(timestamp);
		log.setTargetUser(userId);
		saveSynchLog(log);

		// 生成专题下目录日志
		List<DirectoryEntity> dirList = directoryService
				.findDirsBySubjectOderByTime(subjectId, true, true);
		for (int i = 0; i < dirList.size(); i++) {
			DirectoryEntity dir = dirList.get(i);
			timestamp--;
			SynchLogEntity dirLog = new SynchLogEntity();
			dirLog.setId(UUIDGenerator.uuid());
			dirLog.setAction(DataSynchAction.TRUNCATE.toString());
			dirLog.setClassName(DataType.DIRECTORY.toString());
			dirLog.setClassPK(dir.getId());
			dirLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
			dirLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
			dirLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
			dirLog.setOperateTime(timestamp);
			dirLog.setOperateUser(userId);
			dirLog.setSynchTime(timestamp);
			dirLog.setTargetUser(userId);
			saveSynchLog(dirLog);
		}

		// 生成标签日志
		List<TagEntity> tagList = tagService.findTagBySubject(subjectId);
		for (int i = 0; i < tagList.size(); i++) {
			TagEntity tag = tagList.get(i);
			timestamp--;
			SynchLogEntity synchLog = new SynchLogEntity();
			synchLog.setId(UUIDGenerator.uuid());
			synchLog.setAction(DataSynchAction.TRUNCATE.toString());
			synchLog.setClassName(DataType.TAG.toString());
			synchLog.setClassPK(tag.getId());
			synchLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
			synchLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
			synchLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
			synchLog.setOperateTime(timestamp);
			synchLog.setOperateUser(userId);
			synchLog.setSynchTime(timestamp);
			synchLog.setTargetUser(userId);
			saveSynchLog(synchLog);
		}

		// 生成条目日志
		List<NoteEntity> noteList = noteService.findNotesBySubject(subjectId,
				true);
		for (int i = 0; i < noteList.size(); i++) {
			NoteEntity note = noteList.get(i);

			timestamp--;
			SynchLogEntity synchLog = new SynchLogEntity();
			synchLog.setId(UUIDGenerator.uuid());
			synchLog.setAction(DataSynchAction.TRUNCATE.toString());
			synchLog.setClassName(DataType.NOTE.toString());
			synchLog.setClassPK(note.getId());
			synchLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
			synchLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
			synchLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
			synchLog.setOperateTime(timestamp);
			synchLog.setOperateUser(userId);
			synchLog.setSynchTime(timestamp);
			synchLog.setTargetUser(userId);
			saveSynchLog(synchLog);

			// 条目标签日志
			List<NoteTag> ntList = tagService.findNoteTagsByNote(note.getId());
			for (int j = 0; j < ntList.size(); j++) {
				NoteTag nt = ntList.get(j);
				timestamp--;
				SynchLogEntity nLog = new SynchLogEntity();
				nLog.setId(UUIDGenerator.uuid());
				nLog.setAction(DataSynchAction.ADD.toString());
				nLog.setClassName(DataType.NOTETAG.toString());
				nLog.setClassPK(nt.getId());
				nLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
				nLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
				nLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
				nLog.setOperateTime(timestamp);
				nLog.setOperateUser(userId);
				nLog.setSynchTime(timestamp);
				nLog.setTargetUser(userId);
				saveSynchLog(nLog);
			}

			// 附件日志
			List<AttachmentEntity> attaList = attachmentService
					.findAttachmentByNote(note.getId(), Constants.FILE_TRANS_COMPLETED, Constants.DATA_NOT_DELETED, new Integer[]{Constants.FILE_TYPE_NORMAL});
			for (int j = 0; j < attaList.size(); j++) {
				AttachmentEntity atta = attaList.get(j);
				timestamp--;
				SynchLogEntity nLog = new SynchLogEntity();
				nLog.setId(UUIDGenerator.uuid());
				nLog.setAction(DataSynchAction.TRUNCATE.toString());
				nLog.setClassName(DataType.ATTACHMENT.toString());
				nLog.setClassPK(atta.getId());
				nLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
				nLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
				nLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
				nLog.setOperateTime(timestamp);
				nLog.setOperateUser(userId);
				nLog.setSynchTime(timestamp);
				nLog.setTargetUser(userId);
				saveSynchLog(nLog);
			}
		}
	}

	@Override
	public void generateRecycleLogs(String subjectId, String userId,
			String roleId, String newRoleId) {
		Role role = roleService.getRole(roleId);
		Role newRole = roleService.getRole(newRoleId);

		// 原来是读者角色
		if (role.getRoleName().equals(RoleName.READER)) {
			generateRecycleLogsReader(subjectId, userId, newRole);
		}

		// 原来为作者
		if (role.getRoleName().equals(RoleName.AUTHOR)) {
			generateRecycleLogsAuthor(subjectId, userId, newRole);
		}

		// 原来为编辑
		if (role.getRoleName().equals(RoleName.EDITOR)) {
			if (!newRole.getRoleName().equals(RoleName.ADMIN)) { // 管理员和编辑的回收站数据是一样的
				generateRecycleLogsEditor(subjectId, userId, newRole);
			}
		}

		// 原来为管理员
		if (role.getRoleName().equals(RoleName.ADMIN)) {
			if (!newRole.getRoleName().equals(RoleName.ADMIN)) { // 管理员和编辑的回收站数据是一样的
				generateRecycleLogsAdmin(subjectId, userId, newRole);
			}
		}
	}

	/**
	 * 读者角色发生改变时生成同步日志
	 * 
	 * @param subjectId
	 * @param userId
	 * @param newRole
	 */
	private void generateRecycleLogsReader(String subjectId, String userId,
			Role newRole) {

		long timestamp = System.currentTimeMillis();

		// 回收站中目录日志
		List<DirectoryEntity> dirList = directoryService
				.findDeletedDirsBySubject(userId, subjectId, "createTime",
						true, Constants.SUBJECT_TYPE_M);
		for (DirectoryEntity dir : dirList) {
			timestamp++;
			//查询回收站中条目对于此用户是否TRUNCATE过（曾经没有回收站显示该条目的权限）
			/*SynchLogEntity synchLog = findLogByData(DataType.DIRECTORY.toString(),
					dir.getId(), userId, DataSynchAction.TRUNCATE.toString());
			if(synchLog != null){
				delete(synchLog);
			}*/
			
			SynchLogEntity dirLog = new SynchLogEntity();
			dirLog.setId(UUIDGenerator.uuid());
			dirLog.setAction(DataSynchAction.ADD.toString());
			dirLog.setClassName(DataType.DIRECTORY.toString());
			dirLog.setClassPK(dir.getId());
			dirLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
			dirLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
			dirLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
			dirLog.setOperateTime(timestamp);
			dirLog.setOperateUser(userId);
			dirLog.setSynchTime(timestamp);
			dirLog.setTargetUser(userId);
			saveSynchLog(dirLog);
		}

		List<NoteEntity> noteList = noteService
				.findNotesInRecycleBySubject(userId, subjectId, null,
						"createTime", Constants.SUBJECT_TYPE_M);
		for (NoteEntity note : noteList) {
			// 作者回收站只显示自己创建的条目
			if (newRole.getRoleName().equals(RoleName.AUTHOR)
					&& !note.getCreateUser().equals(userId)) {
				continue;
			}
			timestamp++;
			//查询回收站中条目对于此用户是否TRUNCATE过（曾经没有回收站显示该条目的权限）
			/*SynchLogEntity synchLog = findLogByData(DataType.NOTE.toString(),
					note.getId(), userId, DataSynchAction.TRUNCATE.toString());
			if(synchLog != null){
				delete(synchLog);
			}*/
			SynchLogEntity synchLog = new SynchLogEntity();
			synchLog.setId(UUIDGenerator.uuid());
			synchLog.setAction(DataSynchAction.ADD.toString());
			synchLog.setClassName(DataType.NOTE.toString());
			synchLog.setClassPK(note.getId());
			synchLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
			synchLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
			synchLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
			synchLog.setOperateTime(timestamp);
			synchLog.setOperateUser(userId);
			synchLog.setSynchTime(timestamp);
			synchLog.setTargetUser(userId);
			saveSynchLog(synchLog);

			// 条目标签日志
			List<NoteTag> ntList = tagService.findNoteTagsByNote(note.getId());
			for (int j = 0; j < ntList.size(); j++) {
				NoteTag nt = ntList.get(j);
				timestamp++;
				/*SynchLogEntity nLog = findLogByData(DataType.NOTETAG.toString(),
						nt.getId(), userId, DataSynchAction.TRUNCATE.toString());
				if(nLog != null){
					delete(nLog);
				}*/
				SynchLogEntity nLog = new SynchLogEntity();
				nLog.setId(UUIDGenerator.uuid());
				nLog.setAction(DataSynchAction.ADD.toString());
				nLog.setClassName(DataType.NOTETAG.toString());
				nLog.setClassPK(nt.getId());
				nLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
				nLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
				nLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
				nLog.setOperateTime(timestamp);
				nLog.setOperateUser(userId);
				nLog.setSynchTime(timestamp);
				nLog.setTargetUser(userId);
				saveSynchLog(nLog);
			}

			// 附件日志
			List<AttachmentEntity> attaList = attachmentService
					.findAttachmentByNote(note.getId(), null, null, new Integer[]{Constants.FILE_TYPE_NORMAL});
			for (int j = 0; j < attaList.size(); j++) {
				AttachmentEntity atta = attaList.get(j);
				timestamp++;
				/*SynchLogEntity nLog = findLogByData(DataType.ATTACHMENT.toString(),
						atta.getId(), userId, DataSynchAction.TRUNCATE.toString());
				if(nLog != null){
					delete(nLog);
				}*/
				SynchLogEntity nLog = new SynchLogEntity();
				nLog.setId(UUIDGenerator.uuid());
				nLog.setAction(DataSynchAction.ADD.toString());
				nLog.setClassName(DataType.ATTACHMENT.toString());
				nLog.setClassPK(atta.getId());
				nLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
				nLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
				nLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
				nLog.setOperateTime(timestamp);
				nLog.setOperateUser(userId);
				nLog.setSynchTime(timestamp);
				nLog.setTargetUser(userId);
				saveSynchLog(nLog);
			}
		}
	}

	/**
	 * 作者角色发生改变时生成同步日志
	 * 
	 * @param subjectId
	 * @param userId
	 * @param newRole
	 */
	private void generateRecycleLogsAuthor(String subjectId, String userId,
			Role newRole) {
		long timestamp = System.currentTimeMillis();
		String action = DataSynchAction.ADD.toString();
		String oldAction = DataSynchAction.TRUNCATE.toString();
		boolean asc = true;
		if (newRole.getRoleName().equals(RoleName.READER)) {
			action = DataSynchAction.TRUNCATE.toString();
			oldAction = DataSynchAction.DELETE.toString();
			asc = false;
		}

		// 回收站中目录日志
		List<DirectoryEntity> dirList = directoryService
				.findDeletedDirsBySubject(userId, subjectId, "createTime", asc,
						Constants.SUBJECT_TYPE_M);
		for (DirectoryEntity dir : dirList) {
			timestamp++;
			
			if(oldAction.equals(DataSynchAction.DELETE.toString())){
				SynchLogEntity synchLog = findLogByData(DataType.DIRECTORY.toString(),
						dir.getId(), userId, oldAction);
				if(synchLog != null){
					delete(synchLog);
				}
			}
			SynchLogEntity dirLog = new SynchLogEntity();
			dirLog.setId(UUIDGenerator.uuid());
			dirLog.setAction(action);
			dirLog.setClassName(DataType.DIRECTORY.toString());
			dirLog.setClassPK(dir.getId());
			dirLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
			dirLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
			dirLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
			dirLog.setOperateTime(timestamp);
			dirLog.setOperateUser(userId);
			dirLog.setSynchTime(timestamp);
			dirLog.setTargetUser(userId);
			saveSynchLog(dirLog);
		}

		List<NoteEntity> noteList = noteService
				.findNotesInRecycleBySubject(userId, subjectId, null,
						"createTime", Constants.SUBJECT_TYPE_M);
		for (NoteEntity note : noteList) {
			// 作者回收站只有自己创建的条目（客户端），所以删除的时候只针对自己的条目
			if (action.equals(DataSynchAction.TRUNCATE.toString())
					&& !note.getCreateUser().equals(userId)) {
				continue;
			}

			// 作者回收站有自己创建的条目（客户端），所以添加的时候跳过这些的条目
			if (action.equals(DataSynchAction.ADD.toString())
					&& note.getCreateUser().equals(userId)) {
				continue;
			}

			timestamp++;
			if(oldAction.equals(DataSynchAction.DELETE.toString())){
				SynchLogEntity synchLog = findLogByData(DataType.NOTE.toString(),
						note.getId(), userId, oldAction);
				if(synchLog != null){
					delete(synchLog);
				}
			}
			
			SynchLogEntity synchLog = new SynchLogEntity();
			synchLog.setId(UUIDGenerator.uuid());
			synchLog.setAction(action);
			synchLog.setClassName(DataType.NOTE.toString());
			synchLog.setClassPK(note.getId());
			synchLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
			synchLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
			synchLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
			synchLog.setOperateTime(timestamp);
			synchLog.setOperateUser(userId);
			synchLog.setSynchTime(timestamp);
			synchLog.setTargetUser(userId);
			saveSynchLog(synchLog);

			// 条目标签日志
			List<NoteTag> ntList = tagService.findNoteTagsByNote(note.getId());
			for (int j = 0; j < ntList.size(); j++) {
				NoteTag nt = ntList.get(j);
				timestamp++;
				/*if(action.equals(DataSynchAction.ADD.toString())){
					SynchLogEntity nLog = findLogByData(DataType.NOTETAG.toString(),
							nt.getId(), userId, oldAction);//truncate
					if(nLog != null){
						delete(nLog);
					}
				}*/
				SynchLogEntity nLog = new SynchLogEntity();
				nLog.setId(UUIDGenerator.uuid());
				nLog.setAction(action);
				nLog.setClassName(DataType.NOTETAG.toString());
				nLog.setClassPK(nt.getId());
				nLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
				nLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
				nLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
				nLog.setOperateTime(timestamp);
				nLog.setOperateUser(userId);
				nLog.setSynchTime(timestamp);
				nLog.setTargetUser(userId);
				saveSynchLog(nLog);
			}

			// 附件日志
			List<AttachmentEntity> attaList = attachmentService
					.findAttachmentByNote(note.getId(), null, null, new Integer[]{Constants.FILE_TYPE_NORMAL});
			for (int j = 0; j < attaList.size(); j++) {
				AttachmentEntity atta = attaList.get(j);
				timestamp++;
				/*if(action.equals(DataSynchAction.ADD.toString())){
					SynchLogEntity nLog = findLogByData(DataType.ATTACHMENT.toString(),
							atta.getId(), userId, oldAction);
					if(nLog != null){
						delete(nLog);
					}
				}*/
				SynchLogEntity nLog = new SynchLogEntity();
				nLog.setId(UUIDGenerator.uuid());
				nLog.setAction(action);
				nLog.setClassName(DataType.ATTACHMENT.toString());
				nLog.setClassPK(atta.getId());
				nLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
				nLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
				nLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
				nLog.setOperateTime(timestamp);
				nLog.setOperateUser(userId);
				nLog.setSynchTime(timestamp);
				nLog.setTargetUser(userId);
				saveSynchLog(nLog);
			}
		}
	}

	/**
	 * 编辑角色发生改变时生成同步日志
	 * 
	 * @param subjectId
	 * @param userId
	 * @param newRole
	 */
	private void generateRecycleLogsEditor(String subjectId, String userId,
			Role newRole) {
		long timestamp = System.currentTimeMillis();
		String action = DataSynchAction.TRUNCATE.toString();
		String oldAction = DataSynchAction.DELETE.toString();
		boolean asc = false;

		// 回收站中目录日志
		LinkedHashSet<String> dirList = new LinkedHashSet<String>();
		if (newRole.getRoleName().equals(RoleName.AUTHOR) || newRole.getRoleName().equals(RoleName.READER)){
			List<NoteEntity> noteList = noteService
					.findNotesInRecycleBySubject(userId, subjectId, null,
							"createTime", Constants.SUBJECT_TYPE_M);
			//生成非本人创建条目的删除日志
			for (NoteEntity note : noteList) {
				// 作者回收站有自己创建的条目（客户端），所以添加的时候跳过这些的条目
				if (note.getCreateUser().equals(userId)) {
					continue;
				}

				SynchLogEntity noteLog = findLogByData(DataType.NOTE.toString(),
						note.getId(), userId, oldAction);
				if(noteLog != null){
					delete(noteLog);
				}
				
				long synchTimestamp = timestamp;
				SynchLogEntity synchLog = new SynchLogEntity();
				synchLog.setId(UUIDGenerator.uuid());
				synchLog.setAction(action);
				synchLog.setClassName(DataType.NOTE.toString());
				synchLog.setClassPK(note.getId());
				synchLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
				synchLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
				synchLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
				synchLog.setOperateTime(synchTimestamp);
				synchLog.setOperateUser(userId);
				synchLog.setSynchTime(synchTimestamp);
				synchLog.setTargetUser(userId);
				saveSynchLog(synchLog);
				
				// 条目标签日志
				List<NoteTag> ntList = tagService.findNoteTagsByNote(note.getId());
				for (int j = 0; j < ntList.size(); j++) {
					NoteTag nt = ntList.get(j);
					synchTimestamp--;
					SynchLogEntity nLog = new SynchLogEntity();
					nLog.setId(UUIDGenerator.uuid());
					nLog.setAction(action);
					nLog.setClassName(DataType.NOTETAG.toString());
					nLog.setClassPK(nt.getId());
					nLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
					nLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
					nLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
					nLog.setOperateTime(synchTimestamp);
					nLog.setOperateUser(userId);
					nLog.setSynchTime(synchTimestamp);
					nLog.setTargetUser(userId);
					saveSynchLog(nLog);
				}

				// 附件日志
				List<AttachmentEntity> attaList = attachmentService
						.findAttachmentByNote(note.getId(), null, null, new Integer[]{Constants.FILE_TYPE_NORMAL});
				for (int j = 0; j < attaList.size(); j++) {
					AttachmentEntity atta = attaList.get(j);
					synchTimestamp--;
					SynchLogEntity nLog = new SynchLogEntity();
					nLog.setId(UUIDGenerator.uuid());
					nLog.setAction(action);
					nLog.setClassName(DataType.ATTACHMENT.toString());
					nLog.setClassPK(atta.getId());
					nLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
					nLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
					nLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
					nLog.setOperateTime(synchTimestamp);
					nLog.setOperateUser(userId);
					nLog.setSynchTime(synchTimestamp);
					nLog.setTargetUser(userId);
					saveSynchLog(nLog);
				}
				
				if(!StringUtil.isEmpty(note.getDirId())){
					List<String> list = new ArrayList<String>();
					directoryService.findUpDirs(note.getDirId(), list);
					dirList.addAll(list);
				}
			}
			
			for (String dirId : dirList) {
				SynchLogEntity nLog = findLogByData(DataType.DIRECTORY.toString(),
						dirId, userId, oldAction);
				if(nLog != null){
					delete(nLog);
				}
				
				timestamp++;
				SynchLogEntity dirLog = new SynchLogEntity();
				dirLog.setId(UUIDGenerator.uuid());
				dirLog.setAction(action);
				dirLog.setClassName(DataType.DIRECTORY.toString());
				dirLog.setClassPK(dirId);
				dirLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
				dirLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
				dirLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
				dirLog.setOperateTime(timestamp);
				dirLog.setOperateUser(userId);
				dirLog.setSynchTime(timestamp);
				dirLog.setTargetUser(userId);
				saveSynchLog(dirLog);
			}
		}
		
		/*List<DirectoryEntity> dirList = directoryService
				.findDeletedDirsBySubject(userId, subjectId, "createTime", asc,
						Constants.SUBJECT_TYPE_M);
		for (DirectoryEntity dir : dirList) {
			timestamp++;
			SynchLogEntity dirLog = new SynchLogEntity();
			dirLog.setId(UUIDGenerator.uuid());
			dirLog.setAction(action);
			dirLog.setClassName(DataType.DIRECTORY.toString());
			dirLog.setClassPK(dir.getId());
			dirLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
			dirLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
			dirLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
			dirLog.setOperateTime(timestamp);
			dirLog.setOperateUser(userId);
			dirLog.setSynchTime(timestamp);
			dirLog.setTargetUser(userId);
			saveSynchLog(dirLog);
		}

		List<NoteEntity> noteList = noteService
				.findNotesInRecycleBySubject(userId, subjectId, null,
						"createTime", Constants.SUBJECT_TYPE_M);
		for (NoteEntity note : noteList) {
			// 作者回收站有自己创建的条目（客户端），所以添加的时候跳过这些的条目
			if (newRole.getRoleName().equals(RoleName.AUTHOR)
					&& note.getCreateUser().equals(userId)) {
				continue;
			}

			timestamp++;
			SynchLogEntity synchLog = new SynchLogEntity();
			synchLog.setId(UUIDGenerator.uuid());
			synchLog.setAction(action);
			synchLog.setClassName(DataType.NOTE.toString());
			synchLog.setClassPK(note.getId());
			synchLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
			synchLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
			synchLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
			synchLog.setOperateTime(timestamp);
			synchLog.setOperateUser(userId);
			synchLog.setSynchTime(timestamp);
			synchLog.setTargetUser(userId);
			saveSynchLog(synchLog);

			// 条目标签日志
			List<NoteTag> ntList = tagService.findNoteTagsByNote(note.getId());
			for (int j = 0; j < ntList.size(); j++) {
				NoteTag nt = ntList.get(j);
				timestamp++;
				SynchLogEntity nLog = new SynchLogEntity();
				nLog.setId(UUIDGenerator.uuid());
				nLog.setAction(DataSynchAction.ADD.toString());
				nLog.setClassName(DataType.NOTETAG.toString());
				nLog.setClassPK(nt.getId());
				nLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
				nLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
				nLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
				nLog.setOperateTime(timestamp);
				nLog.setOperateUser(userId);
				nLog.setSynchTime(timestamp);
				nLog.setTargetUser(userId);
				saveSynchLog(nLog);
			}

			// 附件日志
			List<AttachmentEntity> attaList = attachmentService
					.findAttachmentByNote(note.getId(), null, null, new Integer[]{Constants.FILE_TYPE_NORMAL});
			for (int j = 0; j < attaList.size(); j++) {
				AttachmentEntity atta = attaList.get(j);
				timestamp++;
				SynchLogEntity nLog = new SynchLogEntity();
				nLog.setId(UUIDGenerator.uuid());
				nLog.setAction(DataSynchAction.TRUNCATE.toString());
				nLog.setClassName(DataType.ATTACHMENT.toString());
				nLog.setClassPK(atta.getId());
				nLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
				nLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
				nLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
				nLog.setOperateTime(timestamp);
				nLog.setOperateUser(userId);
				nLog.setSynchTime(timestamp);
				nLog.setTargetUser(userId);
				saveSynchLog(nLog);
			}
		}*/
	}

	/**
	 * 管理员角色发生改变时生成同步日志
	 * 
	 * @param subjectId
	 * @param userId
	 * @param newRole
	 */
	private void generateRecycleLogsAdmin(String subjectId, String userId,
			Role newRole) {
		long timestamp = System.currentTimeMillis();
		String action = DataSynchAction.TRUNCATE.toString();
		String oldAction = DataSynchAction.DELETE.toString();
		boolean asc = false;

		// 回收站中目录日志
		LinkedHashSet<String> dirList = new LinkedHashSet<String>();
		if (newRole.getRoleName().equals(RoleName.AUTHOR) || newRole.getRoleName().equals(RoleName.READER)){
			List<NoteEntity> noteList = noteService
					.findNotesInRecycleBySubject(userId, subjectId, null,
							"createTime", Constants.SUBJECT_TYPE_M);
			//生成非本人创建条目的删除日志
			for (NoteEntity note : noteList) {
				// 作者回收站有自己创建的条目（客户端），所以添加的时候跳过这些的条目
				if (note.getCreateUser().equals(userId)) {
					continue;
				}

				SynchLogEntity noteLog = findLogByData(DataType.NOTE.toString(),
						note.getId(), userId, oldAction);
				if(noteLog != null){
					delete(noteLog);
				}
				
				long synchTimestamp = timestamp;
				SynchLogEntity synchLog = new SynchLogEntity();
				synchLog.setId(UUIDGenerator.uuid());
				synchLog.setAction(action);
				synchLog.setClassName(DataType.NOTE.toString());
				synchLog.setClassPK(note.getId());
				synchLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
				synchLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
				synchLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
				synchLog.setOperateTime(synchTimestamp);
				synchLog.setOperateUser(userId);
				synchLog.setSynchTime(synchTimestamp);
				synchLog.setTargetUser(userId);
				saveSynchLog(synchLog);
				
				// 条目标签日志
				List<NoteTag> ntList = tagService.findNoteTagsByNote(note.getId());
				for (int j = 0; j < ntList.size(); j++) {
					NoteTag nt = ntList.get(j);
					synchTimestamp--;
					SynchLogEntity nLog = new SynchLogEntity();
					nLog.setId(UUIDGenerator.uuid());
					nLog.setAction(action);
					nLog.setClassName(DataType.NOTETAG.toString());
					nLog.setClassPK(nt.getId());
					nLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
					nLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
					nLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
					nLog.setOperateTime(synchTimestamp);
					nLog.setOperateUser(userId);
					nLog.setSynchTime(synchTimestamp);
					nLog.setTargetUser(userId);
					saveSynchLog(nLog);
				}

				// 附件日志
				List<AttachmentEntity> attaList = attachmentService
						.findAttachmentByNote(note.getId(), null, null, new Integer[]{Constants.FILE_TYPE_NORMAL});
				for (int j = 0; j < attaList.size(); j++) {
					AttachmentEntity atta = attaList.get(j);
					synchTimestamp--;
					SynchLogEntity nLog = new SynchLogEntity();
					nLog.setId(UUIDGenerator.uuid());
					nLog.setAction(action);
					nLog.setClassName(DataType.ATTACHMENT.toString());
					nLog.setClassPK(atta.getId());
					nLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
					nLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
					nLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
					nLog.setOperateTime(synchTimestamp);
					nLog.setOperateUser(userId);
					nLog.setSynchTime(synchTimestamp);
					nLog.setTargetUser(userId);
					saveSynchLog(nLog);
				}
				
				if(!StringUtil.isEmpty(note.getDirId())){
					List<String> list = new ArrayList<String>();
					directoryService.findUpDirs(note.getDirId(), list);
					dirList.addAll(list);
				}
			}
			
			for (String dirId : dirList) {
				SynchLogEntity nLog = findLogByData(DataType.DIRECTORY.toString(),
						dirId, userId, oldAction);
				if(nLog != null){
					delete(nLog);
				}
				
				timestamp++;
				SynchLogEntity dirLog = new SynchLogEntity();
				dirLog.setId(UUIDGenerator.uuid());
				dirLog.setAction(action);
				dirLog.setClassName(DataType.DIRECTORY.toString());
				dirLog.setClassPK(dirId);
				dirLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
				dirLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
				dirLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
				dirLog.setOperateTime(timestamp);
				dirLog.setOperateUser(userId);
				dirLog.setSynchTime(timestamp);
				dirLog.setTargetUser(userId);
				saveSynchLog(dirLog);
			}
		}
		
		/*List<DirectoryEntity> dirList = directoryService
				.findDeletedDirsBySubject(userId, subjectId, "createTime", asc,
						Constants.SUBJECT_TYPE_M);
		for (DirectoryEntity dir : dirList) {
			timestamp++;
			SynchLogEntity dirLog = new SynchLogEntity();
			dirLog.setId(UUIDGenerator.uuid());
			dirLog.setAction(action);
			dirLog.setClassName(DataType.DIRECTORY.toString());
			dirLog.setClassPK(dir.getId());
			dirLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
			dirLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
			dirLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
			dirLog.setOperateTime(timestamp);
			dirLog.setOperateUser(userId);
			dirLog.setSynchTime(timestamp);
			dirLog.setTargetUser(userId);
			saveSynchLog(dirLog);
		}

		List<NoteEntity> noteList = noteService
				.findNotesInRecycleBySubject(userId, subjectId, null,
						"createTime", Constants.SUBJECT_TYPE_M);
		for (NoteEntity note : noteList) {
			// 作者回收站有自己创建的条目（客户端），所以添加的时候跳过这些的条目
			if (newRole.getRoleName().equals(RoleName.AUTHOR)
					&& note.getCreateUser().equals(userId)) {
				continue;
			}

			timestamp++;
			SynchLogEntity synchLog = new SynchLogEntity();
			synchLog.setId(UUIDGenerator.uuid());
			synchLog.setAction(action);
			synchLog.setClassName(DataType.NOTE.toString());
			synchLog.setClassPK(note.getId());
			synchLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
			synchLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
			synchLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
			synchLog.setOperateTime(timestamp);
			synchLog.setOperateUser(userId);
			synchLog.setSynchTime(timestamp);
			synchLog.setTargetUser(userId);
			saveSynchLog(synchLog);

			// 条目标签日志
			List<NoteTag> ntList = tagService.findNoteTagsByNote(note.getId());
			for (int j = 0; j < ntList.size(); j++) {
				NoteTag nt = ntList.get(j);
				timestamp++;
				SynchLogEntity nLog = new SynchLogEntity();
				nLog.setId(UUIDGenerator.uuid());
				nLog.setAction(DataSynchAction.ADD.toString());
				nLog.setClassName(DataType.NOTETAG.toString());
				nLog.setClassPK(nt.getId());
				nLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
				nLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
				nLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
				nLog.setOperateTime(timestamp);
				nLog.setOperateUser(userId);
				nLog.setSynchTime(timestamp);
				nLog.setTargetUser(userId);
				saveSynchLog(nLog);
			}

			// 附件日志
			List<AttachmentEntity> attaList = attachmentService
					.findAttachmentByNote(note.getId(), null, null, new Integer[]{Constants.FILE_TYPE_NORMAL});
			for (int j = 0; j < attaList.size(); j++) {
				AttachmentEntity atta = attaList.get(j);
				timestamp++;
				SynchLogEntity nLog = new SynchLogEntity();
				nLog.setId(UUIDGenerator.uuid());
				nLog.setAction(DataSynchAction.TRUNCATE.toString());
				nLog.setClassName(DataType.ATTACHMENT.toString());
				nLog.setClassPK(atta.getId());
				nLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
				nLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
				nLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
				nLog.setOperateTime(timestamp);
				nLog.setOperateUser(userId);
				nLog.setSynchTime(timestamp);
				nLog.setTargetUser(userId);
				saveSynchLog(nLog);
			}
		}*/
	}

	@Override
	public void generateAddSubjectUserLogs(String id, String subjectId,
			String userId, String roleId, String action, String creator,
			long createTimestamp) {
		long timestamp = System.currentTimeMillis();
		SubjectEntity subject = subjectService.getSubject(subjectId);
		if (subject != null
				&& subject.getSubjectType().intValue() == Constants.SUBJECT_TYPE_M) {
			List<RoleUser> ruList = roleService.findSubjectUsers(subjectId);
			for (RoleUser ru : ruList) {
				if (!ru.getUserId().equals(userId)) {
					// 为专题中其他成员生成添加此成员的日志
					SynchLogEntity synchLog = new SynchLogEntity();
					synchLog.setId(UUIDGenerator.uuid());
					synchLog.setAction(action);
					synchLog.setClassName(DataType.SUBJECTUSER.toString());
					synchLog.setClassPK(id);
					synchLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
					synchLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
					synchLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
					synchLog.setOperateTime(createTimestamp);
					synchLog.setOperateUser(creator);
					synchLog.setSynchTime(timestamp);
					synchLog.setTargetUser(ru.getUserId());
					saveSynchLog(synchLog);
					
					// 为专题中其他成员生成添加此成员详细信息的日志
					SynchLogEntity userLog = new SynchLogEntity();
					userLog.setId(UUIDGenerator.uuid());
					userLog.setAction(action);
					userLog.setClassName(DataType.USER.toString());
					userLog.setClassPK(userId);
					userLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
					userLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
					userLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
					userLog.setOperateTime(createTimestamp);
					userLog.setOperateUser(creator);
					userLog.setSynchTime(timestamp - 1);
					userLog.setTargetUser(ru.getUserId());
					saveSynchLog(userLog);

					// 为新增的成员生成添加其他成员的日志
					SynchLogEntity log = new SynchLogEntity();
					log.setId(UUIDGenerator.uuid());
					log.setAction(action);
					log.setClassName(DataType.SUBJECTUSER.toString());
					log.setClassPK(ru.getId());
					log.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
					log.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
					log.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
					log.setOperateTime(ru.getCreateTimeStamp());
					log.setOperateUser(ru.getCreateUserId());
					log.setSynchTime(timestamp);
					log.setTargetUser(userId);
					saveSynchLog(log);
					
					// 为新增的成员生成添加其他成员用户信息的日志
					SynchLogEntity memLog = new SynchLogEntity();
					memLog.setId(UUIDGenerator.uuid());
					memLog.setAction(action);
					memLog.setClassName(DataType.USER.toString());
					memLog.setClassPK(ru.getUserId());
					memLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
					memLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
					memLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
					memLog.setOperateTime(ru.getCreateTimeStamp());
					memLog.setOperateUser(ru.getCreateUserId());
					memLog.setSynchTime(timestamp - 1);
					memLog.setTargetUser(userId);
					saveSynchLog(memLog);

				}else{
					// 为新增的成员生成添加自身日志
					SynchLogEntity log = new SynchLogEntity();
					log.setId(UUIDGenerator.uuid());
					log.setAction(action);
					log.setClassName(DataType.SUBJECTUSER.toString());
					log.setClassPK(ru.getId());
					log.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
					log.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
					log.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
					log.setOperateTime(ru.getCreateTimeStamp());
					log.setOperateUser(ru.getCreateUserId());
					log.setSynchTime(timestamp);
					log.setTargetUser(userId);
					saveSynchLog(log);
				
					// 为新增的成员生成添加自身信息日志
					SynchLogEntity userLog = new SynchLogEntity();
					userLog.setId(UUIDGenerator.uuid());
					userLog.setAction(action);
					userLog.setClassName(DataType.USER.toString());
					userLog.setClassPK(userId);
					userLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
					userLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
					userLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
					userLog.setOperateTime(createTimestamp);
					userLog.setOperateUser(creator);
					userLog.setSynchTime(timestamp - 1);
					userLog.setTargetUser(userId);
					saveSynchLog(userLog);
				}
			}
		}
	}
	
	@Override
	public void recordLog(Object paramEntity, String dataClass, String action, String targetUser, long synchTime) {
		AccountEntity user = accountService.getUser4Session();
		if (user == null) {
			String sessionId = String.valueOf(ContextHolderUtils.getRequest()
					.getAttribute("jsessionid"));
			user = accountService.getUser4Session(sessionId);
		}
		String userId = user == null ? null : user.getId();

		ClientEntity client = null;
		String clientId = user == null ? null : user.getClientId();
		if (!StringUtil.isEmpty(clientId)) {
			client = dataInitService.getClient(clientId);
		}
		if (client == null) {
			client = new ClientEntity();
			client.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
			client.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
		}

		SynchLogEntity log = new SynchLogEntity();
		log.setId(UUIDGenerator.uuid());
		log.setClassName(dataClass);
		log.setAction(action);
		if (log.getClassName().equals(DataType.SUBJECTUSER.toString())) {
			if (paramEntity.getClass().getName()
					.equals(RoleUser.class.getName())) {
				RoleUser ru = (RoleUser) paramEntity;
				userId = ru.getCreateUserId();
				log.setOperateUser(userId);
			} else {
				log.setOperateUser(userId);
			}

		} else {
			log.setOperateUser(userId);
		}
		log.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
		log.setClientType(client.getClientType());
		log.setClientId(client.getClientId());

		// 数据操作发生的时间
		Object dateObj = null;
		try {
			dateObj = ReflectionUtils.invokeGetterMethod(paramEntity,
					"createTime");
		} catch (Exception e1) {
			dateObj = ReflectionUtils.invokeGetterMethod(paramEntity,
					"createTimeStamp");
		}
		if (dateObj != null) {
			if (dateObj instanceof Date) {
				Date date = (Date) dateObj;
				log.setOperateTime(date.getTime());
			} else {
				log.setOperateTime(Long.parseLong(dateObj.toString()));
			}
		} else {
			log.setOperateTime(synchTime);
		}
		// 同步时间
		log.setSynchTime(synchTime);

		String methodName = "getId";
		String primaryKey = null;
		Object pk = ReflectionUtils.invokeMethod(paramEntity, methodName, null,
				null);
		// yuhao 修改
		if (pk instanceof java.lang.String) {
			primaryKey = (String) pk;
		} else {
			primaryKey = pk + "";
		}

		log.setClassPK(primaryKey);

		// 根据注解设置影响用户
		if (!StringUtil.isEmpty(targetUser)) {
			log.setTargetUser(targetUser);
			saveSynchLog(log);
		} else {
			// 是否需要查询操作影响用户，多人专题下的数据变更
			Map<String, String> map = isOwnShareSubject(paramEntity);
			if (map != null) {
				// 查询多人专题下所有成员
				List<String> userIdList = getTargetUsers(map.get("subjectId"),
						map.get("directoryId"), map.get("noteId"));

				for (String uid : userIdList) {
					SynchLogEntity newLog = new SynchLogEntity();
					try {
						BeanUtils.copyProperties(newLog, log);
						newLog.setId(UUIDGenerator.uuid());
						newLog.setTargetUser(uid);
					} catch (Exception e) {
						e.printStackTrace();
					}
					saveSynchLog(newLog);

					// 条目操作发送系统消息给其他成员
					if (!uid.equals(log.getOperateUser())) {
						if (log.getClassName().equals(DataType.NOTE.toString())) {
							MessageEntity msg = new MessageEntity();
							msg.setId(UUIDGenerator.uuid());

							NoteEntity note = (NoteEntity) paramEntity;
							String content = msgContent(log.getAction(),
									user.getUserName(), note);
							msg.setContent(content);
							msg.setClassName(SynchDataCache.getDataClass(
									log.getClassName()).getName());
							msg.setClassPk(log.getClassPK());
							msg.setOperate(log.getAction());

							Date date = new Date();
							msg.setCreateTime(date);
							msg.setCreateTimeStamp(date.getTime());
							msg.setCreateUser(null);
							msg.setMsgType(Constants.MSG_SYSTEM_TYPE);
							msg.setUserIsRead(Constants.NOT_READ_OBJECT);
							messageService.saveMessages(msg, uid);
						}
					}
				}
			} else {
				// 只影响操作者本身
				if(userId == null){
					String meth = "getUpdateUser";
					if(action.equals(DataSynchAction.ADD.toString())){
						meth = "getCreateUser";
					}
					Object uid = ReflectionUtils.invokeMethod(paramEntity, meth, null,
							null);
					userId = uid == null ? null : uid.toString();
				}
				log.setTargetUser(userId);
				saveSynchLog(log);
			}
		}

	}
	
	private String msgContent(String action, String userName, NoteEntity note){
		String operate = "新增";
		if(action.equals(DataSynchAction.UPDATE.toString())){
			operate = "修改";
		}
		if(action.equals(DataSynchAction.DELETE.toString())){
			operate = "删除";
		}
		
		StringBuilder sb = new StringBuilder(userName);
		sb.append(operate).append("条目【");
		
		SubjectEntity sub = subjectService.getSubject(note.getSubjectId());
		sb.append(sub.getSubjectName()).append("/");
		
		if(!StringUtil.isEmptyOrBlank(note.getDirId())){
			DirectoryEntity dir = directoryService.getDirectory(note.getDirId());
			String dirPath = dir.getDirName();
			while(!StringUtil.isEmpty(dir.getParentId())){
				dir = directoryService.getDirectory(dir.getParentId());
				dirPath = dir.getDirName() + "/" + dirPath; 
			}
			sb.append(dirPath);
		}
		sb.append("】：");
		sb.append(note.getTitle());
		
		return sb.toString();
	}
	
	/**
	 * 是否为多人专题下数据
	 * @param paramEntity
	 * @return map：subjectId  directoryId noteId
	 */
	private Map<String, String> isOwnShareSubject(Object paramEntity){
		String subjectId = null;
		String directoryId = null;
		String noteId = null;
		if (paramEntity.getClass().getName().equals(String.class.getName())) {
			subjectId = paramEntity.toString();
		}
		// 专题数据
		if (paramEntity.getClass().getName().equals(SubjectEntity.class.getName())) {
			SubjectEntity sub = (SubjectEntity) paramEntity;
			subjectId = sub.getId();
		}
		// 标签数据
		if (paramEntity.getClass().getName().equals(TagEntity.class.getName())) {
			TagEntity tag = (TagEntity) paramEntity;
			subjectId = tag.getSubjectId();
		}
		//目录
		if (paramEntity.getClass().getName().equals(DirectoryEntity.class.getName())) {
			DirectoryEntity dir = (DirectoryEntity) paramEntity;
			directoryId = dir.getId();
			subjectId = dir.getSubjectId();
		}
		//条目
		if (paramEntity.getClass().getName().equals(NoteEntity.class.getName())) {
			NoteEntity note = (NoteEntity) paramEntity;
			noteId = note.getId();
			subjectId = note.getSubjectId();
		}
		//条目标签关系
		if (paramEntity.getClass().getName().equals(NoteTag.class.getName())) {
			NoteTag noteTag = (NoteTag) paramEntity;
			NoteEntity note = noteService.getNote(noteTag.getNoteId());
			subjectId = note.getSubjectId();
		}
		//专题成员关系
		if (paramEntity.getClass().getName().equals(RoleUser.class.getName())) {
			RoleUser ru = (RoleUser) paramEntity;
			subjectId = ru.getSubjectId();
		}
		//附件
		if (paramEntity.getClass().getName().equals(AttachmentEntity.class.getName())) {
			AttachmentEntity atta = (AttachmentEntity) paramEntity;
			String nid = atta.getNoteId();
			if (!StringUtil.isEmpty(nid)) {
				NoteEntity note = noteService.getNote(nid);
				noteId = note.getId();
				subjectId = note.getSubjectId();
			} else {
				String dirId = atta.getDirectoryId();
				if (!StringUtil.isEmpty(dirId)) {
					DirectoryEntity dir = directoryService.getDirectory(dirId);
					subjectId = dir.getSubjectId();
				}
			}
		}
		if(!StringUtil.isEmpty(subjectId)){
			SubjectEntity subject = subjectService.getSubject(subjectId);
			if (subject.getSubjectType() == Constants.SUBJECT_TYPE_M) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("subjectId", subjectId);
				map.put("directoryId", directoryId);
				map.put("noteId", noteId);
				return map;
			}
		}
		return null;
	}
	
	public List<String> getTargetUsers(String subjectId, String directoryId, String noteId) {
		List<String> userIdList = new ArrayList<String>();

		List<RoleUser> userList = roleService.findSubjectUsers(subjectId);
		for (RoleUser ru : userList) {
			if (directoryId != null) {
				if (!directoryService.inDirBlackList(ru.getUserId(), directoryId)) {
					userIdList.add(ru.getUserId());
				}
			}

			if (noteId != null) {
				if (!noteService.inNoteBlackList(ru.getUserId(), noteId)) {
					userIdList.add(ru.getUserId());
				}
			}
			if(!userIdList.contains(ru.getUserId())){
				userIdList.add(ru.getUserId());
			}
		}
		return userIdList;
	}
	
	@Override
	public void generateDelSubjectUserLogs(String id, String subjectId,
			String userId, String action) {
		long timestamp = System.currentTimeMillis();
		AccountEntity user = accountService.getUser4Session();
		SubjectEntity subject = subjectService.getSubject(subjectId);
		if (subject != null
				&& subject.getSubjectType().intValue() == Constants.SUBJECT_TYPE_M) {
			List<RoleUser> ruList = roleService.findSubjectUsers(subjectId);
			for (RoleUser ru : ruList) {
				if (!ru.getUserId().equals(userId)) {
					// 为专题中其他成员生成删除此成员的日志
					SynchLogEntity synchLog = new SynchLogEntity();
					synchLog.setId(UUIDGenerator.uuid());
					synchLog.setAction(action);
					synchLog.setClassName(DataType.SUBJECTUSER.toString());
					synchLog.setClassPK(id);
					synchLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
					synchLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
					synchLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
					synchLog.setOperateTime(timestamp);
					synchLog.setOperateUser(user.getId());
					synchLog.setSynchTime(timestamp - 1);
					synchLog.setTargetUser(ru.getUserId());
					saveSynchLog(synchLog);
					
					// 为专题中其他成员生成删除此用户信息的日志
					SynchLogEntity userLog = new SynchLogEntity();
					userLog.setId(UUIDGenerator.uuid());
					userLog.setAction(action);
					userLog.setClassName(DataType.USER.toString());
					userLog.setClassPK(userId);
					userLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
					userLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
					userLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
					userLog.setOperateTime(timestamp);
					userLog.setOperateUser(user.getId());
					userLog.setSynchTime(timestamp);
					userLog.setTargetUser(ru.getUserId());
					saveSynchLog(userLog);

					// 为删除的成员生成删除其他成员的日志，需要把这部分数据在客户端删除
					SynchLogEntity log = new SynchLogEntity();
					log.setId(UUIDGenerator.uuid());
					log.setAction(action);
					log.setClassName(DataType.SUBJECTUSER.toString());
					log.setClassPK(ru.getId());
					log.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
					log.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
					log.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
					log.setOperateTime(timestamp);
					log.setOperateUser(user.getId());
					log.setSynchTime(timestamp - 1);
					log.setTargetUser(userId);
					saveSynchLog(log);
					
					// 为删除的成员生成删除其他成员用户信息的日志，需要把这部分数据在客户端删除
					SynchLogEntity oLog = new SynchLogEntity();
					oLog.setId(UUIDGenerator.uuid());
					oLog.setAction(action);
					oLog.setClassName(DataType.USER.toString());
					oLog.setClassPK(ru.getUserId());
					oLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
					oLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
					oLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
					oLog.setOperateTime(timestamp);
					oLog.setOperateUser(user.getId());
					oLog.setSynchTime(timestamp);
					oLog.setTargetUser(userId);
					saveSynchLog(oLog);
				}else{
					// 为删除的成员生成删除自身的日志
					SynchLogEntity log = new SynchLogEntity();
					log.setId(UUIDGenerator.uuid());
					log.setAction(action);
					log.setClassName(DataType.SUBJECTUSER.toString());
					log.setClassPK(ru.getId());
					log.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
					log.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
					log.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
					log.setOperateTime(timestamp);
					log.setOperateUser(user.getId());
					log.setSynchTime(timestamp - 1);
					log.setTargetUser(userId);
					saveSynchLog(log);
					
					SynchLogEntity userLog = new SynchLogEntity();
					userLog.setId(UUIDGenerator.uuid());
					userLog.setAction(action);
					userLog.setClassName(DataType.USER.toString());
					userLog.setClassPK(userId);
					userLog.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
					userLog.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
					userLog.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
					userLog.setOperateTime(timestamp);
					userLog.setOperateUser(user.getId());
					userLog.setSynchTime(timestamp);
					userLog.setTargetUser(userId);
					saveSynchLog(userLog);
				}
			}
		}
	}

	@Override
	public List<SynchLogEntity> findLogByData(String dataClass, String dataKey) {
		DetachedCriteria dc = DetachedCriteria.forClass(SynchLogEntity.class);
		dc.add(Restrictions.eq("className", dataClass));
		dc.add(Restrictions.eq("classPK", dataKey));
		dc.addOrder(Order.asc("operateTime"));
		List<SynchLogEntity> list = findByDetached(dc);
		return list;
	}

	@Override
	public SynchLogEntity findLogByData(String dataClass, String dataKey,
			String userId, String action) {
		DetachedCriteria dc = DetachedCriteria.forClass(SynchLogEntity.class);
		dc.add(Restrictions.eq("className", dataClass));
		dc.add(Restrictions.eq("classPK", dataKey));
		dc.add(Restrictions.eq("targetUser", userId));
		dc.add(Restrictions.eq("action", action));
		List<SynchLogEntity> list = findByDetached(dc);
		if (list != null && !list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}

	/**
	 * 查询某一数据的最近操作日志
	 * 
	 * @throws Exception
	 */
	@Override
	public SynchLogEntity findLogByData(String clientId, String userId,
			long timeStamp, String dataClass, String dataKey) throws Exception {
		DetachedCriteria dc = DetachedCriteria.forClass(SynchLogEntity.class);

		List<String> idList = findSynchedLogIds(clientId, userId);
		if (idList != null && !idList.isEmpty()) {
			dc.add(Restrictions.not(Restrictions.in("id", idList))); // 不包括已经同步过的日志
		}
		dc.add(Restrictions.gt("synchTime", timeStamp));
		dc.add(Restrictions.or(Restrictions.and(
				Restrictions.eq("targetUser", userId),
				Restrictions.neProperty("operateUser", "targetUser")),
				Restrictions.and(Restrictions.eq("operateUser", userId),
						Restrictions.ne("clientId", clientId))));
		dc.add(Restrictions.eq("className", dataClass));
		dc.add(Restrictions.eq("classPK", dataKey));
		dc.addOrder(Order.asc("operateTime"));

		List<SynchLogEntity> subLogList = findByDetached(dc);
		SynchLogEntity theLog = null;
		if (subLogList != null && !subLogList.isEmpty()) {
			for (SynchLogEntity log : subLogList) {
				saveSynchedLog(log, clientId, userId); // 保存到同步完成日志表中
				// 日志合并
				if (theLog == null) {
					theLog = log;
				} else {
					theLog = mergeLog(theLog, log, clientId, userId);
				}
			}
		}
		return theLog;
	}

	/**
	 * 根据操作合并两个日志
	 * 
	 * @param orgi
	 * @param nextLog
	 * @return
	 * @throws Exception
	 */
	@Override
	public SynchLogEntity mergeLog(SynchLogEntity orgi, SynchLogEntity nextLog,
			String clientId, String userId) throws Exception {
		if (orgi == null) {
			return nextLog;
		}
		if (nextLog == null) {
			return orgi;
		}
		if (!orgi.getClassName().equals(nextLog.getClassName())) {
			throw new Exception("合并的两个日志必须日志类型相同！");
		}
		if (!orgi.getClassPK().equals(nextLog.getClassPK())) {
			throw new Exception("合并的两个日志必须属于同一条数据！");
		}

		if (orgi.getAction().equals(DataSynchAction.ADD.toString())) {
			// A + T = null
			if (nextLog.getAction().equals(DataSynchAction.TRUNCATE.toString())) {
				orgi = null;
			}
			// A + U = A
			if (nextLog.getAction().equals(DataSynchAction.UPDATE.toString()) || nextLog.getAction().equals(DataSynchAction.DELETE.toString())) {
				orgi.setClientId(nextLog.getClientId());
				orgi.setClientType(nextLog.getClientType());
				orgi.setOperateUser(nextLog.getOperateUser());
				orgi.setOperateTime(nextLog.getOperateTime());
				orgi.setSynchTime(nextLog.getSynchTime());
			}
			
			if (nextLog.getAction().equals(DataSynchAction.CREATEORUPDATE.toString())) {
				orgi = nextLog;
			}
		} else if (orgi.getAction().equals(DataSynchAction.UPDATE.toString())) {
			// U + D = D
			if (nextLog.getAction().equals(DataSynchAction.DELETE.toString()) || nextLog.getAction().equals(DataSynchAction.TRUNCATE.toString())) {
				orgi = nextLog;
			}
			if (nextLog.getAction().equals(DataSynchAction.CREATEORUPDATE.toString()) || nextLog.getAction().equals(DataSynchAction.RESTORE.toString())) {
				orgi = nextLog;
			}
			
			if(nextLog.getAction().equals(DataSynchAction.ADD.toString())){
				orgi = nextLog;
			}
		} else if (orgi.getAction().equals(DataSynchAction.DELETE.toString())) {
			 // D + T = T
			 if(nextLog.getAction().equals(DataSynchAction.TRUNCATE.toString()) || nextLog.getAction().equals(DataSynchAction.RESTORE.toString()) || nextLog.getAction().equals(DataSynchAction.UPDATE.toString())){
				 orgi = nextLog;
			 }
		} else if (orgi.getAction().equals(DataSynchAction.TRUNCATE.toString())) {
			if(nextLog.getAction().equals(DataSynchAction.TRUNCATE.toString())){
				orgi = nextLog;
			}
			
			if(nextLog.getAction().equals(DataSynchAction.ADD.toString())){
				orgi = nextLog;
			}
		}
		// saveSynchedLog(orgi, clientId, userId); //保存到同步完成日志表中
		// saveSynchedLog(nextLog, clientId, userId); //保存到同步完成日志表中
		return orgi;
	}

	@Override
	public int countLogByData(String clientId, String userId, long timeStamp,
			String dataClass, String dataKey) {
		DetachedCriteria dc = DetachedCriteria.forClass(SynchLogEntity.class);

		List<String> idList = findSynchedLogIds(clientId, userId);
		if (idList != null && !idList.isEmpty()) {
			dc.add(Restrictions.not(Restrictions.in("id", idList))); // 不包括已经同步过的日志
		}
		dc.add(Restrictions.gt("synchTime", timeStamp));
		dc.add(Restrictions.or(Restrictions.and(
				Restrictions.eq("targetUser", userId),
				Restrictions.neProperty("operateUser", "targetUser")),
				Restrictions.and(Restrictions.eq("operateUser", userId),
						Restrictions.ne("clientId", clientId))));
		dc.add(Restrictions.eq("className", dataClass));
		dc.add(Restrictions.eq("classPK", dataKey));

		int count = oConvertUtils.getInt((dc
				.getExecutableCriteria(getSession()).setProjection(Projections
				.rowCount())).uniqueResult(), 0);
		return count;
	}

	/**
	 * 查询条目下附件操作日志
	 * 
	 * @param clientId
	 * @param userId
	 * @param timeStamp
	 * @param dataClass
	 * @param dataKey
	 * @return
	 */
	@Override
	public SynchLogEntity findAttachmentLogByNote(String clientId,
			String userId, long timeStamp, String noteId) {
		DetachedCriteria dc = DetachedCriteria.forClass(SynchLogEntity.class);
		List<String> attaIds = attachmentService
				.findAttaIdsByNote(noteId, null);
		List<String> idList = findSynchedLogIds(clientId, userId);
		if (idList != null && !idList.isEmpty()) {
			dc.add(Restrictions.not(Restrictions.in("id", idList))); // 不包括已经同步过的日志
		}
		if (attaIds != null && !attaIds.isEmpty()) {
			dc.add(Restrictions.in("classPK", attaIds));
		}
		dc.add(Restrictions.gt("synchTime", timeStamp));
		dc.add(Restrictions.or(Restrictions.and(
				Restrictions.eq("targetUser", userId),
				Restrictions.neProperty("operateUser", "targetUser")),
				Restrictions.and(Restrictions.eq("operateUser", userId),
						Restrictions.ne("clientId", clientId))));
		dc.add(Restrictions.eq("className", DataType.ATTACHMENT.toString()));
		dc.addOrder(Order.desc("operateTime"));

		List<SynchLogEntity> subLogList = findByDetached(dc);

		if (subLogList != null && !subLogList.isEmpty()) {
			for (SynchLogEntity log : subLogList) {
				SynchronizedLogEntity sLog = new SynchronizedLogEntity();
				sLog.setClientId(clientId);
				sLog.setLogId(log.getId());
				sLog.setOperateTime(System.currentTimeMillis());
				sLog.setTargetUser(userId);
				save(sLog); // 保存到同步完成日志表中
			}
			return subLogList.get(0);
		}
		return null;
	}

	/**
	 * 查询条目下附件操作日志数量
	 * 
	 * @param clientId
	 * @param userId
	 * @param timeStamp
	 * @param dataClass
	 * @param dataKey
	 * @return
	 */
	@Override
	public int countAttachmentLogByNote(String clientId, String userId,
			long timeStamp, String noteId) {
		DetachedCriteria dc = DetachedCriteria.forClass(SynchLogEntity.class);
		List<String> attaIds = attachmentService
				.findAttaIdsByNote(noteId, null);
		List<String> idList = findSynchedLogIds(clientId, userId);
		if (idList != null && !idList.isEmpty()) {
			dc.add(Restrictions.not(Restrictions.in("id", idList))); // 不包括已经同步过的日志
		}
		if (attaIds != null && !attaIds.isEmpty()) {
			dc.add(Restrictions.in("classPK", attaIds));
		}
		dc.add(Restrictions.gt("synchTime", timeStamp));
		dc.add(Restrictions.or(Restrictions.and(
				Restrictions.eq("targetUser", userId),
				Restrictions.neProperty("operateUser", "targetUser")),
				Restrictions.and(Restrictions.eq("operateUser", userId),
						Restrictions.ne("clientId", clientId))));
		dc.add(Restrictions.eq("className", DataType.ATTACHMENT.toString()));
		int count = oConvertUtils.getInt((dc
				.getExecutableCriteria(getSession()).setProjection(Projections
				.rowCount())).uniqueResult(), 0);
		return count;
	}

	/**
	 * 查询专题成员日志
	 * 
	 * @param clientId
	 * @param userId
	 * @param timeStamp
	 * @param subjectId
	 * @return
	 */
	@Override
	public SynchLogEntity findSubjectUserLogs(String clientId, String userId,
			long timeStamp, String subjectId) {
		DetachedCriteria dc = DetachedCriteria.forClass(SynchLogEntity.class);
		List<String> idList = findSynchedLogIds(clientId, userId);
		if (idList != null && !idList.isEmpty()) {
			dc.add(Restrictions.not(Restrictions.in("id", idList))); // 不包括已经同步过的日志
		}
		dc.add(Restrictions.gt("synchTime", timeStamp));
		// dc.add(Restrictions.or(Restrictions.and(Restrictions.eq("targetUser",
		// userId), Restrictions.neProperty("operateUser", "targetUser")),
		// Restrictions.and(Restrictions.eq("operateUser", userId),
		// Restrictions.ne("clientId", clientId))));
		dc.add(Restrictions.ne("clientId", clientId));
		dc.add(Restrictions.eq("className", DataType.SUBJECTUSER.toString()));
		dc.add(Restrictions.eq("classPK", subjectId));
		dc.addOrder(Order.desc("operateTime"));

		List<SynchLogEntity> subLogList = pageList(dc, 0,
				SynchConstants.RETURN_CLIENT_MAX_COUNT);

		if (subLogList != null && !subLogList.isEmpty()) {
			for (SynchLogEntity log : subLogList) {
				SynchronizedLogEntity sLog = new SynchronizedLogEntity();
				sLog.setClientId(clientId);
				sLog.setLogId(log.getId());
				sLog.setOperateTime(System.currentTimeMillis());
				sLog.setTargetUser(userId);
				save(sLog); // 保存到同步完成日志表中
			}
			return subLogList.get(0);
		}
		return null;
	}

	/**
	 * 查询专题成员日志数量
	 * 
	 * @param clientId
	 * @param userId
	 * @param timeStamp
	 * @param subjectId
	 * @return
	 */
	@Override
	public int countSubjectUserLogs(String clientId, String userId,
			long timeStamp, String subjectId) {
		DetachedCriteria dc = DetachedCriteria.forClass(SynchLogEntity.class);
		List<String> idList = findSynchedLogIds(clientId, userId);
		if (idList != null && !idList.isEmpty()) {
			dc.add(Restrictions.not(Restrictions.in("id", idList))); // 不包括已经同步过的日志
		}
		dc.add(Restrictions.gt("synchTime", timeStamp));
		// dc.add(Restrictions.or(Restrictions.and(Restrictions.eq("targetUser",
		// userId), Restrictions.neProperty("operateUser", "targetUser")),
		// Restrictions.and(Restrictions.eq("operateUser", userId),
		// Restrictions.ne("clientId", clientId))));
		dc.add(Restrictions.ne("clientId", clientId));
		dc.add(Restrictions.eq("className", DataType.SUBJECTUSER.toString()));
		dc.add(Restrictions.eq("classPK", subjectId));
		dc.addOrder(Order.desc("operateTime"));

		int count = oConvertUtils.getInt((dc
				.getExecutableCriteria(getSession()).setProjection(Projections
				.rowCount())).uniqueResult(), 0);
		return count;
	}

	/**
	 * 查询某专题下数据的日志
	 * 
	 * @param clientId
	 * @param userId
	 * @param timeStamp
	 * @param subjectId
	 * @return
	 */
	@Override
	public SynchLogEntity findSubjectRelatedLogs(String clientId,
			String userId, long timeStamp, String subjectId) {
		Group group = groupService.findGroup(SubjectEntity.class.getName(),
				subjectId);
		// 所有该专题下的数据集合，不包括附件
		List<Group> list = groupService.findGroupByParent(group.getGroupId());
		if (list != null && !list.isEmpty()) {
			List<String> keysList = new ArrayList<String>();
			for (Group g : list) {
				keysList.add(g.getClassPk());
			}

			DetachedCriteria dc = DetachedCriteria
					.forClass(SynchLogEntity.class);
			List<String> idList = findSynchedLogIds(clientId, userId);
			if (idList != null && !idList.isEmpty()) {
				dc.add(Restrictions.not(Restrictions.in("id", idList))); // 不包括已经同步过的日志
			}
			dc.add(Restrictions.gt("synchTime", timeStamp));
			// dc.add(Restrictions.or(Restrictions.and(Restrictions.eq("targetUser",
			// userId), Restrictions.neProperty("operateUser", "targetUser")),
			// Restrictions.and(Restrictions.eq("operateUser", userId),
			// Restrictions.ne("clientId", clientId))));
			dc.add(Restrictions.ne("clientId", clientId));
			dc.add(Restrictions.in("classPK", keysList));
			dc.addOrder(Order.desc("operateTime"));

			List<SynchLogEntity> subLogList = pageList(dc, 0,
					SynchConstants.RETURN_CLIENT_MAX_COUNT);

			if (subLogList != null && !subLogList.isEmpty()) {
				for (SynchLogEntity log : subLogList) {
					SynchronizedLogEntity sLog = new SynchronizedLogEntity();
					sLog.setClientId(clientId);
					sLog.setLogId(log.getId());
					sLog.setOperateTime(System.currentTimeMillis());
					sLog.setTargetUser(userId);
					save(sLog); // 保存到同步完成日志表中
				}
				return subLogList.get(0);
			}
		}
		return null;
	}

	@Override
	public int countSubjectRelatedLogs(String clientId, String userId,
			long timeStamp, String subjectId) {
		Group group = groupService.findGroup(SubjectEntity.class.getName(),
				subjectId);
		int count = 0;
		// 所有该专题下的数据集合，不包括附件
		List<Group> list = groupService.findGroupByParent(group.getGroupId());
		if (list != null && !list.isEmpty()) {
			List<String> keysList = new ArrayList<String>();
			for (Group g : list) {
				keysList.add(g.getClassPk());
			}

			DetachedCriteria dc = DetachedCriteria
					.forClass(SynchLogEntity.class);
			List<String> idList = findSynchedLogIds(clientId, userId);
			if (idList != null && !idList.isEmpty()) {
				dc.add(Restrictions.not(Restrictions.in("id", idList))); // 不包括已经同步过的日志
			}
			dc.add(Restrictions.gt("synchTime", timeStamp));
			// dc.add(Restrictions.or(Restrictions.and(Restrictions.eq("targetUser",
			// userId), Restrictions.neProperty("operateUser", "targetUser")),
			// Restrictions.and(Restrictions.eq("operateUser", userId),
			// Restrictions.ne("clientId", clientId))));
			dc.add(Restrictions.ne("clientId", clientId));
			dc.add(Restrictions.in("classPK", keysList));

			count = oConvertUtils.getInt((dc
					.getExecutableCriteria(getSession())
					.setProjection(Projections.rowCount())).uniqueResult(), 0);
		}
		return count;
	}

	/**
	 * 查询某一类型数据日志
	 * 
	 * @param dataClass
	 * @return
	 */
	public List<SynchLogEntity> findLogsByDataClass(String dataClass) {
		DetachedCriteria dc = DetachedCriteria.forClass(SynchLogEntity.class);
		dc.add(Restrictions.eq("className", dataClass));
		List<SynchLogEntity> list = findByDetached(dc);
		return list;
	}

	public List<SynchLogEntity> findLogsByDataKeys(String dataClass,
			Collection<String> dataKeys, long timeStamp) {
		DetachedCriteria dc = DetachedCriteria.forClass(SynchLogEntity.class);
		dc.add(Restrictions.gt("synchTime", timeStamp));
		dc.add(Restrictions.eq("className", dataClass));
		dc.add(Restrictions.in("classPK", dataKeys));
		List<SynchLogEntity> list = findByDetached(dc);
		return list;
	}

	public List<SynchLogEntity> findLogsByDataKeys(String dataClass,
			String[] dataKeys, long timeStamp) {
		DetachedCriteria dc = DetachedCriteria.forClass(SynchLogEntity.class);
		dc.add(Restrictions.gt("synchTime", timeStamp));
		dc.add(Restrictions.eq("className", dataClass));
		dc.add(Restrictions.in("classPK", dataKeys));
		List<SynchLogEntity> list = findByDetached(dc);
		return list;
	}

	@Override
	public List<SynchLogEntity> findSynchLogsByTarget(String clientId,
			String userId, long timeStamp, int offset) {
		DetachedCriteria dc = DetachedCriteria.forClass(SynchLogEntity.class);
		dc.add(Restrictions.gt("synchTime", timeStamp));
		dc.add(Restrictions.or(Restrictions.and(
				Restrictions.eq("targetUser", userId),
				Restrictions.neProperty("operateUser", "targetUser")),
				Restrictions.and(Restrictions.eq("operateUser", userId),
						Restrictions.ne("clientId", clientId))));
		dc.addOrder(Order.asc("operateTime"));

		List<SynchLogEntity> subLogList = pageList(dc, offset,
				SynchConstants.RETURN_CLIENT_MAX_COUNT);

		return subLogList;
	}

	@Override
	public int countSynchLogsByTarget(String clientId, String userId, String action,
			long beiginTimeStamp, long endTimestamp) {
		DetachedCriteria dc = DetachedCriteria.forClass(SynchLogEntity.class);
		
		dc.add(Restrictions.or(Restrictions.and(
				Restrictions.eq("targetUser", userId),
				Restrictions.neProperty("operateUser", "targetUser")),
				Restrictions.and(Restrictions.eq("operateUser", userId),
						Restrictions.ne("clientId", clientId))));
		dc.add(Restrictions.gt("synchTime", beiginTimeStamp));
		dc.add(Restrictions.le("synchTime", endTimestamp));
		dc.add(Restrictions.eq("action", action));
		
		int count = oConvertUtils.getInt((dc
				.getExecutableCriteria(getSession()).setProjection(Projections
				.rowCount())).uniqueResult(), 0);
		return count;
	}

	/**
	 * 返回客户端日志查询方法
	 */
	@Override
	public List<SynchLogEntity> findSynchLogsByTarget(String clientId,
			String userId, long timeStamp, String action, String dataClass,
			int offset) {
		DetachedCriteria dc = DetachedCriteria.forClass(SynchLogEntity.class);
		dc.add(Restrictions.or(Restrictions.and(
				Restrictions.eq("targetUser", userId),
				Restrictions.neProperty("operateUser", "targetUser")),
				Restrictions.and(Restrictions.eq("operateUser", userId),
						Restrictions.ne("clientId", clientId))));
		// dc.add(Restrictions.eq("clientId",
		// SynchConstants.CLIENT_DEFAULT_ID)); // web页面操作产生的操作日志
		List<String> idList = findSynchedLogIds(clientId, userId);
		if (idList != null && !idList.isEmpty()) {
			dc.add(Restrictions.not(Restrictions.in("id", idList))); // 不包括已经同步过的日志
		}
		dc.add(Restrictions.gt("synchTime", timeStamp));
		dc.add(Restrictions.eq("action", action));
		dc.add(Restrictions.eq("className", dataClass));
		dc.addOrder(Order.asc("operateTime"));

		List<SynchLogEntity> subLogList = pageList(dc, offset,
				SynchConstants.RETURN_CLIENT_MAX_COUNT);

		return subLogList;
	}

	@Override
	public int countSynchLogsByTarget(String clientId, String userId,
			long timeStamp, long endTime, String dataClass, String action) {
		DetachedCriteria dc = DetachedCriteria.forClass(SynchLogEntity.class);
		/*dc.add(Restrictions.or(Restrictions.and(
				Restrictions.eq("targetUser", userId),
				Restrictions.neProperty("operateUser", "targetUser")),
				Restrictions.and(Restrictions.eq("operateUser", userId),
						Restrictions.ne("clientId", clientId))));*/
		List<String> idList = findSynchedLogIds(clientId, userId);
		if (idList != null && !idList.isEmpty()) {
			dc.add(Restrictions.not(Restrictions.in("id", idList))); // 不包括已经同步过的日志
		}
		dc.add(Restrictions.or(Restrictions.and(
				Restrictions.eq("targetUser", userId),
				Restrictions.neProperty("operateUser", "targetUser")),
				Restrictions.and(Restrictions.eq("operateUser", userId),
						Restrictions.ne("clientId", clientId))));
		dc.add(Restrictions.gt("synchTime", timeStamp));
		dc.add(Restrictions.le("synchTime", endTime));
		dc.add(Restrictions.eq("className", dataClass));
		if (!StringUtil.isEmpty(action)) {
			dc.add(Restrictions.eq("action", action));
		}
		int count = oConvertUtils.getInt((dc
				.getExecutableCriteria(getSession()).setProjection(Projections
				.rowCount())).uniqueResult(), 0);
		return count;
	}

	/**
	 * 返回客户端日志查询方法
	 * 
	 * @throws Exception
	 */
	@Override
	public List<SynchLogEntity> findTruncSynchLogs(String clientId,
			String userId, long timeStamp, long endTime, String dataClass, boolean saveLog) throws Exception {
		String[] dataTypes = { DataType.NOTE.toString(),
				DataType.DIRECTORY.toString() };
		// dc.add(Restrictions.eq("clientId",
		// SynchConstants.CLIENT_DEFAULT_ID)); // web页面操作产生的操作日志
		List<String> idList = findSynchedLogIds(clientId, userId);
		List<SynchLogEntity> logList = null;

		if (dataClass.equals(DataType.ALL.toString())
				|| dataClass.equals(DataType.BATCHDATA.toString())) {
			for (int i = 0; i < dataTypes.length; i++) {
				DetachedCriteria dc = DetachedCriteria
						.forClass(SynchLogEntity.class);
				dc.add(Restrictions.or(Restrictions.and(
						Restrictions.eq("targetUser", userId),
						Restrictions.neProperty("operateUser", "targetUser")),
						Restrictions.and(
								Restrictions.eq("operateUser", userId),
								Restrictions.ne("clientId", clientId))));
				dc.add(Restrictions.eq("className", dataTypes[i]));
				if (idList != null && !idList.isEmpty()) {
					dc.add(Restrictions.not(Restrictions.in("id", idList))); // 不包括已经同步过的日志
				}
				dc.add(Restrictions.gt("synchTime", timeStamp));
				dc.add(Restrictions.le("synchTime", endTime));
				
				//dc.addOrder(Order.asc("classPK"));
				dc.addOrder(Order.asc("operateTime"));
				dc.addOrder(Order.desc("operateType"));
				List<SynchLogEntity> subLogList = findByDetached(dc);

				if (subLogList != null && !subLogList.isEmpty()) {
					subLogList = filterTruncSynchLogs(subLogList); // 过滤日志，找出truncate操作日志
					subLogList = DataSynchizeUtil.sortSynchLog(subLogList);
					if (subLogList != null && !subLogList.isEmpty()) {
						// 删除操作的日志，一次合并一个数据类型下的数据，一起返回
						logList = mergeAllLogs(subLogList, clientId, userId, DataSynchAction.TRUNCATE.toString(), saveLog);
						// 有日志数据返回
						if (logList != null && !logList.isEmpty()) {
							break;
						}
					}
				}
			}
		} else {
			DetachedCriteria dc = DetachedCriteria
					.forClass(SynchLogEntity.class);
			dc.add(Restrictions.or(Restrictions.and(
					Restrictions.eq("targetUser", userId),
					Restrictions.neProperty("operateUser", "targetUser")),
					Restrictions.and(Restrictions.eq("operateUser", userId),
							Restrictions.ne("clientId", clientId))));
			if (idList != null && !idList.isEmpty()) {
				dc.add(Restrictions.not(Restrictions.in("id", idList))); // 不包括已经同步过的日志
			}
			dc.add(Restrictions.gt("synchTime", timeStamp));
			dc.add(Restrictions.eq("className", dataClass));
			//dc.addOrder(Order.asc("classPK"));
			dc.addOrder(Order.asc("operateTime"));
			dc.addOrder(Order.desc("operateType"));
			List<SynchLogEntity> subLogList = findByDetached(dc);
			if (subLogList != null && !subLogList.isEmpty()) {
				subLogList = filterTruncSynchLogs(subLogList); // 过滤日志，找出所有truncate操作日志
				subLogList = DataSynchizeUtil.sortSynchLog(subLogList);
				if (subLogList != null && !subLogList.isEmpty()) {
					if (true) { // 删除操作的日志，一次全部合并返回
						logList = mergeAllLogs(subLogList, clientId, userId, DataSynchAction.TRUNCATE.toString(), saveLog);
					}
				}
			}
		}
		return logList;

	}

	/**
	 * 返回客户端日志查询方法
	 * 
	 * @throws Exception
	 */
	@Override
	public List<SynchLogEntity> findSynchLogsByTarget(String clientId,
			String userId, long timeStamp, long endTime, String dataClass,
			boolean filterDelete, boolean saveLog) throws Exception {
		String[] dataTypes = SynchDataCache.getDatasSort();
		if (!filterDelete) {
			dataTypes = SynchDataCache.getReverseDatasSort();
		}
		// dc.add(Restrictions.eq("clientId",
		// SynchConstants.CLIENT_DEFAULT_ID)); // web页面操作产生的操作日志
		List<String> idList = findSynchedLogIds(clientId, userId);
		List<SynchLogEntity> logList = null;

		if (dataClass.equals(DataType.ALL.toString())
				|| dataClass.equals(DataType.BATCHDATA.toString())) {
			for (int i = 0; i < dataTypes.length; i++) {
				DetachedCriteria dc = DetachedCriteria
						.forClass(SynchLogEntity.class);
				dc.add(Restrictions.or(Restrictions.and(
						Restrictions.eq("targetUser", userId),
						Restrictions.neProperty("operateUser", "targetUser")),
						Restrictions.and(
								Restrictions.eq("operateUser", userId),
								Restrictions.ne("clientId", clientId))));
				dc.add(Restrictions.eq("className", dataTypes[i]));
				if (idList != null && !idList.isEmpty()) {
					dc.add(Restrictions.not(Restrictions.in("id", idList))); // 不包括已经同步过的日志
				}
				dc.add(Restrictions.gt("synchTime", timeStamp));
				dc.add(Restrictions.le("synchTime", endTime));
				
				//dc.addOrder(Order.asc("classPK"));
				dc.addOrder(Order.asc("operateTime"));
				dc.addOrder(Order.desc("operateType"));
				List<SynchLogEntity> subLogList = findByDetached(dc);

				if (subLogList != null && !subLogList.isEmpty()) {
					subLogList = filterSynchLogs(subLogList, filterDelete); // 过滤日志，例如先不处理删除操作的日志就先过滤掉
					subLogList = DataSynchizeUtil.sortSynchLog(subLogList);
					if (subLogList != null && !subLogList.isEmpty()) {
						if (!filterDelete) {
							// 删除操作的日志，一次合并一个数据类型下的数据，一起返回
							logList = mergeAllLogs(subLogList, clientId, userId, DataSynchAction.DELETE.toString(), saveLog);
						} else {
							logList = mergeLog(subLogList, clientId, userId, saveLog);
						}
						// 有日志数据返回
						if (logList != null && !logList.isEmpty()) {
							break;
						}
					}
				}
			}
		} else {
			DetachedCriteria dc = DetachedCriteria
					.forClass(SynchLogEntity.class);
			dc.add(Restrictions.or(Restrictions.and(
					Restrictions.eq("targetUser", userId),
					Restrictions.neProperty("operateUser", "targetUser")),
					Restrictions.and(Restrictions.eq("operateUser", userId),
							Restrictions.ne("clientId", clientId))));
			if (idList != null && !idList.isEmpty()) {
				dc.add(Restrictions.not(Restrictions.in("id", idList))); // 不包括已经同步过的日志
			}
			dc.add(Restrictions.gt("synchTime", timeStamp));
			dc.add(Restrictions.le("synchTime", endTime));
			dc.add(Restrictions.eq("className", dataClass));
			
			//dc.addOrder(Order.asc("classPK"));
			dc.addOrder(Order.asc("operateTime"));
			dc.addOrder(Order.desc("operateType"));
			List<SynchLogEntity> subLogList = findByDetached(dc);
			if (subLogList != null && !subLogList.isEmpty()) {
				subLogList = filterSynchLogs(subLogList, filterDelete); // 过滤日志，例如先不处理删除操作的日志就先过滤掉
				subLogList = DataSynchizeUtil.sortSynchLog(subLogList);
				if (subLogList != null && !subLogList.isEmpty()) {
					if (!filterDelete) { // 删除操作的日志，一次全部合并返回
						logList = mergeAllLogs(subLogList, clientId, userId, DataSynchAction.DELETE.toString(), saveLog);
					} else { // 其它日志只返回指定条数
						logList = mergeLog(subLogList, clientId, userId, saveLog);
					}
				}
			}
		}
		return logList;

	}

	/**
	 * 处理BAN掉条目相关的日志
	 * 
	 * @throws Exception
	 */
	@Override
	public List<SynchLogEntity> dealBanNoteSynchLogs(String noteId, String clientId,
			String userId, long timeStamp, long endTime, String[] dataClass,
			boolean filterDelete, boolean saveLog){
		List<String> idList = findSynchedLogIds(clientId, userId);
		
		List<NoteTag> tagList = tagService.findNoteTagsByNote(noteId);
		List<AttachmentEntity> attaList = attachmentService.findAttachmentByNote(noteId, null, null, new Integer[]{Constants.FILE_TYPE_NORMAL});
		List<String> banIdList = new ArrayList<String>();
		if(attaList != null && !attaList.isEmpty()){
			for(AttachmentEntity atta : attaList){
				banIdList.add(atta.getId());
			}
		}
		
		if(tagList != null && !tagList.isEmpty()){
			for(NoteTag nt : tagList){
				banIdList.add(nt.getId());
			}
		}
		
		DetachedCriteria dc = DetachedCriteria.forClass(SynchLogEntity.class);
		dc.add(Restrictions.or(Restrictions.and(
				Restrictions.eq("targetUser", userId),
				Restrictions.neProperty("operateUser", "targetUser")),
				Restrictions.and(Restrictions.eq("operateUser", userId),
						Restrictions.ne("clientId", clientId))));
		if (idList != null && !idList.isEmpty()) {
			dc.add(Restrictions.not(Restrictions.in("id", idList))); // 不包括已经同步过的日志
		}
		if(banIdList != null && !banIdList.isEmpty()){
			dc.add(Restrictions.in("classPK", banIdList));
		}
		
		dc.add(Restrictions.in("className", dataClass));
		dc.add(Restrictions.gt("synchTime", timeStamp));
		dc.add(Restrictions.le("synchTime", endTime));
		
		//dc.addOrder(Order.asc("classPK"));
		dc.addOrder(Order.asc("operateTime"));
		dc.addOrder(Order.desc("operateType"));
		List<SynchLogEntity> subLogList = findByDetached(dc);
		if (subLogList != null && !subLogList.isEmpty()) {
			if (subLogList != null && !subLogList.isEmpty()) {
				saveSynchedLog(subLogList, clientId, userId);
			}
		}
		return subLogList;
	}
	
	/**
	 * 处理BAN掉条目相关的日志
	 * 
	 * @throws Exception
	 */
	@Override
	public List<SynchLogEntity> dealBanDirSynchLogs(String dirId, String clientId,
			String userId, long timeStamp, long endTime, String dataClass,
			boolean filterDelete, boolean saveLog){
		List<String> idList = findSynchedLogIds(clientId, userId);
		
		List<AttachmentEntity> attaList = attachmentService.findAttachmentByDir(dirId);
		List<String> attaIdList = null;
		if(attaList != null && !attaList.isEmpty()){
			attaIdList = new ArrayList<String>(attaList.size());
			for(AttachmentEntity atta : attaList){
				attaIdList.add(atta.getId());
			}
		}
		
		DetachedCriteria dc = DetachedCriteria.forClass(SynchLogEntity.class);
		dc.add(Restrictions.or(Restrictions.and(
				Restrictions.eq("targetUser", userId),
				Restrictions.neProperty("operateUser", "targetUser")),
				Restrictions.and(Restrictions.eq("operateUser", userId),
						Restrictions.ne("clientId", clientId))));
		if (idList != null && !idList.isEmpty()) {
			dc.add(Restrictions.not(Restrictions.in("id", idList))); // 不包括已经同步过的日志
		}
		if(attaIdList != null && !attaIdList.isEmpty()){
			dc.add(Restrictions.in("classPK", attaIdList));
		}
		
		dc.add(Restrictions.gt("synchTime", timeStamp));
		dc.add(Restrictions.le("synchTime", endTime));
		dc.add(Restrictions.eq("className", dataClass));
		
		//dc.addOrder(Order.asc("classPK"));
		dc.addOrder(Order.asc("operateTime"));
		dc.addOrder(Order.desc("operateType"));
		List<SynchLogEntity> subLogList = findByDetached(dc);
		if (subLogList != null && !subLogList.isEmpty()) {
			if (subLogList != null && !subLogList.isEmpty()) {
				saveSynchedLog(subLogList, clientId, userId);
			}
		}
		return subLogList;
	}
	
	/**
	 * 返回客户端日志查询方法
	 * 
	 * @throws Exception
	 */
	@Override
	public List<SynchLogEntity> findSynchLogsBySQL(String clientId,
			String userId, long timeStamp, String dataClass) throws Exception {
		String[] dataTypes = SynchDataCache.getDatasSort();

		// dc.add(Restrictions.eq("clientId",
		// SynchConstants.CLIENT_DEFAULT_ID)); // web页面操作产生的操作日志
		List<String> idList = findSynchedLogIds(clientId, userId);
		List<SynchLogEntity> logList = null;
		List<Map<String, Object>> resultList = null;
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT SUM(tt.actionvalue) val");
		sb.append(",tt.classname");
		sb.append(",tt.classpk");
		sb.append(",MAX(tt.operatetime) operatetime");
		sb.append(",tt.operateUser");
		sb.append(",MAX(tt.synchTime) synchTime");
		sb.append(",tt.targetuser ");
		sb.append("FROM (");
		sb.append("SELECT t.classname");
		sb.append(",t.classpk");
		sb.append(",CASE WHEN t.action='ADD' THEN 1 WHEN t.action='DELETE' THEN -1 ELSE 0 END AS actionvalue");
		sb.append(",t.operatetime");
		sb.append(",t.operateUser");
		sb.append(",t.synchTime");
		sb.append(",t.targetuser ");
		sb.append("FROM eht_synchlog t WHERE ");
		sb.append("((t.targetUser=? and t.operateUser<>t.targetUser) or (t.operateUser=? and t.clientId<>?)) ");
		if (idList != null && !idList.isEmpty()) {
			sb.append("and t.id not in(");
			for (String id : idList) {
				sb.append("'" + id + "',");
			}
			sb.setLength(sb.length() - 1);
			sb.append(") ");
		}
		sb.append("and t.synchTime>? ");
		sb.append("and t.className=? ");

		sb.append(") tt ");
		sb.append("GROUP BY tt.classpk ");
		sb.append("ORDER BY SUM(tt.actionvalue) DESC,tt.synchTime,tt.classpk");
		if (dataClass.equals(DataType.ALL.toString())) {
			for (int i = 0; i < dataTypes.length; i++) {
				Object[] args = new Object[] { userId, userId, clientId,
						timeStamp, dataTypes[i] };
				resultList = findForJdbc(sb.toString(), args);
			}
		} else {
			Object[] args = new Object[] { userId, userId, clientId, timeStamp,
					dataClass };
			resultList = findForJdbc(sb.toString(), args);
		}
		return logList;

	}

	/**
	 * 过滤集合中删除操作类型日志或过滤非删除日志
	 * 
	 * @param logList
	 * @param filterDelete
	 *            是否是过滤删除操作类型日志
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private List<SynchLogEntity> filterSynchLogs(List<SynchLogEntity> logList,
			boolean filterDelete) throws Exception {
		// 所有删除操作日志集合
		List<SynchLogEntity> filterList = new ArrayList<SynchLogEntity>();
		for (SynchLogEntity log : logList) {
			if (log.getAction().equals(DataSynchAction.DELETE.toString())) {
				filterList.add(log);
			}
		}

		List<SynchLogEntity> result = new ArrayList<SynchLogEntity>();
		if (filterList != null && !filterList.isEmpty()) {
			for (SynchLogEntity l : logList) {
				boolean equal = false;
				for (SynchLogEntity log : filterList) {
					if (l.getClassName().equals(log.getClassName())
							&& l.getClassPK().equals(log.getClassPK())) {
						equal = true;
						break;
					}
				}
				if (equal) {
					result.add(l); // 将所有有删除操作的日志添加进result
				}
			}
			// 如果要过滤掉删除操作日志
			if (filterDelete) {
				return ListUtils.subtract(logList, result);
			} else {
				return result;
			}

		} else {
			if (filterDelete) {
				return logList;
			}
		}
		return result;
	}

	/**
	 * 过滤集合中删除操作类型日志或过滤非删除日志
	 * 
	 * @param logList
	 * @param filterDelete
	 *            是否是过滤删除操作类型日志
	 * @return
	 * @throws Exception
	 */
	private List<SynchLogEntity> filterTruncSynchLogs(
			List<SynchLogEntity> logList) throws Exception {
		// 所有删除操作日志集合
		List<SynchLogEntity> filterList = new ArrayList<SynchLogEntity>();
		for (SynchLogEntity log : logList) {
			if (log.getAction().equals(DataSynchAction.TRUNCATE.toString())) {
				filterList.add(log);
			}
		}

		List<SynchLogEntity> result = new ArrayList<SynchLogEntity>();
		if (filterList != null && !filterList.isEmpty()) {
			for (SynchLogEntity l : logList) {
				boolean equal = false;
				for (SynchLogEntity log : filterList) {
					if (l.getClassName().equals(log.getClassName())
							&& l.getClassPK().equals(log.getClassPK())) {
						equal = true;
						break;
					}
				}
				if (equal) {
					result.add(l); // 将所有有truncate操作的日志添加进result
				}
			}
			return result;

		} else {
			return filterList;
		}
	}

	/**
	 * 合并集合中的日志,返回客户端一条
	 * 
	 * @throws Exception
	 */
	private List<SynchLogEntity> mergeLog(List<SynchLogEntity> logList,
			String clientId, String userId, boolean saveLog) throws Exception {
		if (logList.size() == SynchConstants.RETURN_CLIENT_MAX_COUNT) {
			saveSynchedLog(logList.get(0), clientId, userId);
			return logList;
		}
		boolean findedLog = false;
		List<SynchLogEntity> result = new ArrayList<SynchLogEntity>();
		for (int i = 0; i < logList.size(); i++) {
			if (result.size() == SynchConstants.RETURN_CLIENT_MAX_COUNT + 1) {
				break;
			}
			SynchLogEntity orgi = logList.get(i);
			String classPK = orgi.getClassPK();
			if (!findedLog) {
				if(saveLog){
					saveSynchedLog(orgi, clientId, userId);
				}
			} else {
				result.add(orgi); // 此日志用来确定客户下一次请求哪种类型日志
				break;
			}
			if (i < logList.size() - 1) {
				for (int k = i + 1; k < logList.size(); k++) {
					SynchLogEntity nextLog = logList.get(k);
					// 判断是否为同一数据日志
					if (nextLog.getClassPK().equals(classPK)) {
						orgi = mergeLog(orgi, nextLog, clientId, userId);
						if (!findedLog) {
							if(saveLog){
								saveSynchedLog(nextLog, clientId, userId);
							}
						}
						i++; // 外层循环跳过 nextLog
					}
					// 下一数据日志了或已合并到最后
					if (!nextLog.getClassPK().equals(classPK)
							|| k == logList.size() - 1) {
						if (orgi != null
								&& result.size() <= SynchConstants.RETURN_CLIENT_MAX_COUNT) {
							result.add(orgi);
							findedLog = true; // 已经找到要返回客户端的日志
							break;
						}
					}
				}

			}
		}
		return result;
	}

	@Override
	public List<SynchLogEntity> mergeAllLogs(List<SynchLogEntity> logList,
			String clientId, String userId, String mergeAction, boolean saveLog) throws Exception {
		List<SynchLogEntity> result = new ArrayList<SynchLogEntity>();
		// 只有一条日志，不需要合并
		if (logList.size() == 1) {
			SynchLogEntity orgi = logList.get(0);
			if(saveLog){
				saveSynchedLog(orgi, clientId, userId);
			}
			result.add(orgi);
			return result;
		}
		
		for (int i = 0; i < logList.size(); i++) {
			List<SynchLogEntity> saveList = new ArrayList<SynchLogEntity>();
			SynchLogEntity orgi = logList.get(i);
			String classPK = orgi.getClassPK();
			/*if(saveLog){
				saveSynchedLog(orgi, clientId, userId);
			}*/
			saveList.add(orgi);
			if (i < logList.size() - 1) {
				for (int k = i + 1; k < logList.size(); k++) {
					SynchLogEntity nextLog = logList.get(k);
					// 判断是否为同一数据日志
					if (nextLog.getClassPK().equals(classPK)) {
						orgi = mergeLog(orgi, nextLog, clientId, userId);
						/*if(saveLog){
							saveSynchedLog(nextLog, clientId, userId);
						}*/
						saveList.add(nextLog);
						i++; // 外层循环跳过 nextLog
					}
					// 下一数据日志了或已合并到最后
					if (!nextLog.getClassPK().equals(classPK)
							|| k == logList.size() - 1) {
						if (orgi != null) {
							if(orgi.getAction().equals(mergeAction)){
								result.add(orgi);
								saveSynchedLog(saveList, clientId, userId);
							}
						}else{
							saveSynchedLog(saveList, clientId, userId);
						}
						break;
					}
				}

			} else { // i == 最后一条记录索引
				result.add(orgi);
			}
		}
		return result;
	}
	
	private void saveSynchedLog(List<SynchLogEntity> saveList, String clientId, String userId){
		for(SynchLogEntity log : saveList){
			saveSynchedLog(log, clientId, userId);
		}
	}
	
	@Override
	public List<String> findSynchedLogIds(String clientId, String userId) {
		String sql = "select logId from eht_usedlog where clientId='"
				+ clientId + "' and targetUser='" + userId + "'";
		List<String> list = findListbySql(sql);
		return list;
	}

	@Override
	public void saveSynchedLog(SynchLogEntity theLog, String clientId,
			String userId) {
		SynchronizedLogEntity sLog = new SynchronizedLogEntity();
		sLog.setClientId(clientId);
		sLog.setLogId(theLog.getId());
		sLog.setOperateTime(theLog.getSynchTime());
		sLog.setTargetUser(userId);
		sLog.setClassName(theLog.getClassName());
		sLog.setClassPK(theLog.getClassPK());
		sLog.setAction(theLog.getAction());
		saveOrUpdate(sLog); // 保存到同步完成日志表中
	}

	@Override
	public long deleteSynchedLogs(String clientId, String userId) {
		List<SynchronizedLogEntity> list = findSynchedLogs(clientId, userId);
		if (list != null && !list.isEmpty()) {
			deleteAllEntitie(list);
			return list.get(0).getOperateTime();
		}
		return 0;
	}

	@Override
	public List<SynchronizedLogEntity> findSynchedLogs(String clientId,
			String userId) {
		DetachedCriteria dc = DetachedCriteria
				.forClass(SynchronizedLogEntity.class);
		dc.add(Restrictions.eq("clientId", clientId));
		dc.add(Restrictions.eq("targetUser", userId));
		dc.addOrder(Order.desc("operateTime"));
		List<SynchronizedLogEntity> list = findByDetached(dc);

		return list;
	}
	
	@Override
	public SynchronizedLogEntity findSynchedLog(String clientId,
			String userId, String className, String classPK, String action) {
		DetachedCriteria dc = DetachedCriteria
				.forClass(SynchronizedLogEntity.class);
		dc.add(Restrictions.eq("clientId", clientId));
		dc.add(Restrictions.eq("targetUser", userId));
		dc.add(Restrictions.eq("className", className));
		dc.add(Restrictions.eq("classPK", classPK));
		dc.add(Restrictions.eq("action", action));
		
		List<SynchronizedLogEntity> list = findByDetached(dc);
		if(list != null && !list.isEmpty()){
			return list.get(0);
		}
		return null;
	}
	
	@Override
	public SynchronizedLogEntity findSynchedLogByLogId(String logId) {
		DetachedCriteria dc = DetachedCriteria
				.forClass(SynchronizedLogEntity.class);
		dc.add(Restrictions.eq("logId", logId));
		List<SynchronizedLogEntity> list = findByDetached(dc);
		if(list != null && !list.isEmpty()){
			return list.get(0);
		}
		return null;
	}
	
	/**
	 * 从集合中移除日志或从集合中挑选日志
	 * 
	 * @param logList
	 * @param className
	 * @param classPk
	 * @param remove
	 *            true为移除, false保留
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<SynchLogEntity> filterLogsByClassPK(
			List<SynchLogEntity> logList, String className, String classPk,
			boolean remove) {
		List<SynchLogEntity> list = new ArrayList<SynchLogEntity>();
		for (SynchLogEntity log : logList) {
			if (!log.getClassName().equals(className)
					|| !log.getClassPK().equals(classPk)) {
				list.add(log);
			}
		}
		if (remove) {
			return list;
		} else {
			return ListUtils.subtract(logList, list);
		}
	}

	/**
	 * 查询需要直载的文件--附件
	 * @param userId
	 * @param clientId
	 * @return
	 * @throws Exception
	 */
	@Override
	public List<SynchronizedLogEntity> findNeedDownloadAttachment(
			String userId, String clientId) throws Exception {
		DetachedCriteria dc = DetachedCriteria
				.forClass(SynchronizedLogEntity.class);
		dc.add(Restrictions.eq("clientId", clientId));
		dc.add(Restrictions.eq("targetUser", userId));
		dc.add(Restrictions.eq("className", DataType.ATTACHMENT.toString()));
		dc.add(Restrictions.eq("action", DataSynchAction.ADD.toString()));
		dc.add(Restrictions.eq("status", SynchConstants.LOG_NOT_SYNCHRONIZED));
		dc.addOrder(Order.asc("className"));
		dc.addOrder(Order.asc("classPK"));
		dc.addOrder(Order.asc("operateTime"));
		List<SynchronizedLogEntity> list = findByDetached(dc);

		List<SynchronizedLogEntity> logList = mergeAttaLogs(list, clientId,
				userId);
		return logList;
	}
	
	@Override
	public void updateSynchedLogStatus(String userId, String clientId, String dataType, String classPk){
		Object[] params = new Object[]{SynchConstants.LOG_SYNCHRONIZED, userId, clientId, dataType, classPk};
		executeHql("update SynchronizedLogEntity set status=? where targetUser=? and clientId=? and className=? and classPK=?", params);
	}
			
	
	/**
	 * 查询需要直载的文件--条目ZIP
	 * @param userId
	 * @param clientId
	 * @return
	 * @throws Exception
	 */
	@Override
	public List<SynchronizedLogEntity> findNeedDownloadNoteFile(
			String userId, String clientId) throws Exception {
		DetachedCriteria dc = DetachedCriteria
				.forClass(SynchronizedLogEntity.class);
		dc.add(Restrictions.eq("clientId", clientId));
		dc.add(Restrictions.eq("targetUser", userId));
		dc.add(Restrictions.eq("className", DataType.NOTE.toString()));
		dc.add(Restrictions.or(Restrictions.eq("action", DataSynchAction.ADD.toString()), Restrictions.eqOrIsNull("action", DataSynchAction.UPDATE.toString())));
		dc.add(Restrictions.eq("status", SynchConstants.LOG_NOT_SYNCHRONIZED));
		List<SynchronizedLogEntity> list = findByDetached(dc);

		List<SynchronizedLogEntity> logList = mergeAttaLogs(list, clientId,
				userId);
		return logList;
	}

	/**
	 * 查询需要直载的文件（附件，条目ZIP）
	 * @param userId
	 * @param clientId
	 * @return
	 * @throws Exception
	 */
	@Override
	public List<SynchronizedLogEntity> findNeedDownloadFile(
			String userId, String clientId) throws Exception {
		DetachedCriteria dc = DetachedCriteria
				.forClass(SynchronizedLogEntity.class);
		
		dc.add(Restrictions.or(
				Restrictions.eq("className", DataType.ATTACHMENT.toString()),
				
				Restrictions.eq("className",
						DataType.NOTE.toString())));
		
		dc.add(Restrictions.eq("clientId", clientId));
		dc.add(Restrictions.eq("targetUser", userId));
		dc.add(Restrictions.eq("status", SynchConstants.LOG_NOT_SYNCHRONIZED));
		dc.addOrder(Order.asc("className"));
		dc.addOrder(Order.asc("classPK"));
		dc.addOrder(Order.asc("operateTime"));
		
		List<SynchronizedLogEntity> list = findByDetached(dc);

		List<SynchronizedLogEntity> logList = mergeAttaLogs(list, clientId,
				userId);
		return logList;
	}
	
	public List<SynchronizedLogEntity> mergeAttaLogs(
			List<SynchronizedLogEntity> logList, String clientId, String userId)
			throws Exception {
		List<SynchronizedLogEntity> result = new ArrayList<SynchronizedLogEntity>();
		// 只有一条日志，不需要合并
		if (logList.size() == 1) {
			if(!logList.get(0).getAction().equals(DataSynchAction.TRUNCATE.toString())){
				result.add(logList.get(0));
			}
			return result;
		}
		for (int i = 0; i < logList.size(); i++) {
			SynchronizedLogEntity orgi = logList.get(i);
			String classPK = orgi.getClassPK();
			if (i < logList.size() - 1) {
				for (int k = i + 1; k < logList.size(); k++) {
					SynchronizedLogEntity nextLog = logList.get(k);
					// 判断是否为同一数据日志
					if (nextLog.getClassPK().equals(classPK)) {
						orgi = mergeLog(orgi, nextLog, clientId, userId);
						i++; // 外层循环跳过 nextLog
					}
					// 下一数据日志了或已合并到最后
					if (!nextLog.getClassPK().equals(classPK)
							|| k == logList.size() - 1) {
						if (orgi != null) {
							// 因为是查询需下载的文件日志，所以不要已经删除的
							if(!orgi.getAction().equals(DataSynchAction.TRUNCATE.toString())){
								result.add(orgi);
							}
							break;
						}
					}
				}

			} else { // i == 最后一条记录索引
				// 因为是查询需下载的文件日志，所以不要已经删除的
				if(!orgi.getAction().equals(DataSynchAction.TRUNCATE.toString())){
					result.add(orgi);
				}
			}
		}
		return result;
	}

	public SynchronizedLogEntity mergeLog(SynchronizedLogEntity orgi,
			SynchronizedLogEntity nextLog, String clientId, String userId)
			throws Exception {
		if (orgi == null) {
			return nextLog;
		}
		if (nextLog == null) {
			return orgi;
		}
		if (!orgi.getClassName().equals(nextLog.getClassName())) {
			throw new Exception("合并的两个日志必须日志类型相同！");
		}
		if (!orgi.getClassPK().equals(nextLog.getClassPK())) {
			throw new Exception("合并的两个日志必须属于同一条数据！");
		}

		if (orgi.getAction().equals(DataSynchAction.ADD.toString())) {
			// A + T = null
			if (nextLog.getAction().equals(DataSynchAction.TRUNCATE.toString())) {
				orgi = null;
			}
			// A + U = A
			if (nextLog.getAction().equals(DataSynchAction.UPDATE.toString()) || nextLog.getAction().equals(DataSynchAction.DELETE.toString())) {
				orgi.setClientId(nextLog.getClientId());
				orgi.setTargetUser(nextLog.getTargetUser());
				orgi.setOperateTime(nextLog.getOperateTime());
			}
			if (nextLog.getAction().equals(DataSynchAction.CREATEORUPDATE.toString())) {
				orgi = nextLog;
			}
		} else if (orgi.getAction().equals(DataSynchAction.UPDATE.toString())) {
			// U + D = D
			if (nextLog.getAction().equals(DataSynchAction.DELETE.toString()) || nextLog.getAction().equals(DataSynchAction.TRUNCATE.toString())) {
				orgi = nextLog;
			}
			if (nextLog.getAction().equals(DataSynchAction.CREATEORUPDATE.toString()) || nextLog.getAction().equals(DataSynchAction.RESTORE.toString())) {
				orgi = nextLog;
			}
		} else if (orgi.getAction().equals(DataSynchAction.DELETE.toString())) {
			 if(nextLog.getAction().equals(DataSynchAction.TRUNCATE.toString()) || nextLog.getAction().equals(DataSynchAction.RESTORE.toString()) || nextLog.getAction().equals(DataSynchAction.UPDATE.toString())){
				 orgi = nextLog;
			 }
		}else if (orgi.getAction().equals(DataSynchAction.TRUNCATE.toString())) {
			if(nextLog.getAction().equals(DataSynchAction.TRUNCATE.toString())){
				orgi = nextLog;
			}
			if(nextLog.getAction().equals(DataSynchAction.ADD.toString())){
				orgi = nextLog;
			}
		}
		return orgi;
	}
}
