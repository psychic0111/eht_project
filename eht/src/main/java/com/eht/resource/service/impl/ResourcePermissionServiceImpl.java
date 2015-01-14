package com.eht.resource.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.jeecgframework.core.common.service.impl.CommonServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eht.common.constant.Constants;
import com.eht.group.entity.Group;
import com.eht.group.service.GroupService;
import com.eht.resource.entity.ResourceAction;
import com.eht.resource.entity.ResourcePermission;
import com.eht.resource.service.ResourceActionService;
import com.eht.resource.service.ResourcePermissionService;
import com.eht.role.entity.RoleUser;
import com.eht.role.service.RoleService;

@Service("resourcePermissionService")
@Transactional
public class ResourcePermissionServiceImpl extends CommonServiceImpl implements ResourcePermissionService {
	
	@Autowired
	private ResourceActionService resourceActionService;
	
	@Autowired
	private RoleService roleService;
	
	@Autowired
	private GroupService groupService;
	
	@Override
	public void saveOrUpdatePermission(ResourcePermission rp) {
		saveOrUpdate(rp);
	}
	
	@Override
	public void grantPermissions(String resourceName, String primaryKey, String roleId, String action) {
		ResourceAction ra = resourceActionService.findResourceAction(resourceName, action);
		grantPermissions(resourceName, primaryKey, roleId, ra.getBitwiseValue());
	}

	@Override
	public boolean hasPermissionRole(String resourceName, String actionName, String primaryKey, String roleId) {
		ResourcePermission rp = findResourcePermission(roleId, resourceName, primaryKey);
		int value = rp.getActionIds();
		ResourceAction resourceAction = resourceActionService.findResourceAction(resourceName, actionName);
		int actionId = resourceAction.getBitwiseValue();
		
		if((value & actionId) == actionId){
			return true;
		}else{
			return false;
		}
	}

	@Override
	@Deprecated
	public boolean hasPermissionUser(String resourceName, String actionName, String primaryKey, String userId) {
		//Group group = groupService.findGroup(resourceName, primaryKey);
		RoleUser ru = roleService.findUserRole(userId, primaryKey);
		if(ru != null){
			if(hasPermissionRole(resourceName, actionName, primaryKey, ru.getRoleId())){
				return true;
			}
		}
		return false;
	}

	@Override
	public ResourcePermission findResourcePermission(String roleId, String resourceName, String primaryKey) {
		DetachedCriteria dc = DetachedCriteria.forClass(ResourcePermission.class);
		dc.add(Restrictions.eq("roleId", roleId));
		dc.add(Restrictions.eq("resourceName", resourceName));
		dc.add(Restrictions.eq("primaryKey", primaryKey));
		List<ResourcePermission> list = findByDetached(dc);
		if(list != null && !list.isEmpty()){
			return list.get(0);
		}
		return null;
	}
	
	@Override
	public List<ResourcePermission> findResourcePermission(String resourceName, String primaryKey) {
		DetachedCriteria dc = DetachedCriteria.forClass(ResourcePermission.class);
		dc.add(Restrictions.eq("resourceName", resourceName));
		dc.add(Restrictions.eq("primaryKey", primaryKey));
		List<ResourcePermission> list = findByDetached(dc);
		return list;
	}

	@Override
	public void grantPermissions(String resourceName, String primaryKey, String roleId, int actionValue) {
		ResourcePermission rp = findResourcePermission(roleId, resourceName, primaryKey);
		if(rp == null){
			rp = new ResourcePermission();
			rp.setActionIds(actionValue);
			rp.setPrimaryKey(primaryKey);
			rp.setResourceName(resourceName);
			rp.setRoleId(roleId);
			
		}else{
			int val = rp.getActionIds() | actionValue;
			rp.setActionIds(val);
		}
		saveOrUpdatePermission(rp);
	}

	@Override
	public void saveResourcePermission(ResourcePermission rp) {
		save(rp);
	}

	@Override
	public void updateResourcePermission(ResourcePermission rp) {
		updateEntitie(rp);
	}

	@Override
	public void removePermissions(String resourceName, String primaryKey, String roleId, int actionValue) {
		ResourcePermission rp = findResourcePermission(roleId, resourceName, primaryKey);
		if(rp != null){
			int value = rp.getActionIds();
			if(value > 0){
				int newValue = value & (~actionValue);
				if(newValue > 0){
					rp.setActionIds(newValue);
					updateEntitie(rp);
				} else{
					deleteResourcePermission(rp);
				}
			}
		}
	}

	@Override
	public void removePermissions(String resourceName, String primaryKey, String roleId, String action) {
		ResourceAction ra = resourceActionService.findResourceAction(resourceName, action);
		removePermissions(resourceName, primaryKey, roleId, ra.getBitwiseValue());
	}

	@Override
	public void deleteResourcePermission(ResourcePermission rp) {
		delete(rp);
	}

	@Override
	public void deleteResourcePermission(Long id) {
		ResourcePermission rp = get(ResourcePermission.class, id);
		deleteResourcePermission(rp);
	}

	@Override
	public void deletePermissionByPK(String resourceName, String primaryKey) {
		List<ResourcePermission> list = findResourcePermission(resourceName, primaryKey);
		deleteAllEntitie(list);
	}

	@Override
	public void deletePermissionByRole(String roleId) {
		List<ResourcePermission> list = findByProperty(ResourcePermission.class, "roleId", roleId);
		deleteAllEntitie(list);
	}

	@Override
	public Map<String, String> findSubjectPermissionsByUser(String userId, String subjectId) {
		RoleUser ru = roleService.findUserRole(userId, subjectId);
		String primaryKey = resourceActionService.findResourceByName(Constants.SUBJECT_MODULE_NAME).getClassNameId() + "";  //专题管理中权限，主键是classNameid
		List<ResourceAction> raList = resourceActionService.findActionsByName(Constants.SUBJECT_MODULE_NAME);
		Map<String, String> actionMap = new HashMap<String, String>();
		if(ru != null){
			for(ResourceAction ra : raList){
				boolean flag = hasPermissionRole(Constants.SUBJECT_MODULE_NAME, ra.getAction(), primaryKey, ru.getRoleId());
				if(flag){
					actionMap.put(ra.getAction(), String.valueOf(flag));
					//break;
				}else{
					
				}
			}
		}
		return actionMap;
	}

	@Override
	public Map<String, String> findSubjectPermissionsByRole(String roleId, String subjectId) {
		String primaryKey = resourceActionService.findResourceByName(Constants.SUBJECT_MODULE_NAME).getClassNameId() + "";  //专题管理中权限，主键是classNameid
		List<ResourceAction> raList = resourceActionService.findActionsByName(Constants.SUBJECT_MODULE_NAME);
		Map<String, String> actionMap = new HashMap<String, String>();
		for(ResourceAction ra : raList){
			boolean flag = hasPermissionRole(Constants.SUBJECT_MODULE_NAME, ra.getAction(), primaryKey, roleId);
			actionMap.put(ra.getAction(), String.valueOf(flag));
		}
		return actionMap;
	}

	@Override
	public Map<String, String> findSubjectPermissionsByUser(String userId,String subjectId, String noteId) {
		Map<String, String> actionMap = findSubjectPermissionsByUser(userId,subjectId);
		if(noteId!=null && noteId.length()>0 &&( actionMap.get("UPDATE_NOTE")==null || actionMap.get("DELETE_NOTE")==null)){
			List<Long> list = super.findHql("select count(*) from NoteEntity where id = ? and createUser = ?  ", new Object[]{noteId,userId});
			if(list!=null && !list.isEmpty() && list.get(0)>0L){
				actionMap.put("UPDATE_NOTE", "true");
				actionMap.put("DELETE_NOTE", "true");
			}
		}
		return actionMap;
	}

}
