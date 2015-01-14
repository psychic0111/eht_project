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
	public int countSynchLogsByTarget(String clientId, String userId, String action, long timeStamp, long endTimestamp);

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
	public List<SynchLogEntity> mergeAllLogs(List<SynchLogEntity> logList, String clientId, String userId, String mergetAction, boolean saveLog) throws Exception;
	
	/**
	 * 返回客户端日志查询方法
	 * @param clientId
	 * @param userId
	 * @param timeStamp
	 * @param dataClass
	 * @param isDeleteFilter
	 * @param saveLog   是否存记录表（存入后表示此日志已同步过）
	 * @return
	 * @throws Exception 
	 */
	public List<SynchLogEntity> findSynchLogsByTarget(String clientId, String userId, long timeStamp, long endTime, String dataClass, boolean isDeleteFilter, boolean saveLog) throws Exception;
	
	/**
	 * 日志保存到已同步表中
	 * @param theLog
	 * @param clientId
	 * @param userId
	 */
	public void saveSynchedLog(SynchLogEntity theLog, String clientId, String userId);

	public int countSynchLogsByTarget(String clientId, String userId, long timeStamp, long endTime,
			String dataClass, String action);

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

	/**
	 * 查询truncate日志
	 * @param clientId  客户端ID
	 * @param userId    用户ID
	 * @param timeStamp 同步下限时间戳
	 * @param endTime   同步上限时间戳
	 * @param dataClass 数据类型
	 * @param saveLog   是否存记录表（存入后表示此日志已同步过）
	 * @return
	 * @throws Exception
	 */
	public List<SynchLogEntity> findTruncSynchLogs(String clientId, String userId,
			long timeStamp, long endTime, String dataClass, boolean saveLog) throws Exception;

	/**
	 * 生成删除成员角色的日志
	 * @param subjectId
	 * @param userId
	 */
	public void generateUserSubjectDelLog(String subjectId,
			String userId);

	/**
	 * 生成添加成员角色的日志
	 * @param subjectId
	 * @param userId
	 */
	public void generateUserSubjectAddLog(String subjectId, String userId,
			String roleId);
	
	/**
	 * 生成改变成员角色的回收站日志
	 * @param subjectId
	 * @param userId
	 */
	public void generateRecycleLogs(String subjectId, String userId,
			String roleId, String newRoleId);
	
	/**
	 * 生成添加专题成员日志
	 * @param subjectId
	 * @param userId
	 */
	public void generateAddSubjectUserLogs(String id, String subjectId, String userId,
			String roleId, String action, String creator, long createTimestamp);

	/**
	 * 生成删除专题成员日志
	 * @param subjectId
	 * @param userId
	 */
	public void generateDelSubjectUserLogs(String id, String subjectId, String userId,
			String action);

	/**
	 * 记录同步日志
	 * @param paramEntity
	 * @param dataClass
	 * @param action
	 * @param targetUser
	 */
	public void recordLog(Object paramEntity, String dataClass, String action,
			String targetUser, long synchTime);

	/**
	 * 查询需要下载的条目ZIP
	 * @param userId
	 * @param clientId
	 * @return
	 * @throws Exception
	 */
	public List<SynchronizedLogEntity> findNeedDownloadNoteFile(String userId,
			String clientId) throws Exception;

	/**
	 * 查询需要下载的文件
	 * @param userId
	 * @param clientId
	 * @return
	 * @throws Exception
	 */
	public List<SynchronizedLogEntity> findNeedDownloadFile(String userId,
			String clientId) throws Exception;

	/**
	 * 更新日志状态
	 * @param userId
	 * @param clientId
	 * @param dataType
	 * @param classPk
	 * @return
	 */
	public void updateSynchedLogStatus(String userId,
			String clientId, String dataType, String classPk);

	/**
	 * 根据logid查询已处理的日志记录
	 * @param logId
	 * @return
	 */
	public SynchronizedLogEntity findSynchedLogByLogId(String logId);

	/**
	 * 查询已处理的日志记录
	 * @param logId
	 * @return
	 */
	public SynchronizedLogEntity findSynchedLog(String clientId, String userId,
			String className, String classPK, String action);

}
