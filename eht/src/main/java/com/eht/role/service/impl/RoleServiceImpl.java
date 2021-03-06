package com.eht.role.service.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jeecgframework.core.common.service.impl.CommonServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eht.common.annotation.RecordOperate;
import com.eht.common.constant.RoleName;
import com.eht.common.enumeration.DataSynchAction;
import com.eht.common.enumeration.DataType;
import com.eht.common.page.PageResult;
import com.eht.common.util.UUIDGenerator;
import com.eht.role.entity.Role;
import com.eht.role.entity.RoleUser;
import com.eht.role.service.RoleService;
import com.eht.subject.service.DirectoryServiceI;


@Service("roleService")
@Transactional
public class RoleServiceImpl extends CommonServiceImpl implements RoleService {
	
	@Autowired
	private DirectoryServiceI directoryService;

	@Override
	@RecordOperate(dataClass=DataType.ROLE, action=DataSynchAction.ADD, keyMethod="getId")
	public void addRole(Role role) {
		save(role);
	}

	@Override
	public Role getRole(String id) {
		return get(Role.class, id);
	}

	@Override
	public Role findRoleByName(String roleName) {
		return findUniqueByProperty(Role.class, "roleName", roleName);
	}

	@Override
	public boolean deleteRole(String roleId) {
		deleteEntityById(Role.class, roleId);
		return true;
	}

	@Override
	public boolean deleteRole(Role role) {
		delete(role);
		return true;
	}
	
	@Override
	public boolean addRoleUser(RoleUser ru) {
		save(ru);
		return true;
	}
	
	/**
	 * 参数顺序不能改变,记录日志时用到
	 */
	@Override
	@RecordOperate(dataClass=DataType.SUBJECTUSER, action=DataSynchAction.ADD, keyIndex=0, targetUser=-1)
	public boolean addRoleUser(String subjectId, String userId, String roleId) {
		RoleUser ru = new RoleUser();
		ru.setId(UUIDGenerator.uuid());
		ru.setGroupId(subjectId);
		ru.setRoleId(roleId);
		ru.setUserId(userId);
		addRoleUser(ru);
		return true;
	}
	
	@Override
	//@RecordOperate(dataClass="SUBJECTUSER", action=SynchConstants.DATA_OPERATE_DELETE, keyMethod="getId")
	public boolean removeRoleUser(RoleUser ru) {
		delete(ru);
		return true;
	}
	
	@Override
	@RecordOperate(dataClass=DataType.SUBJECTUSER, action=DataSynchAction.DELETE, keyIndex=0, targetUser=-1)
	public boolean removeRoleUser(String subjectId, String userId) {
		DetachedCriteria dc = DetachedCriteria.forClass(RoleUser.class);
		dc.add(Restrictions.eq("userId", userId));
		dc.add(Restrictions.eq("groupId", subjectId));
		List<RoleUser> list = findByDetached(dc);
		for(RoleUser ru : list){
			removeRoleUser(ru);
		}
		return true;
	}

	@Override
	public boolean removeRUByRole(String roleId, String subjectId) {
		DetachedCriteria dc = DetachedCriteria.forClass(RoleUser.class);
		dc.add(Restrictions.eq("roleId", roleId));
		dc.add(Restrictions.eq("groupId", subjectId));
		List<RoleUser> list = findByDetached(dc);
		for(RoleUser ru : list){
			removeRoleUser(ru);
		}
		return true;
	}

	@Override
	public RoleUser findUserRole(String userId, String subjectId) {
		DetachedCriteria dc = DetachedCriteria.forClass(RoleUser.class);
		dc.add(Restrictions.eq("userId", userId));
		dc.add(Restrictions.eq("groupId", subjectId));
		List<RoleUser> list = findByDetached(dc);
		if(list != null && !list.isEmpty()){
			return list.get(0);
		}
		return null;
	}

	@Override
	public void addOrUpdateRole(Role role) {
		saveOrUpdate(role);
	}

	@Override
	public List<RoleUser> findSubjectUsers(String subjectId) {
		DetachedCriteria dc = DetachedCriteria.forClass(RoleUser.class);
		dc.add(Restrictions.eq("groupId", subjectId));
		List<RoleUser> list = findByDetached(dc);
		return list;
	}
	
	@Override
	public List<RoleUser> findSubjectUsers(String subjectId,PageResult pageResult) {
		if(pageResult == null){
			pageResult = new PageResult();
		}
		pageResult.setPageSize(10);
		DetachedCriteria count = DetachedCriteria.forClass(RoleUser.class);
		count.add(Restrictions.eq("groupId", subjectId)).createCriteria("role").add( Restrictions.like("roleType", 2)).add( Restrictions.ne("roleName", RoleName.ADMIN));
			
		Long total=(Long) count.getExecutableCriteria(getSession()).setProjection(Projections.rowCount()).uniqueResult();
		pageResult.setTotal(total);
		DetachedCriteria dc = DetachedCriteria.forClass(RoleUser.class);
		dc.add(Restrictions.eq("groupId", subjectId)).createCriteria("role").add( Restrictions.like("roleType", 2)).add( Restrictions.ne("roleName", RoleName.ADMIN));
		int firstRow = (pageResult.getPageNo() - 1) * pageResult.getPageSize();
		pageResult.setRows(pageList(dc, firstRow, pageResult.getPageSize()));
		return (List<RoleUser>) pageResult.getRows();
	}
	
	@Override
	public List<RoleUser> findDirtUsers(String subjectId, String dirId,PageResult pageResult) {
		if(pageResult == null){
			pageResult = new PageResult();
		}
		List <String>list = new ArrayList<String>();
		directoryService.findUpDirs(dirId,list);
		StringBuffer sb=new StringBuffer("");
		for (int i = 0; i < list.size(); i++) {
			if(i!=0){
				sb.append(",");
			}
			sb.append("'"+list.get(i)+"'");
		}
		String noin="";
		if(sb.length()>0){
			 noin=" and u.userid not in(select u.userid from eht_group p , eht_group_user u where  u.groupid=p.groupid and p.classpk in("+sb.toString()+") )";
		}
		pageResult.setPageSize(10);
		int firstRow = (pageResult.getPageNo() - 1) * pageResult.getPageSize();
		String sql="select u.* from eht_user_role u,eht_role r where u.groupid='"+subjectId+"' and r.roletype=2 and rolename!='"+RoleName.ADMIN+"' and u.roleid=r.id  "+noin;
		sql+="  limit " + firstRow + "," + (firstRow+pageResult.getPageSize());
		String sqlcout="select count(*) from eht_user_role u,eht_role r where u.groupid='"+subjectId+"' and r.roletype=2 and rolename!='"+RoleName.ADMIN+"' and u.roleid=r.id"+noin;
		BigInteger b=(BigInteger)this.commonDao.findListbySql(sqlcout).get(0);
		pageResult.setTotal(b.longValue());
		pageResult.setRows(this.commonDao.findListbySql(sql, RoleUser.class));
		return (List<RoleUser>) pageResult.getRows();
	}
	

	@Override
	public void updateRoleUser(String subjectId, String userId, String roleId) {
		DetachedCriteria dc = DetachedCriteria.forClass(RoleUser.class);
		dc.add(Restrictions.eq("userId", userId));
		dc.add(Restrictions.eq("groupId", subjectId));
		dc.add(Restrictions.eq("roleId", roleId));
		List<RoleUser> list = findByDetached(dc);
		if(list != null && !list.isEmpty()){
			updateRoleUser(list.get(0));
		}
	}
	
	@Override
	@RecordOperate(dataClass=DataType.SUBJECTUSER, action=DataSynchAction.UPDATE, keyIndex=0, keyMethod="getId")
	public void updateRoleUser(RoleUser ru) {
		updateEntitie(ru);
	}

	@Override
	public List<Role> findAllRoles() {
		DetachedCriteria dc = DetachedCriteria.forClass(Role.class);
		return findByDetached(dc);
	}


}
