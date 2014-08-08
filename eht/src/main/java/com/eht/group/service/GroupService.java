/**
 * 
 */
package com.eht.group.service;

import java.util.List;

import org.jeecgframework.core.common.service.CommonService;

import com.eht.group.entity.Group;
import com.eht.group.entity.GroupUser;

/**
 * @author chenlong
 *
 */
public interface GroupService extends CommonService {
	
	/**
	 * 保存组
	 * @param ac
	 */
	public void addGroup(Group group);
	/**
	 * 保存组
	 * @param ac
	 */
	public Group addGroup(Long classNameId, String classPK, String description, String groupName, long parentGroupId);
	/**
	 * 删除组
	 * @param groupId
	 * @return
	 */
	public boolean deleteGroup(Long groupId);
	
	/**
	 * 删除组
	 * @param group
	 * @return
	 */
	public boolean deleteGroup(Group group);
	
	/**
	 * 查询资源所属GROUP
	 * @param classNameId
	 * @param classPk
	 * @return
	 */
	public Group findGroup(Long classNameId, String classPk);
	
	/**
	 * 查询资源所属GROUP
	 * @param className
	 * @param classPk
	 * @return
	 */
	public Group findGroup(String className, String classPk);
	
	/**
	 * 根据父组ID查询所有子组
	 * @param parentGroupId
	 * @return
	 */
	public List<Group> findGroupByParent(long parentGroupId);
	
	/**
	 * 添加组用户
	 * @param groupId
	 * @param UserId
	 * @return
	 */
	public boolean addGroupUser(Long groupId, String userId);
	
	/**
	 * 移除组用户
	 * @param groupId
	 * @param UserId
	 * @return
	 */
	public boolean removeGroupUser(Long groupId, String userId);
	
	/**
	 * 移除某组所有用户关系
	 * @param groupId
	 * @return
	 */
	public boolean removeGUByGroupId(Long groupId);
	
	/**
	 * 移除某用户所有组关系
	 * @param userId
	 * @return
	 */
	public boolean removeGUByUserId(String userId);
	
	/**
	 * 查询用户组关系
	 * @param groupId
	 * @param userId
	 * @return
	 */
	public GroupUser findGroupUser(Long groupId, String userId);
	
	/**
	 * 查询用户是否在条目黑名单
	 * @param noteId
	 * @param userId
	 * @return
	 */
	public  boolean   checkNoteUser(String userId, String noteId);
	
	/**
	 * 查询用户是否在目录黑名单
	 * @param noteId
	 * @param userId
	 * @return
	 */
	public boolean checkDirectoryUser(String userId, String noteId);
	
	/**
	 * 查询用户是否在资源下黑名单
	 * @return
	 */
	public boolean checkGroupUser(String className,String userId, String noteId);
	
}
