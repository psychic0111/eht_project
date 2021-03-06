package com.eht.log.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.ArrayUtils;
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
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eht.common.bean.ResponseStatus;
import com.eht.common.constant.SynchConstants;
import com.eht.common.enumeration.DataSynchAction;
import com.eht.common.enumeration.DataType;
import com.eht.group.entity.Group;
import com.eht.group.service.GroupService;
import com.eht.log.entity.SynchLogEntity;
import com.eht.log.entity.SynchronizedLogEntity;
import com.eht.log.service.SynchLogServiceI;
import com.eht.note.service.AttachmentServiceI;
import com.eht.note.service.NoteServiceI;
import com.eht.subject.entity.SubjectEntity;
import com.eht.subject.service.DirectoryServiceI;
import com.eht.subject.service.SubjectServiceI;
import com.eht.system.bean.ClientEntity;
import com.eht.system.service.DataInitService;
import com.eht.user.entity.AccountEntity;
import com.eht.user.service.AccountServiceI;
import com.eht.webservice.service.impl.DataSynchizeServiceImpl;
import com.eht.webservice.util.SynchDataCache;

@Service("synchLogService")
@Transactional
public class SynchLogServiceImpl extends CommonServiceImpl implements SynchLogServiceI {
	
	private Logger logger = Logger.getLogger(SynchLogServiceImpl.class);
	
	@Autowired
	private DataInitService dataInitService;
	
	@Autowired
	private SubjectServiceI subjectService;
	
	@Autowired
	private DirectoryServiceI directoryService;
	
	@Autowired
	private NoteServiceI noteService;
	
	@Autowired
	private AccountServiceI accountService;
	
	@Autowired
	private AttachmentServiceI attachmentService;
	
	@Autowired
	private GroupService groupService;
	
	@Override
	public SynchLogEntity getSynchLog(String logId) {
		return get(SynchLogEntity.class, logId);
	}

	@Override
	public String saveSynchLog(SynchLogEntity log) {
		logger.info("保存操作日志; 数据类型：" + log.getClassName() + ", 数据主键：" + log.getClassPK() + ", 数据操作：" + log.getAction());
		ClientEntity client = null;
		SecurityContext securityContext = SecurityContextHolder.getContext(); 
		if(securityContext != null){
			AccountEntity user = accountService.getUser4Session();
			if(user == null){
				//String sessionId = ContextHolderUtils.getRequest().getParameter("jsessionid");
				String sessionId = ContextHolderUtils.getRequest().getAttribute("jsessionid").toString();
				user = accountService.getUser4Session(sessionId);
			}
			String clientId = user.getClientId();
			if(!StringUtil.isEmpty(clientId)){
				client = dataInitService.getClient(clientId);
			}
		}
		if(client != null){
			log.setClientId(client.getClientId());
			log.setClientType(client.getClientType());
		}else{
			// 默认为WEB客户端
			log.setClientId(SynchConstants.CLIENT_DEFAULT_ID);
			log.setClientType(SynchConstants.CLIENT_DEFAULT_TYPE);
		}
		
		SynchLogEntity oldLog = findLogByData(log.getClassName(), log.getClassPK(), log.getTargetUser(), log.getAction());
		if(oldLog != null){
			logger.info("发现已存在日志; 数据类型：" + oldLog.getClassName() + ", 数据主键：" + oldLog.getClassPK() + ", 数据操作：" + oldLog.getAction());
			if(oldLog.getOperateTime() > log.getOperateTime()){
				logger.info("已有数据操作时间比此次操作日志时间新, 忽略此次日志记录!");
			}else{
				oldLog.setOperateUser(log.getOperateUser());
				oldLog.setOperateTime(log.getOperateTime());
				oldLog.setSynchTime(System.currentTimeMillis());
				oldLog.setClientType(log.getClientType());
				oldLog.setClientId(log.getClientId());
				updateEntitie(oldLog);
			}
		}else{
			save(log);
		}
		return new ResponseStatus().toString();
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
	public SynchLogEntity findLogByData(String dataClass, String dataKey, String userId, String action) {
		DetachedCriteria dc = DetachedCriteria.forClass(SynchLogEntity.class);
		dc.add(Restrictions.eq("className", dataClass));
		dc.add(Restrictions.eq("classPK", dataKey));
		dc.add(Restrictions.eq("targetUser", userId));
		dc.add(Restrictions.eq("action", action));
		List<SynchLogEntity> list = findByDetached(dc);
		if(list != null && !list.isEmpty()){
			return list.get(0);
		}
		return null;
	}
	
	/**
	 * 查询某一数据的最近操作日志
	 * @throws Exception 
	 */
	@Override
	public SynchLogEntity findLogByData(String clientId, String userId, long timeStamp, String dataClass, String dataKey) throws Exception {
		DetachedCriteria dc = DetachedCriteria.forClass(SynchLogEntity.class);
		
		List<String> idList = findSynchedLogIds(clientId, userId);
		if(idList != null && !idList.isEmpty()){
			dc.add(Restrictions.not(Restrictions.in("id", idList)));     //不包括已经同步过的日志
		}
		dc.add(Restrictions.gt("synchTime", timeStamp));
		dc.add(Restrictions.or(Restrictions.and(Restrictions.eq("targetUser", userId), Restrictions.neProperty("operateUser", "targetUser")), Restrictions.and(Restrictions.eq("operateUser", userId), Restrictions.ne("clientId", clientId))));
		dc.add(Restrictions.eq("className", dataClass));
		dc.add(Restrictions.eq("classPK", dataKey));
		dc.addOrder(Order.asc("operateTime"));
		
		List<SynchLogEntity> subLogList = findByDetached(dc);
		SynchLogEntity theLog = null;
		if(subLogList != null && !subLogList.isEmpty()){
			for(SynchLogEntity log : subLogList){
				saveSynchedLog(log, clientId, userId);   //保存到同步完成日志表中
				//日志合并
				if(theLog == null){
					theLog = log;
				}else{
					theLog = mergeLog(theLog, log, clientId, userId);
				}
			}
		}
		return theLog;
	}
	
	/**
	 * 根据操作合并两个日志
	 * @param orgi
	 * @param nextLog
	 * @return
	 * @throws Exception 
	 */
	@Override
	public SynchLogEntity mergeLog(SynchLogEntity orgi, SynchLogEntity nextLog, String clientId, String userId) throws Exception{
		if(orgi == null) {
			return nextLog;
		}
		if(nextLog == null){
			return orgi;
		}
		if(!orgi.getClassName().equals(nextLog.getClassName())){
			throw new Exception("合并的两个日志必须日志类型相同！");
		}
		if(!orgi.getClassPK().equals(nextLog.getClassPK())){
			throw new Exception("合并的两个日志必须属于同一条数据！");
		}
		
		if(orgi.getAction().equals(DataSynchAction.ADD.toString())){
			// A + D = null
			if(nextLog.getAction().equals(DataSynchAction.DELETE.toString())){
				orgi = null;
			}
			// A + U = A
			if(nextLog.getAction().equals(DataSynchAction.UPDATE.toString())){
				orgi = nextLog;
				orgi.setAction(DataSynchAction.ADD.toString());
			}
		}else if(orgi.getAction().equals(DataSynchAction.UPDATE.toString())){
			// U + D = D
			if(nextLog.getAction().equals(DataSynchAction.DELETE.toString())){
				orgi = nextLog;
			}
		}else if(orgi.getAction().equals(DataSynchAction.DELETE.toString())){
			// 数据已删除
		}
		//saveSynchedLog(orgi, clientId, userId);  //保存到同步完成日志表中
		//saveSynchedLog(nextLog, clientId, userId);  //保存到同步完成日志表中
		return orgi;
	}
		
	@Override
	public int countLogByData(String clientId, String userId, long timeStamp, String dataClass, String dataKey) {
		DetachedCriteria dc = DetachedCriteria.forClass(SynchLogEntity.class);
		
		List<String> idList = findSynchedLogIds(clientId, userId);
		if(idList != null && !idList.isEmpty()){
			dc.add(Restrictions.not(Restrictions.in("id", idList)));     //不包括已经同步过的日志
		}
		dc.add(Restrictions.gt("synchTime", timeStamp));
		dc.add(Restrictions.or(Restrictions.and(Restrictions.eq("targetUser", userId), Restrictions.neProperty("operateUser", "targetUser")), Restrictions.and(Restrictions.eq("operateUser", userId), Restrictions.ne("clientId", clientId))));
		dc.add(Restrictions.eq("className", dataClass));
		dc.add(Restrictions.eq("classPK", dataKey));
		
		int count = oConvertUtils.getInt((dc.getExecutableCriteria(getSession())
				.setProjection(Projections.rowCount())).uniqueResult(), 0);
		return count;
	}
	
	/**
	 * 查询条目下附件操作日志
	 * @param clientId
	 * @param userId
	 * @param timeStamp
	 * @param dataClass
	 * @param dataKey
	 * @return
	 */
	@Override
	public SynchLogEntity findAttachmentLogByNote(String clientId, String userId, long timeStamp, String noteId) {
		DetachedCriteria dc = DetachedCriteria.forClass(SynchLogEntity.class);
		List<String> attaIds = attachmentService.findAttaIdsByNote(noteId, null);
		List<String> idList = findSynchedLogIds(clientId, userId);
		if(idList != null && !idList.isEmpty()){
			dc.add(Restrictions.not(Restrictions.in("id", idList)));     //不包括已经同步过的日志
		}
		if(attaIds != null && !attaIds.isEmpty()){
			dc.add(Restrictions.in("classPK", attaIds));
		}
		dc.add(Restrictions.gt("synchTime", timeStamp));
		dc.add(Restrictions.or(Restrictions.and(Restrictions.eq("targetUser", userId), Restrictions.neProperty("operateUser", "targetUser")), Restrictions.and(Restrictions.eq("operateUser", userId), Restrictions.ne("clientId", clientId))));
		dc.add(Restrictions.eq("className", DataType.ATTACHMENT.toString()));
		dc.addOrder(Order.desc("operateTime"));
		
		List<SynchLogEntity> subLogList = findByDetached(dc);
		
		if(subLogList != null && !subLogList.isEmpty()){
			for(SynchLogEntity log : subLogList){
				SynchronizedLogEntity sLog = new SynchronizedLogEntity();
				sLog.setClientId(clientId);
				sLog.setLogId(log.getId());
				sLog.setOperateTime(System.currentTimeMillis());
				sLog.setTargetUser(userId);
				save(sLog);   //保存到同步完成日志表中
			}
			return subLogList.get(0);
		}
		return null;
	}
	
	/**
	 * 查询条目下附件操作日志数量
	 * @param clientId
	 * @param userId
	 * @param timeStamp
	 * @param dataClass
	 * @param dataKey
	 * @return
	 */
	@Override
	public int countAttachmentLogByNote(String clientId, String userId, long timeStamp, String noteId) {
		DetachedCriteria dc = DetachedCriteria.forClass(SynchLogEntity.class);
		List<String> attaIds = attachmentService.findAttaIdsByNote(noteId, null);
		List<String> idList = findSynchedLogIds(clientId, userId);
		if(idList != null && !idList.isEmpty()){
			dc.add(Restrictions.not(Restrictions.in("id", idList)));     //不包括已经同步过的日志
		}
		if(attaIds != null && !attaIds.isEmpty()){
			dc.add(Restrictions.in("classPK", attaIds));
		}
		dc.add(Restrictions.gt("synchTime", timeStamp));
		dc.add(Restrictions.or(Restrictions.and(Restrictions.eq("targetUser", userId), Restrictions.neProperty("operateUser", "targetUser")), Restrictions.and(Restrictions.eq("operateUser", userId), Restrictions.ne("clientId", clientId))));
		dc.add(Restrictions.eq("className", DataType.ATTACHMENT.toString()));
		int count = oConvertUtils.getInt((dc.getExecutableCriteria(getSession())
				.setProjection(Projections.rowCount())).uniqueResult(), 0);
		return count;
	}
	
	/**
	 * 查询专题成员日志
	 * @param clientId
	 * @param userId
	 * @param timeStamp
	 * @param subjectId
	 * @return
	 */
	@Override
	public SynchLogEntity findSubjectUserLogs(String clientId, String userId, long timeStamp, String subjectId) {
		DetachedCriteria dc = DetachedCriteria.forClass(SynchLogEntity.class);
		List<String> idList = findSynchedLogIds(clientId, userId);
		if(idList != null && !idList.isEmpty()){
			dc.add(Restrictions.not(Restrictions.in("id", idList)));     //不包括已经同步过的日志
		}
		dc.add(Restrictions.gt("synchTime", timeStamp));
		//dc.add(Restrictions.or(Restrictions.and(Restrictions.eq("targetUser", userId), Restrictions.neProperty("operateUser", "targetUser")), Restrictions.and(Restrictions.eq("operateUser", userId), Restrictions.ne("clientId", clientId))));
		dc.add(Restrictions.ne("clientId", clientId));
		dc.add(Restrictions.eq("className", DataType.SUBJECTUSER.toString()));
		dc.add(Restrictions.eq("classPK", subjectId));
		dc.addOrder(Order.desc("operateTime"));
		
		List<SynchLogEntity> subLogList = pageList(dc, 0, SynchConstants.RETURN_CLIENT_MAX_COUNT);
		
		if(subLogList != null && !subLogList.isEmpty()){
			for(SynchLogEntity log : subLogList){
				SynchronizedLogEntity sLog = new SynchronizedLogEntity();
				sLog.setClientId(clientId);
				sLog.setLogId(log.getId());
				sLog.setOperateTime(System.currentTimeMillis());
				sLog.setTargetUser(userId);
				save(sLog);   //保存到同步完成日志表中
			}
			return subLogList.get(0);
		}
		return null;
	}
	
	/**
	 * 查询专题成员日志数量
	 * @param clientId
	 * @param userId
	 * @param timeStamp
	 * @param subjectId
	 * @return
	 */
	@Override
	public int countSubjectUserLogs(String clientId, String userId, long timeStamp, String subjectId) {
		DetachedCriteria dc = DetachedCriteria.forClass(SynchLogEntity.class);
		List<String> idList = findSynchedLogIds(clientId, userId);
		if(idList != null && !idList.isEmpty()){
			dc.add(Restrictions.not(Restrictions.in("id", idList)));     //不包括已经同步过的日志
		}
		dc.add(Restrictions.gt("synchTime", timeStamp));
		//dc.add(Restrictions.or(Restrictions.and(Restrictions.eq("targetUser", userId), Restrictions.neProperty("operateUser", "targetUser")), Restrictions.and(Restrictions.eq("operateUser", userId), Restrictions.ne("clientId", clientId))));
		dc.add(Restrictions.ne("clientId", clientId));
		dc.add(Restrictions.eq("className", DataType.SUBJECTUSER.toString()));
		dc.add(Restrictions.eq("classPK", subjectId));
		dc.addOrder(Order.desc("operateTime"));
		
		int count = oConvertUtils.getInt((dc.getExecutableCriteria(getSession())
				.setProjection(Projections.rowCount())).uniqueResult(), 0);
		return count;
	}
	
	/**
	 * 查询某专题下数据的日志
	 * @param clientId
	 * @param userId
	 * @param timeStamp
	 * @param subjectId
	 * @return
	 */
	@Override
	public SynchLogEntity findSubjectRelatedLogs(String clientId, String userId, long timeStamp, String subjectId) {
		Group group = groupService.findGroup(SubjectEntity.class.getName(), subjectId);
		//所有该专题下的数据集合，不包括附件
		List<Group> list = groupService.findGroupByParent(group.getGroupId());
		if(list != null && !list.isEmpty()){
			List<String> keysList = new ArrayList<String>();
			for(Group g : list){
				keysList.add(g.getClassPk());
			}
			
			DetachedCriteria dc = DetachedCriteria.forClass(SynchLogEntity.class);
			List<String> idList = findSynchedLogIds(clientId, userId);
			if(idList != null && !idList.isEmpty()){
				dc.add(Restrictions.not(Restrictions.in("id", idList)));     //不包括已经同步过的日志
			}
			dc.add(Restrictions.gt("synchTime", timeStamp));
			//dc.add(Restrictions.or(Restrictions.and(Restrictions.eq("targetUser", userId), Restrictions.neProperty("operateUser", "targetUser")), Restrictions.and(Restrictions.eq("operateUser", userId), Restrictions.ne("clientId", clientId))));
			dc.add(Restrictions.ne("clientId", clientId));
			dc.add(Restrictions.in("classPK", keysList));
			dc.addOrder(Order.desc("operateTime"));
			
			List<SynchLogEntity> subLogList = pageList(dc, 0, SynchConstants.RETURN_CLIENT_MAX_COUNT);
			
			if(subLogList != null && !subLogList.isEmpty()){
				for(SynchLogEntity log : subLogList){
					SynchronizedLogEntity sLog = new SynchronizedLogEntity();
					sLog.setClientId(clientId);
					sLog.setLogId(log.getId());
					sLog.setOperateTime(System.currentTimeMillis());
					sLog.setTargetUser(userId);
					save(sLog);   //保存到同步完成日志表中
				}
				return subLogList.get(0);
			}
		}
		return null;
	}
	
	@Override
	public int countSubjectRelatedLogs(String clientId, String userId, long timeStamp, String subjectId) {
		Group group = groupService.findGroup(SubjectEntity.class.getName(), subjectId);
		int count = 0;
		//所有该专题下的数据集合，不包括附件
		List<Group> list = groupService.findGroupByParent(group.getGroupId());
		if(list != null && !list.isEmpty()){
			List<String> keysList = new ArrayList<String>();
			for(Group g : list){
				keysList.add(g.getClassPk());
			}
			
			DetachedCriteria dc = DetachedCriteria.forClass(SynchLogEntity.class);
			List<String> idList = findSynchedLogIds(clientId, userId);
			if(idList != null && !idList.isEmpty()){
				dc.add(Restrictions.not(Restrictions.in("id", idList)));     //不包括已经同步过的日志
			}
			dc.add(Restrictions.gt("synchTime", timeStamp));
			//dc.add(Restrictions.or(Restrictions.and(Restrictions.eq("targetUser", userId), Restrictions.neProperty("operateUser", "targetUser")), Restrictions.and(Restrictions.eq("operateUser", userId), Restrictions.ne("clientId", clientId))));
			dc.add(Restrictions.ne("clientId", clientId));
			dc.add(Restrictions.in("classPK", keysList));
			
			count = oConvertUtils.getInt((dc.getExecutableCriteria(getSession())
					.setProjection(Projections.rowCount())).uniqueResult(), 0);
		}
		return count;
	}
	
	/**
	 * 查询某一类型数据日志
	 * @param dataClass
	 * @return
	 */
	public List<SynchLogEntity> findLogsByDataClass(String dataClass){
		DetachedCriteria dc = DetachedCriteria.forClass(SynchLogEntity.class);
		dc.add(Restrictions.eq("className", dataClass));
		List<SynchLogEntity> list = findByDetached(dc);
		return list;
	}
	
	public List<SynchLogEntity> findLogsByDataKeys(String dataClass, Collection<String> dataKeys, long timeStamp){
		DetachedCriteria dc = DetachedCriteria.forClass(SynchLogEntity.class);
		dc.add(Restrictions.gt("synchTime", timeStamp));
		dc.add(Restrictions.eq("className", dataClass));
		dc.add(Restrictions.in("classPK", dataKeys));
		List<SynchLogEntity> list = findByDetached(dc);
		return list;
	}
	
	public List<SynchLogEntity> findLogsByDataKeys(String dataClass, String[] dataKeys, long timeStamp){
		DetachedCriteria dc = DetachedCriteria.forClass(SynchLogEntity.class);
		dc.add(Restrictions.gt("synchTime", timeStamp));
		dc.add(Restrictions.eq("className", dataClass));
		dc.add(Restrictions.in("classPK", dataKeys));
		List<SynchLogEntity> list = findByDetached(dc);
		return list;
	}
	
	@Override
	public List<SynchLogEntity> findSynchLogsByTarget(String clientId, String userId, long timeStamp, int offset) {
		DetachedCriteria dc = DetachedCriteria.forClass(SynchLogEntity.class);
		dc.add(Restrictions.gt("synchTime", timeStamp));
		dc.add(Restrictions.or(Restrictions.and(Restrictions.eq("targetUser", userId), Restrictions.neProperty("operateUser", "targetUser")), Restrictions.and(Restrictions.eq("operateUser", userId), Restrictions.ne("clientId", clientId))));
		dc.addOrder(Order.asc("operateTime"));
		
		List<SynchLogEntity> subLogList = pageList(dc, offset, SynchConstants.RETURN_CLIENT_MAX_COUNT);
		
		return subLogList;
	}
	
	@Override
	public int countSynchLogsByTarget(String clientId, String userId, long timeStamp) {
		DetachedCriteria dc = DetachedCriteria.forClass(SynchLogEntity.class);
		dc.add(Restrictions.gt("synchTime", timeStamp));
		
		dc.add(Restrictions.or(Restrictions.and(Restrictions.eq("targetUser", userId), Restrictions.neProperty("operateUser", "targetUser")), Restrictions.and(Restrictions.eq("operateUser", userId), Restrictions.ne("clientId", clientId))));
		int count = oConvertUtils.getInt((dc.getExecutableCriteria(getSession())
				.setProjection(Projections.rowCount())).uniqueResult(), 0);
		return count;
	}
	
	/**
	 * 返回客户端日志查询方法
	 */
	@Override
	public List<SynchLogEntity> findSynchLogsByTarget(String clientId, String userId, long timeStamp, String action, String dataClass, int offset) {
		DetachedCriteria dc = DetachedCriteria.forClass(SynchLogEntity.class);
		dc.add(Restrictions.or(Restrictions.and(Restrictions.eq("targetUser", userId), Restrictions.neProperty("operateUser", "targetUser")), Restrictions.and(Restrictions.eq("operateUser", userId), Restrictions.ne("clientId", clientId))));
		//dc.add(Restrictions.eq("clientId", SynchConstants.CLIENT_DEFAULT_ID));   // web页面操作产生的操作日志
		List<String> idList = findSynchedLogIds(clientId, userId);
		if(idList != null && !idList.isEmpty()){
			dc.add(Restrictions.not(Restrictions.in("id", idList)));     //不包括已经同步过的日志
		}
		dc.add(Restrictions.gt("synchTime", timeStamp));
		dc.add(Restrictions.eq("action", action));
		dc.add(Restrictions.eq("className", dataClass));
		dc.addOrder(Order.asc("operateTime"));
		
		List<SynchLogEntity> subLogList = pageList(dc, offset, SynchConstants.RETURN_CLIENT_MAX_COUNT);
		
		return subLogList;
	}
	
	@Override
	public int countSynchLogsByTarget(String clientId, String userId, long timeStamp, String dataClass) {
		DetachedCriteria dc = DetachedCriteria.forClass(SynchLogEntity.class);
		dc.add(Restrictions.or(Restrictions.and(Restrictions.eq("targetUser", userId), Restrictions.neProperty("operateUser", "targetUser")), Restrictions.and(Restrictions.eq("operateUser", userId), Restrictions.ne("clientId", clientId))));
		List<String> idList = findSynchedLogIds(clientId, userId);
		if(idList != null && !idList.isEmpty()){
			dc.add(Restrictions.not(Restrictions.in("id", idList)));     //不包括已经同步过的日志
		}
		dc.add(Restrictions.or(Restrictions.and(Restrictions.eq("targetUser", userId), Restrictions.neProperty("operateUser", "targetUser")), Restrictions.and(Restrictions.eq("operateUser", userId), Restrictions.ne("clientId", clientId))));
		dc.add(Restrictions.gt("synchTime", timeStamp));
		dc.add(Restrictions.eq("className", dataClass));
		int count = oConvertUtils.getInt((dc.getExecutableCriteria(getSession())
				.setProjection(Projections.rowCount())).uniqueResult(), 0);
		return count;
	}
	
	/**
	 * 返回客户端日志查询方法
	 * @throws Exception 
	 */
	@Override
	public List<SynchLogEntity> findSynchLogsByTarget(String clientId, String userId, long timeStamp, String dataClass, boolean filterDelete) throws Exception {
		String[] dataTypes = SynchDataCache.getDatasSort();
		if(!filterDelete){
			dataTypes = SynchDataCache.getReverseDatasSort();
		}
		//dc.add(Restrictions.eq("clientId", SynchConstants.CLIENT_DEFAULT_ID));   // web页面操作产生的操作日志
		List<String> idList = findSynchedLogIds(clientId, userId);
		List<SynchLogEntity> logList = null;
		
		if(dataClass.equals(DataType.ALL.toString()) || dataClass.equals(DataType.BATCHDATA.toString())){
			for(int i = 0; i < dataTypes.length; i++){
				DetachedCriteria dc = DetachedCriteria.forClass(SynchLogEntity.class);
				dc.add(Restrictions.or(Restrictions.and(Restrictions.eq("targetUser", userId), Restrictions.neProperty("operateUser", "targetUser")), Restrictions.and(Restrictions.eq("operateUser", userId), Restrictions.ne("clientId", clientId))));
				dc.add(Restrictions.eq("className", dataTypes[i]));
				if(idList != null && !idList.isEmpty()){
					dc.add(Restrictions.not(Restrictions.in("id", idList)));     //不包括已经同步过的日志
				}
				dc.add(Restrictions.gt("synchTime", timeStamp));
				dc.addOrder(Order.asc("classPK"));
				dc.addOrder(Order.asc("operateTime"));
				List<SynchLogEntity> subLogList = findByDetached(dc);
				
				if(subLogList != null && !subLogList.isEmpty()){
					subLogList = filterSynchLogs(subLogList, filterDelete); // 过滤日志，例如先不处理删除操作的日志就先过滤掉
					if(subLogList != null && !subLogList.isEmpty()){
						if(!filterDelete){
							//删除操作的日志，一次合并一个数据类型下的数据，一起返回
							logList = mergeAllLogs(subLogList, clientId, userId);
						}else{
							logList = mergeLog(subLogList, clientId, userId);
						}
						// 有日志数据返回
						if(logList != null && !logList.isEmpty()){
							break;
						}
					}
				}
			}
		}else {
			DetachedCriteria dc = DetachedCriteria.forClass(SynchLogEntity.class);
			dc.add(Restrictions.or(Restrictions.and(Restrictions.eq("targetUser", userId), Restrictions.neProperty("operateUser", "targetUser")), Restrictions.and(Restrictions.eq("operateUser", userId), Restrictions.ne("clientId", clientId))));
			if(idList != null && !idList.isEmpty()){
				dc.add(Restrictions.not(Restrictions.in("id", idList)));     //不包括已经同步过的日志
			}
			dc.add(Restrictions.gt("synchTime", timeStamp));
			dc.add(Restrictions.eq("className", dataClass));
			dc.addOrder(Order.asc("classPK"));
			dc.addOrder(Order.asc("operateTime"));
			List<SynchLogEntity> subLogList = findByDetached(dc);
			if(subLogList != null && !subLogList.isEmpty()){
				subLogList = filterSynchLogs(subLogList, filterDelete);   // 过滤日志，例如先不处理删除操作的日志就先过滤掉
				if(subLogList != null && !subLogList.isEmpty()){
					if(!filterDelete){ //删除操作的日志，一次全部合并返回
						logList = mergeAllLogs(subLogList, clientId, userId);
					}else{  // 其它日志只返回指定条数
						logList = mergeLog(subLogList, clientId, userId);
					}
				}
			}
		}
		return logList;
		
	}
	
	/**
	 * 返回客户端日志查询方法
	 * @throws Exception 
	 */
	@Override
	public List<SynchLogEntity> findSynchLogsBySQL(String clientId, String userId, long timeStamp, String dataClass) throws Exception {
		String[] dataTypes = SynchDataCache.getDatasSort();
		
		//dc.add(Restrictions.eq("clientId", SynchConstants.CLIENT_DEFAULT_ID));   // web页面操作产生的操作日志
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
		if(idList != null && !idList.isEmpty()){
			sb.append("and t.id not in(");
			for(String id : idList){
				sb.append("'"+id+"',");
			}
			sb.setLength(sb.length() - 1);
			sb.append(") ");
		}
		sb.append("and t.synchTime>? ");
		sb.append("and t.className=? ");
		
		sb.append(") tt ");
		sb.append("GROUP BY tt.classpk ");
		sb.append("ORDER BY SUM(tt.actionvalue) DESC,tt.synchTime,tt.classpk");
		if(dataClass.equals(DataType.ALL.toString())){
			for(int i = 0; i < dataTypes.length; i++){
				Object[] args = new Object[]{userId, userId, clientId, timeStamp, dataTypes[i]};
				resultList = findForJdbc(sb.toString(), args);
			}
		}else {
			Object[] args = new Object[]{userId, userId, clientId, timeStamp, dataClass};
			resultList = findForJdbc(sb.toString(), args);
		}
		return logList;
		
	}
	
	/**
	 * 过滤集合中删除操作类型日志或过滤非删除日志
	 * @param logList
	 * @param filterDelete 是否是过滤删除操作类型日志
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private List<SynchLogEntity> filterSynchLogs(List<SynchLogEntity> logList, boolean filterDelete) throws Exception {
		//所有删除操作日志集合
		List<SynchLogEntity> filterList = new ArrayList<SynchLogEntity>();
		for(SynchLogEntity log : logList) {
			if(log.getAction().equals(DataSynchAction.DELETE.toString())){
				filterList.add(log);
			}
		}
		
		List<SynchLogEntity> result = new ArrayList<SynchLogEntity>();
		if(filterList != null && !filterList.isEmpty()){
			for(SynchLogEntity l : logList) {
				boolean equal = false;
				for(SynchLogEntity log : filterList) {
					if(l.getClassName().equals(log.getClassName()) && l.getClassPK().equals(log.getClassPK())){
						equal = true;
						break;
					}
				}
				if(equal){
					result.add(l);  // 将所有有删除操作的日志添加进result
				}
			}
			// 如果要过滤掉删除操作日志
			if(filterDelete){
				return ListUtils.subtract(logList, result);
			}else{
				return result;
			}
			
		}else{
			if(filterDelete){
				return logList;
			}
		}
		return result;
	}
	
	/**
	 * 合并集合中的日志,返回客户端一条
	 * @throws Exception 
	 */
	private List<SynchLogEntity> mergeLog(List<SynchLogEntity> logList, String clientId, String userId) throws Exception {
		if(logList.size() == SynchConstants.RETURN_CLIENT_MAX_COUNT){
			saveSynchedLog(logList.get(0), clientId, userId);
			return logList;
		}
		boolean findedLog = false;
		List<SynchLogEntity> result = new ArrayList<SynchLogEntity>();
		for(int i = 0; i < logList.size(); i++) {
			if(result.size() == SynchConstants.RETURN_CLIENT_MAX_COUNT + 1){
				break;
			}
			SynchLogEntity orgi = logList.get(i);
			String classPK = orgi.getClassPK();
			if(!findedLog){
				saveSynchedLog(orgi, clientId, userId);
			}else{
				result.add(orgi);  //此日志用来确定客户下一次请求哪种类型日志
				break;
			}
			if(i < logList.size() - 1){
				for(int k = i + 1; k < logList.size(); k++){
					SynchLogEntity nextLog = logList.get(k);
					//判断是否为同一数据日志
					if(nextLog.getClassPK().equals(classPK)){
						orgi = mergeLog(orgi, nextLog, clientId, userId);
						if(!findedLog){
							saveSynchedLog(nextLog, clientId, userId);
						}
						i ++;  // 外层循环跳过 nextLog
					}
					// 下一数据日志了或已合并到最后
					if(!nextLog.getClassPK().equals(classPK) || k == logList.size() - 1) {
						if(orgi != null && result.size() <= SynchConstants.RETURN_CLIENT_MAX_COUNT){
							result.add(orgi);
							findedLog = true;  //已经找到要返回客户端的日志
							break;
						}
					}
				}
				
			}
		}
		return result;
	}
	
	@Override
	public List<SynchLogEntity> mergeAllLogs(List<SynchLogEntity> logList, String clientId, String userId) throws Exception {
		List<SynchLogEntity> result = new ArrayList<SynchLogEntity>();
		// 只有一条日志，不需要合并
		if(logList.size() == 1){
			result.add(logList.get(0));
			return result;
		}
		for(int i = 0; i < logList.size(); i++) {
			SynchLogEntity orgi = logList.get(i);
			String classPK = orgi.getClassPK();
			saveSynchedLog(orgi, clientId, userId);
			if(i < logList.size() - 1){
				for(int k = i + 1; k < logList.size(); k++){
					SynchLogEntity nextLog = logList.get(k);
					//判断是否为同一数据日志
					if(nextLog.getClassPK().equals(classPK)){
						orgi = mergeLog(orgi, nextLog, clientId, userId);
						saveSynchedLog(nextLog, clientId, userId);
						i ++;  // 外层循环跳过 nextLog
					}
					// 下一数据日志了或已合并到最后
					if(!nextLog.getClassPK().equals(classPK) || k == logList.size() - 1) {
						if(orgi != null){
							result.add(orgi);
							break;
						}
					}
				}
				
			}else{ //i == 最后一条记录索引
				result.add(orgi);
			}
		}
		return result;
	}
	
	@Override
	public List<String> findSynchedLogIds(String clientId, String userId){
		String sql = "select logId from eht_usedlog where clientId='"+ clientId +"' and targetUser='"+ userId +"'";
		List<String> list = findListbySql(sql);
		return list;
	}
	@Override
	public void saveSynchedLog(SynchLogEntity theLog, String clientId, String userId){
		SynchronizedLogEntity sLog = new SynchronizedLogEntity();
		sLog.setClientId(clientId);
		sLog.setLogId(theLog.getId());
		sLog.setOperateTime(theLog.getSynchTime());
		sLog.setTargetUser(userId);
		sLog.setClassName(theLog.getClassName());
		sLog.setClassPK(theLog.getClassPK());
		sLog.setAction(theLog.getAction());
		saveOrUpdate(sLog);   //保存到同步完成日志表中
	}
	
	@Override
	public long deleteSynchedLogs(String clientId, String userId) {
		List<SynchronizedLogEntity> list = findSynchedLogs(clientId, userId);
		if(list != null && !list.isEmpty()){
			deleteAllEntitie(list);
			return list.get(0).getOperateTime();
		}
		return 0;
	}
	
	@Override
	public List<SynchronizedLogEntity> findSynchedLogs(String clientId, String userId) {
		DetachedCriteria dc = DetachedCriteria.forClass(SynchronizedLogEntity.class);
		dc.add(Restrictions.eq("clientId", clientId));
		dc.add(Restrictions.eq("targetUser", userId));
		dc.addOrder(Order.desc("operateTime"));
		List<SynchronizedLogEntity> list = findByDetached(dc);
		
		return list;
	}
	
	/**
	 * 从集合中移除日志或从集合中挑选日志
	 * @param logList
	 * @param className
	 * @param classPk
	 * @param remove   true为移除, false保留
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<SynchLogEntity> filterLogsByClassPK(List<SynchLogEntity> logList, String className, String classPk, boolean remove){
		List<SynchLogEntity> list = new ArrayList<SynchLogEntity>();
		for(SynchLogEntity log : logList){
			if(!log.getClassName().equals(className) || !log.getClassPK().equals(classPk)){
				list.add(log);
			}
		}
		if(remove){
			return list;
		}else{
			return ListUtils.subtract(logList, list);
		}
	}

	@Override
	public List<SynchronizedLogEntity> findNeedDownloadAttachment(String userId,
			String clientId) throws Exception {
		DetachedCriteria dc = DetachedCriteria.forClass(SynchronizedLogEntity.class);
		dc.add(Restrictions.eq("clientId", clientId));
		dc.add(Restrictions.eq("targetUser", userId));
		dc.add(Restrictions.eq("className", DataType.ATTACHMENT.toString()));
		dc.add(Restrictions.eq("action", DataSynchAction.ADD.toString()));
		dc.add(Restrictions.eq("status", SynchConstants.LOG_NOT_SYNCHRONIZED));
		List<SynchronizedLogEntity> list = findByDetached(dc);
		
		List<SynchronizedLogEntity> logList = mergeAttaLogs(list, clientId, userId);
		return logList;
	}
	
	public List<SynchronizedLogEntity> mergeAttaLogs(List<SynchronizedLogEntity> logList, String clientId, String userId) throws Exception {
		List<SynchronizedLogEntity> result = new ArrayList<SynchronizedLogEntity>();
		// 只有一条日志，不需要合并
		if(logList.size() == 1){
			result.add(logList.get(0));
			return result;
		}
		for(int i = 0; i < logList.size(); i++) {
			SynchronizedLogEntity orgi = logList.get(i);
			String classPK = orgi.getClassPK();
			if(i < logList.size() - 1){
				for(int k = i + 1; k < logList.size(); k++){
					SynchronizedLogEntity nextLog = logList.get(k);
					//判断是否为同一数据日志
					if(nextLog.getClassPK().equals(classPK)){
						orgi = mergeLog(orgi, nextLog, clientId, userId);
						i ++;  // 外层循环跳过 nextLog
					}
					// 下一数据日志了或已合并到最后
					if(!nextLog.getClassPK().equals(classPK) || k == logList.size() - 1) {
						if(orgi != null){
							result.add(orgi);
							break;
						}
					}
				}
				
			}else{ //i == 最后一条记录索引
				result.add(orgi);
			}
		}
		return result;
	}
	
	public SynchronizedLogEntity mergeLog(SynchronizedLogEntity orgi, SynchronizedLogEntity nextLog, String clientId, String userId) throws Exception{
		if(orgi == null) {
			return nextLog;
		}
		if(nextLog == null){
			return orgi;
		}
		if(!orgi.getClassName().equals(nextLog.getClassName())){
			throw new Exception("合并的两个日志必须日志类型相同！");
		}
		if(!orgi.getClassPK().equals(nextLog.getClassPK())){
			throw new Exception("合并的两个日志必须属于同一条数据！");
		}
		
		if(orgi.getAction().equals(DataSynchAction.ADD.toString())){
			// A + D = null
			if(nextLog.getAction().equals(DataSynchAction.DELETE.toString())){
				orgi = null;
			}
			// A + U = A
			if(nextLog.getAction().equals(DataSynchAction.UPDATE.toString())){
				orgi = nextLog;
				orgi.setAction(DataSynchAction.ADD.toString());
			}
		}else if(orgi.getAction().equals(DataSynchAction.UPDATE.toString())){
			// U + D = D
			if(nextLog.getAction().equals(DataSynchAction.DELETE.toString())){
				orgi = nextLog;
			}
		}else if(orgi.getAction().equals(DataSynchAction.DELETE.toString())){
			// 数据已删除
		}
		return orgi;
	}
}
