/**
 * 
 */
package com.eht.role.service;

import java.util.List;

import org.jeecgframework.core.common.service.CommonService;

import com.eht.common.page.PageResult;
import com.eht.role.entity.Role;
import com.eht.role.entity.RoleUser;

/**
 * @author chenlong
 *
 */
public interface RoleService extends CommonService {
	
	/**
	 * 添加角色
	 * @param ac
	 */
	public void addRole(Role role);
	
	/**
	 * 添加或更新角色
	 * @param ac
	 */
	public void addOrUpdateRole(Role role);
	
	/**
	 * 根据ID查询角色
	 * @param id
	 */
	public Role getRole(String id);
	
	/**
	 * 删除角色
	 * @param roleId
	 * @return
	 */
	public boolean deleteRole(String roleId);
	
	/**
	 * 删除角色
	 * @param role
	 * @return
	 */
	public boolean deleteRole(Role role);
	
	/**
	 * 根据角色名称查询角色
	 * @param resourceName
	 * @param action
	 * @return
	 */
	public Role findRoleByName(String roleName);
	
	/**
	 * 添加角色-用户
	 * @param roleId
	 * @param userId
	 * @return
	 */
	public boolean addRoleUser(String subjectId, String userId, String roleId);
	
	/**
	 * 更新用户角色
	 * @param subjectId
	 * @param userId
	 * @param roleId
	 */
	public void updateRoleUser(String subjectId, String userId, String roleId);
	/**
	 * 移除角色-用户
	 * @param roleId
	 * @param userId
	 * @return
	 */
	public boolean removeRoleUser(String subjectId, String userId);
	
	/**
	 * 移除某角色所有用户
	 * @param roleId
	 * @return
	 */
	public boolean removeRUByRole(String roleId, String subjectId);
	
	/**
	 * 查询用户拥有角色
	 */
	public RoleUser findUserRole(String userId, String subjectId);
	
	/**
	 * 查询专题下的用户
	 */
	public List<RoleUser> findSubjectUsers(String subjectId);
	
	/**
	 * 查询专题下的用户分页
	 */
	public List<RoleUser> findSubjectUsers(String subjectId,PageResult pageResult);
	
	
	/**
	 * 查询目录下的成员 
	 * 如果上级目录出现此成员则下级目录不显示该成员
	 */
	public List<RoleUser> findDirtUsers(String subjectId,String dirId,PageResult pageResult);
	
	/**
	 * 更新专题用户角色
	 * @param ru
	 */
	public void updateRoleUser(RoleUser ru);
	
	/**
	 * 专题移除用户
	 * @param ru
	 * @return
	 */
	public boolean removeRoleUser(RoleUser ru);
	
	/**
	 * 专题添加成员
	 * @param ru
	 * @return
	 */
	public boolean addRoleUser(RoleUser ru);
	
	/**
	 * 查询所有角色
	 * @return
	 */
	public List<Role> findAllRoles();
}
