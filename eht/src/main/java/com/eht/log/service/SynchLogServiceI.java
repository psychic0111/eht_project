package com.eht.log.service;

import java.util.List;

import org.jeecgframework.core.common.service.CommonService;

import com.eht.log.entity.SynchLogEntity;
import com.eht.log.entity.SynchronizedLogEntity;

public interface SynchLogServiceI extends CommonService{
	
	/**
	 * 查询同步日志
	 * @param logId
	 * @return
	 */
	public SynchLogEntity getSynchLog(String logId);
	
	/**
	 * 根据类型、ID查询同步日志
	 * @param dataClass
	 * @param dataKey
	 * @return
	 */
	public List<SynchLogEntity> findLogByData(String dataClass, String dataKey);
	
	/**
	 * 根据类型、ID、用户ID、操作查询同步日志
	 * @param dataClass
	 * @param dataKey
	 * @param userId
	 * @param action
	 * @return
	 */
	public SynchLogEntity findLogByData(String dataClass, String dataKey, String userId, String action);
	
	/**
	 * 保存操作日志
	 * @param log
	 * @return 
	 */
	public String saveSynchLog(SynchLogEntity log);
	
	/**
	 * 按照时间戳顺序查询用户需同步日志
	 * @return
	 */
	public List<SynchLogEntity> findSynchLogsByTarget(String clientId, String userId, long timeStamp, int offset);

	/**
	 * 按照操作和数据类型顺序查询用户需同步日志
	 * @return
	 */
	public List<SynchLogEntity> findSynchLogsByTarget(String clientId, String userId, long timeStamp, String action, String dataClass, int offset);
	/**
	 * 查询用户需同步日志数量
	 * @param clientId
	 * @param userId
	 * @param timeStamp
	 * @return
	 */
	public int countSynchLogsByTarget(String clientId, String userId, long timeStamp);

	/**
	 * 客户端某用户对某条数据需更新的日志记录
	 * @param clientId
	 * @param userId
	 * @param timeStamp
	 * @param dataClass
	 * @param dataKey
	 * @return
	 * @throws Exception 
	 */
	public SynchLogEntity findLogByData(String clientId, String userId,	long timeStamp, String dataClass, String dataKey) throws Exception;
	
	/**
	 * 查询已同步过的日志ID
	 * @param clientId
	 * @param userId
	 * @return
	 */
	public List<String> findSynchedLogIds(String clientId, String userId);
	
	/**
	 * 从中间表中删除已经同步过的日志记录,返回最新的时间戳
	 * @param clientId
	 * @param userId
	 */
	public long deleteSynchedLogs(String clientId, String userId);
	
	/**
	 * 查询某一数据日志数量
	 * @param clientId
	 * @param userId
	 * @param timeStamp
	 * @param dataClass
	 * @param dataKey
	 * @return
	 */
	public int countLogByData(String clientId, String userId, long timeStamp, String dataClass, String dataKey);

	/**
	 * 查询条目下附件日志
	 * @param clientId
	 * @param userId
	 * @param timeStamp
	 * @param noteId
	 * @return
	 */
	public SynchLogEntity findAttachmentLogByNote(String clientId, String userId, long timeStamp, String noteId);

	/**
	 * 查询条目下附件操作日志数量
	 * @param clientId
	 * @param userId
	 * @param timeStamp
	 * @param dataClass
	 * @param dataKey
	 * @return
	 */
	public int countAttachmentLogByNote(String clientId, String userId,
			long timeStamp, String noteId);
	
	/**
	 * 查询专题成员日志
	 * @param clientId
	 * @param userId
	 * @param timeStamp
	 * @param subjectId
	 * @return
	 */
	public SynchLogEntity findSubjectUserLogs(String clientId, String userId, long timeStamp, String subjectId);
	
	/**
	 * 查询专题成员日志数量
	 * @param clientId
	 * @param userId
	 * @param timeStamp
	 * @param subjectId
	 * @return
	 */
	public int countSubjectUserLogs(String clientId, String userId, long timeStamp, String subjectId);

	/**
	 * 查询某专题下数据的同步日志
	 * @param clientId
	 * @param userId
	 * @param timeStamp
	 * @param subjectId
	 * @return
	 */
	public SynchLogEntity findSubjectRelatedLogs(String clientId, String userId,long timeStamp, String subjectId);

	/**
	 * 查询某专题下数据的同步日志数量
	 * @param clientId
	 * @param userId
	 * @param timeStamp
	 * @param subjectId
	 * @return
	 */
	public int countSubjectRelatedLogs(String clientId, String userId, long timeStamp, String subjectId);

	/**
	 * 根据操作合并两个日志
	 * @param orgi
	 * @param nextLog
	 * @param clientId
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public SynchLogEntity mergeLog(SynchLogEntity orgi, SynchLogEntity nextLog, String clientId, String userId)
			throws Exception;

	/**
	 * 合并集合中的日志
	 * @param logList
	 * @param clientId
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public List<SynchLogEntity> mergeAllLogs(List<SynchLogEntity> logList, String clientId, String userId) throws Exception;
	
	/**
	 * 返回客户端日志查询方法
	 * @param clientId
	 * @param userId
	 * @param timeStamp
	 * @param dataClass
	 * @param isDeleteFilter
	 * @return
	 * @throws Exception 
	 */
	public List<SynchLogEntity> findSynchLogsByTarget(String clientId, String userId, long timeStamp, String dataClass, boolean isDeleteFilter) throws Exception;
	
	/**
	 * 日志保存到已同步表中
	 * @param theLog
	 * @param clientId
	 * @param userId
	 */
	public void saveSynchedLog(SynchLogEntity theLog, String clientId, String userId);

	public int countSynchLogsByTarget(String clientId, String userId, long timeStamp,
			String dataClass);

	public List<SynchLogEntity> findSynchLogsBySQL(String clientId, String userId,
			long timeStamp, String dataClass) throws Exception;
	
	/**
	 * 查询用户需下载的附件
	 * @param userId
	 * @param clientId
	 * @return
	 * @throws Exception 
	 */
	public List<SynchronizedLogEntity> findNeedDownloadAttachment(String userId, String clientId) throws Exception;

	/**
	 * 根据用户、客户端查询已同步日志
	 * @param clientId
	 * @param userId
	 * @return
	 */
	public List<SynchronizedLogEntity> findSynchedLogs(String clientId, String userId);
}
