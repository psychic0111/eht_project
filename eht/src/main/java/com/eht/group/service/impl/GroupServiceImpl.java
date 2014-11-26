package com.eht.group.service.impl;

import java.util.Date;
import java.util.List;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jeecgframework.core.common.service.impl.CommonServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.eht.group.entity.Group;
import com.eht.group.entity.GroupUser;
import com.eht.group.service.GroupService;
import com.eht.note.entity.NoteEntity;
import com.eht.resource.entity.ClassName;
import com.eht.resource.service.ResourceActionService;
import com.eht.role.entity.RoleUser;
import com.eht.subject.entity.DirectoryEntity;
import com.eht.subject.entity.SubjectEntity;

@Service("groupService")
@Transactional
public class GroupServiceImpl extends CommonServiceImpl implements GroupService {
	
	@Autowired
	private ResourceActionService resourceActionService;
	
	@Override
	public void addGroup(Group group) {
		save(group);
	}

	@Override
	public boolean deleteGroup(Long groupId) {
		deleteEntityById(Group.class, groupId);
		return true;
	}

	@Override
	public boolean deleteGroup(Group group) {
		delete(group);
		return true;
	}

	@Override
	public boolean addGroupUser(Long groupId, String userId) {
		GroupUser gu = new GroupUser();
		gu.setGroupId(groupId);
		gu.setUserId(userId);
		save(gu);
		return true;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public GroupUser findGroupUser(Long groupId, String userId) {
		GroupUser gu = new GroupUser();
		gu.setGroupId(groupId);
		gu.setUserId(userId);
		List<GroupUser> list = findByExample(GroupUser.class.getName(), gu);
		if(list != null && !list.isEmpty()){
			return list.get(0);
		}
		return null;
	}
	
	@Override
	public List<GroupUser> findGroupUsers(Long groupId) {
		GroupUser gu = new GroupUser();
		gu.setGroupId(groupId);
		List<GroupUser> list = findByExample(GroupUser.class.getName(), gu);
		
		return list;
	}
	
	@Override
	public boolean removeGroupUser(Long groupId, String userId) {
		GroupUser gu = findGroupUser(groupId, userId);
		if(gu != null){
			delete(gu);
		}
		return true;
	}

	@Override
	public boolean removeGUByGroupId(Long groupId) {
		DetachedCriteria dc = DetachedCriteria.forClass(GroupUser.class);
		dc.add(Restrictions.eq("groupId", groupId));
		List<GroupUser> list = findByDetached(dc);
		deleteAllEntitie(list);
		return true;
	}

	@Override
	public boolean removeGUByUserId(String userId) {
		DetachedCriteria dc = DetachedCriteria.forClass(GroupUser.class);
		dc.add(Restrictions.eq("userId", userId));
		List<GroupUser> list = findByDetached(dc);
		deleteAllEntitie(list);
		return false;
	}

	@Override
	public Group findGroup(Long classNameId, String classPk) {
		DetachedCriteria dc = DetachedCriteria.forClass(Group.class);
		dc.add(Restrictions.eq("classNameId", classNameId));
		dc.add(Restrictions.eq("classPk", classPk));
		List<Group> list = findByDetached(dc);
		if(list != null && !list.isEmpty()){
			return list.get(0);
		}
		return null;
	}

	@Override
	public Group findGroup(String className, String classPk) {
		ClassName cn = resourceActionService.findResourceByName(className);
		return findGroup(cn.getClassNameId(), classPk);
	}

	@Override
	public List<Group> findGroupByParent(long parentGroupId) {
		DetachedCriteria dc = DetachedCriteria.forClass(Group.class);
		dc.add(Restrictions.eq("parentGroupId", parentGroupId));
		dc.addOrder(Order.asc("createTime"));
		List<Group> list = findByDetached(dc);
		return list;
	}
	
	@Override
	public List<Group> findGroupByParent(long parentGroupId, String className) {
		DetachedCriteria dc = DetachedCriteria.forClass(Group.class);
		dc.add(Restrictions.eq("classNameId", resourceActionService.findResourceByName(className).getClassNameId()));
		dc.add(Restrictions.eq("parentGroupId", parentGroupId));
		List<Group> list = findByDetached(dc);
		return list;
	}
	
	@Override
	public Group addGroup(Long classNameId, String classPK, String description, String groupName, long parentGroupId) {
		Group group = new Group();
		group.setClassNameId(classNameId);
		group.setClassPk(classPK);
		group.setCreateTime(new Date());
		group.setDescription(description);
		group.setGroupName(groupName);
		group.setParentGroupId(parentGroupId);
		addGroup(group);
		return group;
	}
	
	@Override
	public Group addGroup(String className, String classPK, String description, String groupName, String subjectId) {
		ClassName c = resourceActionService.findResourceByName(className);
		Group parentGroup = findGroup(SubjectEntity.class.getName(), subjectId);
		Group group = new Group();
		group.setClassNameId(c.getClassNameId());
		group.setClassPk(classPK);
		group.setCreateTime(new Date());
		group.setDescription(description);
		group.setGroupName(groupName);
		group.setParentGroupId(parentGroup.getGroupId());
		addGroup(group);
		return group;
	}
	
	@Override
	public boolean checkGroupUser(String className,String userId, String noteId) {
		Group group= findGroup(className,noteId);
		DetachedCriteria count = DetachedCriteria.forClass(GroupUser.class);
		count.add(Restrictions.eq("userId", userId));
		count.add(Restrictions.eq("groupId", group.getGroupId()));
		Long total=(Long) count.getExecutableCriteria(getSession()).setProjection(Projections.rowCount()).uniqueResult();
		if(total>0){
			return true;
		}
		return false;
	}

	@Override
	public boolean checkDirectoryUser(String userId, String noteId) {
		return checkGroupUser(DirectoryEntity.class.getName(),userId,noteId) ;
	}

	@Override
	public boolean checkNoteUser(String userId, String noteId) {
		return checkGroupUser(NoteEntity.class.getName(),userId,noteId) ;
	}

}
