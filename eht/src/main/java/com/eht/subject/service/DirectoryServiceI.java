package com.eht.subject.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jeecgframework.core.common.service.CommonService;

import com.eht.subject.entity.DirectoryEntity;
import com.eht.subject.entity.SubjectEntity;

public interface DirectoryServiceI extends CommonService{
	
	/**
	 * 
	 * @param ids
	 * @return
	 */
	public List<DirectoryEntity> findDirsByIds(String[] ids);
	/**
	 * 添加目录,返回目录groupId
	 * @param dir
	 */
	public Serializable addDirectory(DirectoryEntity dir);
	/**
	 * 添加目录
	 * @param subject
	 * @param parentDir
	 * @param dirName
	 * @return
	 */
	public Serializable addDirectory(SubjectEntity subject, DirectoryEntity parentDir, String dirName);
	/**
	 * 更新目录
	 * @param dir
	 */
	public void updateDirectory(DirectoryEntity dir);
	/**
	 * 删除目录
	 * @param id
	 */
	public void deleteDirectory(String id);
	/**
	 * 删除目录
	 * @param dir
	 */
	public void deleteOnlyDirectory(DirectoryEntity dir);
	
	/**
	 * 根据ID查询专题
	 * @param id
	 * @return
	 */
	public DirectoryEntity getDirectory(Serializable id);
	
	/**
	 * 查询专题下目录
	 * @param subjectId
	 * @return
	 */
	public List<DirectoryEntity> findDirsBySubject(String subjectId);
	
	/**
	 * 查询专题下目录根据时间排序
	 * @param subjectId
	 * @return
	 */
	public List<DirectoryEntity> findDirsBySubjectOderByTime(String subjectId,boolean isasc);
	/**
	 * 查询专题下目录
	 * @param subjectId
	 * @return
	 */
	public List<DirectoryEntity> findDirsBySubject(String subjectId,String userId);
	/**
	 * 查询专题下某目录下的子目录
	 * @param subjectId
	 * @return
	 */
	public List<DirectoryEntity> findSubDirs(String subjectId, String parentId);
	/**
	 * 查询专题下用户可见目录
	 * @param subjectId
	 * @return
	 */
	public List<DirectoryEntity> findUserDirsBySubject(String userId, String subjectId);
	/**
	 * 将用户加入黑名单，用户将不能访问该目录及目录下资源
	 * @param userId
	 * @param dirId
	 * @return
	 */
	public void blacklistedUser(String userId, String dirId);
	
	/**
	 * 将用户从黑名单移除
	 * @param userId
	 * @param dirId
	 * @return
	 */
	public void removeUser4lacklist(String userId, String dirId);
	
	/**
	 * 检查目录名是否存在，同级目录不能重名
	 * @param dirName
	 * @param dirId
	 * @return
	 */
	public boolean nameExists(DirectoryEntity dir);
	
	/**
	 * 是否在目录黑名单
	 * @param groupId
	 * @param userId
	 * @return
	 */
	public boolean inDirBlackList(String userId, String dirId);
	
	/**
	 * 标记删除目录
	 * @param dir
	 */
	public void markDelDirectory(DirectoryEntity dir);
	
	/**
	 * 查询已标记删除的目录
	 * @param userId
	 * @param subjectType
	 * @return
	 */
	public List<DirectoryEntity> findDeletedDirs(String userId, int subjectType);
	
	/**
	 * 还原已删除目录
	 * @param dir
	 */
	public DirectoryEntity restoreDirectory(DirectoryEntity dir, List<String> dirIdList,boolean isNoteUpdate);
	
	/**
	 * 查询已标记删除的目录
	 * @param userId
	 * @param subjectId 可为空
	 * @return
	 */
	public List<DirectoryEntity> findDeletedDirs(String userId, String subjectId);
	
	/**
	 * 取得所有目录上层的目录id
	 * @return
	 */
	public void findUpDirs(String dirId,List<String> list);
}
