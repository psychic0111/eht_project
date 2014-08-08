/**
 * 
 */
package com.eht.resource.service;

import java.util.List;
import java.util.Map;

import org.jeecgframework.core.common.service.CommonService;

import com.eht.resource.entity.ResourcePermission;

/**
 * @author chenlong
 *
 */
public interface ResourcePermissionService extends CommonService {
	
	/**
	 * 添加资源权限
	 * @param ac
	 */
	public void saveResourcePermission(ResourcePermission rp);
	
	/**
	 * 更新资源权限
	 * @param ac
	 */
	public void updateResourcePermission(ResourcePermission rp);
	/**
	 * 添加或更新资源权限
	 * @param ac
	 */
	public void saveOrUpdatePermission(ResourcePermission rp);
	
	/**
	 * 授予资源权限
	 * @param sysCode
	 * @param resourceName
	 * @param primaryKey
	 * @param roleId
	 * @param actionValue
	 */
	public void grantPermissions(String resourceName, String primaryKey, String roleId , int actionValue);
	
	/**
	 * 授予资源权限
	 * @param sysCode
	 * @param resourceName
	 * @param primaryKey
	 * @param roleId
	 * @param actionValue
	 */
	public void grantPermissions(String resourceName, String primaryKey, String roleId , String action);
	/**
	 * 收回资源权限
	 * @param sysCode
	 * @param resourceName
	 * @param primaryKey
	 * @param roleId
	 * @param actionValue
	 */
	public void removePermissions(String resourceName, String primaryKey, String roleId , int actionValue);
	
	/**
	 * 收回资源权限
	 * @param sysCode
	 * @param resourceName
	 * @param primaryKey
	 * @param roleId
	 * @param actionValue
	 */
	public void removePermissions(String resourceName, String primaryKey, String roleId , String action);
	
	/**
	 * 判断角色权限
	 * @param resourceAction
	 * @param roleId
	 * @return
	 */
	public boolean hasPermissionRole(String resourceName, String actionName, String primaryKey, String roleId);
	
	/**
	 * 判断用户权限 
	 * @param resourceAction
	 * @param userId
	 * @return
	 */
	@Deprecated
	public boolean hasPermissionUser(String resourceName, String actionName, String primaryKey, String userId);
	
	/**
	 * 查询角色权限 
	 * @return
	 */
	public ResourcePermission findResourcePermission(String roleId, String resourceName, String primaryKey);
	
	/**
	 * 查询资源权限 
	 * @return
	 */
	public List<ResourcePermission> findResourcePermission(String resourceName, String primaryKey);
	
	/**
	 * 删除权限
	 * @param rp
	 */
	public void deleteResourcePermission(ResourcePermission rp);
	
	/**
	 * 删除权限
	 * @param id
	 */
	public void deleteResourcePermission(Long id);
	
	/**
	 * 删除某资源所有授权
	 * @param resourceName
	 * @param primaryKey
	 */
	public void deletePermissionByPK(String resourceName, String primaryKey);
	
	/**
	 * 删除某角色所有授权
	 * @param roleId
	 */
	public void deletePermissionByRole(String roleId);
	
	/**
	 * 查询用户在专题下的有权限操作
	 * @param userId
	 * @param subjectId
	 * @return
	 */
	public Map<String, String> findSubjectPermissionsByUser(String userId, String subjectId);
	
	/**
	 * 查询用户在专题某条目下的有权限操作
	 * @param userId
	 * @param subjectId
	 * @return
	 */
	public Map<String, String> findSubjectPermissionsByUser(String userId, String subjectId, String noteId);
	
	/**
	 * 查询角色在专题下的有权限操作
	 * @param roleId
	 * @param subjectId
	 * @return
	 */
	public Map<String, String> findSubjectPermissionsByRole(String roleId, String subjectId);
}
