package com.eht.subject.service.impl;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.jeecgframework.core.common.service.impl.CommonServiceImpl;
import org.jeecgframework.core.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eht.common.annotation.RecordOperate;
import com.eht.common.constant.Constants;
import com.eht.common.constant.RoleName;
import com.eht.common.constant.SynchConstants;
import com.eht.common.enumeration.DataSynchAction;
import com.eht.common.enumeration.DataType;
import com.eht.common.util.CollectionUtil;
import com.eht.common.util.UUIDGenerator;
import com.eht.group.entity.Group;
import com.eht.group.entity.GroupUser;
import com.eht.group.service.GroupService;
import com.eht.note.service.NoteServiceI;
import com.eht.resource.entity.ClassName;
import com.eht.resource.entity.ResourceAction;
import com.eht.resource.service.ResourceActionService;
import com.eht.resource.service.ResourcePermissionService;
import com.eht.role.entity.Role;
import com.eht.role.service.RoleService;
import com.eht.subject.entity.DirectoryEntity;
import com.eht.subject.entity.SubjectEntity;
import com.eht.subject.service.DirectoryServiceI;

@Service("directoryService")
@Transactional
public class DirectoryServiceImpl extends CommonServiceImpl implements DirectoryServiceI {
	
	@Autowired
	private ResourceActionService resourceActionService;
	
	@Autowired
	private ResourcePermissionService resourcePermissionService;
	
	@Autowired
	private GroupService groupService;
	
	@Autowired
	private RoleService roleService;
	
	@Autowired
	private NoteServiceI noteService;
	
	@Override
	@RecordOperate(dataClass=DataType.DIRECTORY, action=DataSynchAction.ADD, keyIndex=0, keyMethod="getId", timeStamp="createTime")
	public Serializable addDirectory(DirectoryEntity dir) {
		save(dir);
		ClassName c = resourceActionService.findResourceByName(DirectoryEntity.class.getName());
		if(c == null){
			c = new ClassName();
			c.setClassName(DirectoryEntity.class.getName());
			resourceActionService.addResource(c);
		}
		long parentGroupId = 0L;
		if(!StringUtil.isEmpty(dir.getSubjectId())){
			Group group = groupService.findGroup(SubjectEntity.class.getName(), dir.getSubjectId());
			parentGroupId = group.getGroupId();
		}
		Group group = groupService.addGroup(c.getClassNameId(), dir.getId(), dir.getDirName(), dir.getId(), parentGroupId);
		grantDirectoryPermissions(dir);
		return group.getGroupId();
	}
	
	/**
	 * 为添加的目录授权
	 * @param subject
	 */
	private void grantDirectoryPermissions(DirectoryEntity dir){
		String resourceName = DirectoryEntity.class.getName();
		List<ResourceAction> list = resourceActionService.findActionsByName(resourceName);
		
		// 给owner角色授权,所有权限
		Role ownerRole = roleService.findRoleByName(RoleName.OWNER);
		int value = 0;
		for(ResourceAction ra : list){
			value |= ra.getBitwiseValue();
		}
		resourcePermissionService.grantPermissions(resourceName, dir.getId(), ownerRole.getId(), value);
		
		// 给admin角色授权,所有权限
		Role adminRole = roleService.findRoleByName(RoleName.ADMIN);
		resourcePermissionService.grantPermissions(resourceName, dir.getId(), adminRole.getId(), value);
		
	}
	
	@Override
	@RecordOperate(dataClass=DataType.DIRECTORY, action=DataSynchAction.UPDATE, keyIndex=0, keyMethod="getId", timeStamp="updateTime")
	public void updateDirectory(DirectoryEntity dir) {
		updateEntitie(dir);
	}

	@Override
	@RecordOperate(dataClass=DataType.DIRECTORY, action=DataSynchAction.DELETE, keyIndex=0, keyMethod="getId", timeStamp="updateTime")
	public void markDelDirectory(DirectoryEntity dir) {
		//目录标记为删除状态
		dir.setDeleted(Constants.DATA_DELETED);
		updateEntitie(dir);
		//目录下的条目标记为不可检索
		super.executeHql("update NoteEntity set deleted = ? where dirId = ? and  deleted = ? ", new Object[]{Constants.DATA_NOTSEARCH,dir.getId(),Constants.DATA_NOT_DELETED});
		
		//查看目录下的子目录，标记为不可检索，子目录下的条目不可以检索
		setSonDirectoriesAndNotesStatus(Constants.DATA_NOTSEARCH,dir);
	}
	
	/**
	 * 设置dir的子目录以及子目录的条目的数据状态
	 * @param dataStatus
	 * @param dir
	 */
	private void setSonDirectoriesAndNotesStatus(int dataStatus,DirectoryEntity dir) {
		DetachedCriteria dc = DetachedCriteria.forClass(DirectoryEntity.class);
		dc.add(Restrictions.eq("parentId", dir.getId()));
		dc.add(Restrictions.eq("deleted", Constants.DATA_NOT_DELETED));
		List<DirectoryEntity> sonList = findByDetached(dc);
		if(sonList!=null && !sonList.isEmpty()){
			for(DirectoryEntity sonDir : sonList){
				sonDir.setDeleted(dataStatus);
				updateEntitie(sonDir);
				super.executeHql("update NoteEntity set deleted = ? where dirId = ? and  deleted = ? ", new Object[]{dataStatus,sonDir.getId(),Constants.DATA_NOT_DELETED});
				setSonDirectoriesAndNotesStatus(dataStatus,sonDir);
			}
		}
	}

	@Override
	public DirectoryEntity restoreDirectory(DirectoryEntity dir, List<String> dirIdList,boolean isNoteUpdate) {
		//恢复目录以及父目录
		restorDir(dir,dirIdList);
		//恢复不可访问的子目录
		restorNosearchDir(dir);
		return dir;
	}
	
	/**
	 * 恢复不可访问的子目录和条目
	 * @param dir
	 */
	private void restorNosearchDir(DirectoryEntity dir) {
		DetachedCriteria dc = DetachedCriteria.forClass(DirectoryEntity.class);
		dc.add(Restrictions.eq("parentId", dir.getId()));
		dc.add(Restrictions.eq("deleted", Constants.DATA_NOTSEARCH));
		List<DirectoryEntity> sonList = findByDetached(dc);
		if(sonList!=null && !sonList.isEmpty()){
			for(DirectoryEntity sonDir : sonList){
				sonDir.setDeleted(Constants.DATA_NOT_DELETED);
				updateEntitie(sonDir);
				super.executeHql("update NoteEntity set deleted = ? where dirId = ?  and deleted = ? ", new Object[]{Constants.DATA_NOT_DELETED,sonDir.getId(),Constants.DATA_NOTSEARCH});
				restorNosearchDir(sonDir);
			}
		}
	}

	private void restorDir(DirectoryEntity dir,List<String> dirIdList){
		//设置目录状态为未删除
		if(dir.getDeleted() == Constants.DATA_DELETED){
			dir.setDeleted(Constants.DATA_NOT_DELETED);
			updateEntitie(dir);
			//设置目录下的条目为未删除
			super.executeHql("update NoteEntity set deleted = ? where dirId = ? and  deleted = ? ", new Object[]{Constants.DATA_NOT_DELETED,dir.getId(),Constants.DATA_NOTSEARCH});
			dirIdList.add(dir.getId());
		}
		//查询删除的父节点
		DetachedCriteria dc = DetachedCriteria.forClass(DirectoryEntity.class);
		dc.add(Restrictions.eq("id", dir.getParentId()));
		dc.add(Restrictions.eq("deleted", Constants.DATA_DELETED));
		List<DirectoryEntity> parentList = findByDetached(dc);
		if(parentList!=null && !parentList.isEmpty()){
			restorDir(parentList.get(0),dirIdList);
		}
	}
	
	@RecordOperate(dataClass=DataType.DIRECTORY, action=DataSynchAction.ADD, keyIndex=0, keyMethod="getId", timeStamp="updateTime")
	public void markDirectoryUndeleted(DirectoryEntity dir) {
		dir.setDeleted(Constants.DATA_NOT_DELETED);
		updateEntitie(dir);
	}
	
	@Override
	public void deleteDirectory(String id) {
		//删除目录的关联群组信息
		super.executeHql("delete from GroupUser where GroupId in (select g.id from Group g  where g.classPk = ? ) ",new Object[]{id});
		super.executeHql("delete from Group g   where  g.classPk  = ? ",new Object[]{id});
		//删除条目
		noteService.deleteNoteByDir(id);
		//删除目录
		super.executeHql("delete from DirectoryEntity where id = ? ", new Object[]{id});
	}
	
	@Override
	public void deleteOnlyDirectory(DirectoryEntity dir) {
		delete(dir);
		Group g = groupService.findGroup(DirectoryEntity.class.getName(), dir.getId());
		if(g != null){
			groupService.removeGUByGroupId(g.getGroupId());
			groupService.deleteGroup(g);
		}
	}

	@Override
	public DirectoryEntity getDirectory(Serializable id) {
		return get(DirectoryEntity.class, id);
	}

	@Override
	public List<DirectoryEntity> findDirsBySubject(String subjectId) {
		DetachedCriteria dc = DetachedCriteria.forClass(DirectoryEntity.class);
		dc.add(Restrictions.eq("subjectId", subjectId));
		dc.add(Restrictions.eq("deleted", Constants.DATA_NOT_DELETED));
		List<DirectoryEntity> dirList = findByDetached(dc);
		return dirList;
	}
	
	@Override
	public List<DirectoryEntity> findDirsBySubjectOderByTime(String subjectId,boolean isasc) {
		DetachedCriteria dc = DetachedCriteria.forClass(DirectoryEntity.class);
		dc.add(Restrictions.eq("subjectId", subjectId));
		dc.add(Restrictions.eq("deleted", Constants.DATA_NOT_DELETED));
		if(isasc){
			dc.addOrder( Order.asc("createTime"));
		}else{
			dc.addOrder( Order.desc("createTime"));
		}
		List<DirectoryEntity> dirList = findByDetached(dc);
		return dirList;
	}

	@Override
	public Serializable addDirectory(SubjectEntity subject, DirectoryEntity parentDir, String dirName) {
		DirectoryEntity dir = new DirectoryEntity();
		dir.setId(UUIDGenerator.uuid());
		dir.setDeleted(0);
		dir.setCreateTime(new Date());
		dir.setCreateUser(subject.getCreateUser());
		dir.setDirName(dirName);
		dir.setSubjectId(subject.getId());
		dir.setParentId(parentDir == null ? null : parentDir.getId());
		return addDirectory(dir);
	}

	@Override
	public List<DirectoryEntity> findUserDirsBySubject(String userId, String subjectId) {
		List<DirectoryEntity> dirList = findDirsBySubject(subjectId);
		for(DirectoryEntity dir : dirList){
			if(inDirBlackList(userId, dir.getId())){
				dirList.remove(dir);
			}
		}
		return dirList;
	}

	@Override
	@RecordOperate(dataClass={DataType.DIRECTORY,DataType.DIRECTORYBLACK}, action={DataSynchAction.DELETE, DataSynchAction.ADD}, keyIndex={1, 1}, targetUser={0, 0}, timeStamp={"",""}, keyMethod={"", ""})
	public void blacklistedUser(String userId, String dirId) {
		Group group = groupService.findGroup(DirectoryEntity.class.getName(), dirId);
		//将用户放入group-user关系表
		groupService.addGroupUser(group.getGroupId(), userId);
	}

	@Override
	public boolean nameExists(DirectoryEntity dir) {
		DetachedCriteria dc = DetachedCriteria.forClass(DirectoryEntity.class);
		if(!StringUtil.isEmpty(dir.getSubjectId())){
			dc.add(Restrictions.eq("subjectId", dir.getSubjectId()));
		}
		if(!StringUtil.isEmpty(dir.getParentId())){
			dc.add(Restrictions.eq("parentId", dir.getParentId()));
		}
		dc.add(Restrictions.eq("dirName", dir.getDirName()));
		dc.add(Restrictions.eq("deleted", Constants.DATA_NOT_DELETED));
		List<DirectoryEntity> dirList = findByDetached(dc);
		if(CollectionUtil.isValidateCollection(dirList)){
			DirectoryEntity dir1 = dirList.get(0);
			//新建目录
			if(dir.getId() == null || dir.getId().equals("")){
				return true;
			}else if(!dir.getId().equals(dir1.getId())){
				return true;
			}else{
				return false;
			}
		}
		return false;
	}

	@Override
	public List<DirectoryEntity> findSubDirs(String subjectId, String parentId) {
		DetachedCriteria dc = DetachedCriteria.forClass(DirectoryEntity.class);
		dc.add(Restrictions.eq("subjectId", subjectId));
		dc.add(Restrictions.eqOrIsNull("parentId", parentId));
		dc.add(Restrictions.eq("deleted", Constants.DATA_NOT_DELETED));
		List<DirectoryEntity> dirList = findByDetached(dc);
		
		return dirList;
	}
	
	@Override
	public boolean inDirBlackList(String userId, String dirId) {
		Group group = groupService.findGroup(DirectoryEntity.class.getName(), dirId);
		GroupUser gu = groupService.findGroupUser(group.getGroupId(), userId);
		return gu != null;
	}

	@Override
	@RecordOperate(dataClass={DataType.DIRECTORY,DataType.DIRECTORYBLACK}, action={DataSynchAction.ADD, DataSynchAction.DELETE}, keyIndex={1, 1}, targetUser={0, 0}, timeStamp={"",""}, keyMethod={"", ""})
	public void removeUser4lacklist(String userId, String dirId) {
		Group group = groupService.findGroup(DirectoryEntity.class.getName(), dirId);
		groupService.removeGroupUser(group.getGroupId(), userId);
	}

	@Override
	public List<DirectoryEntity> findDeletedDirs(String userId, int subjectType) {
		DetachedCriteria dc = DetachedCriteria.forClass(DirectoryEntity.class, "d");
		dc.createCriteria("subjectEntity", "s", JoinType.INNER_JOIN);
		
		dc.add(Restrictions.eq("s.subjectType", subjectType));
		dc.add(Restrictions.eq("d.createUser", userId));
		dc.add(Restrictions.eq("d.deleted", Constants.DATA_DELETED));
		List<DirectoryEntity> list = findByDetached(dc);
		return list;
	}

	@Override
	public List<DirectoryEntity> findDeletedDirs(String userId, String subjectId) {
		DetachedCriteria dc = DetachedCriteria.forClass(DirectoryEntity.class, "d");
		dc.add(Restrictions.eq("d.createUser", userId));
		dc.add(Restrictions.eq("d.deleted", Constants.DATA_DELETED));
		dc.add(Restrictions.eq("d.subjectId", subjectId));
		List<DirectoryEntity> list = findByDetached(dc);
		return list;
	}
	@Override
	public List<DirectoryEntity> findDirsByIds(String[] ids) {
		String query = "from DirectoryEntity d where id=-1 ";
		if(ids!=null&&ids.length>0){
			for(String id:ids){
				query += " or d.id = '"+id+"'"; 
			}
		}
		List<DirectoryEntity> list = findByQueryString(query);
		return list;
	}

	@Override
	public List<DirectoryEntity> findDirsBySubject(String subjectId,String userId) {
		String query="select d.* from eht_directory d where  d.subjectId='"+subjectId+"' and d.deleted="+Constants.DATA_NOT_DELETED+"  and d.id not in(select p.classpk from eht_group p , eht_group_user u where u.userid='"+userId +"' and  u.groupid=p.groupid )  order by d.createTime asc ";
		return  this.commonDao.findListbySql(query, DirectoryEntity.class);
	}

	@Override
	public List<DirectoryEntity> findDirsDelBlackSubject(String subjectId,String userId) {
		String query="select d.* from eht_directory d where  d.subjectId='"+subjectId+"' and d.deleted="+Constants.DATA_NOT_DELETED+"  and d.id not in(select p.classpk from eht_group p , eht_group_user u where u.userid='"+userId +"' and  u.groupid=p.groupid )  order by d.createTime asc ";
		List<DirectoryEntity> list=this.commonDao.findListbySql(query, DirectoryEntity.class);
		Map <String, DirectoryEntity>map=new HashMap<String, DirectoryEntity>();
		for (DirectoryEntity directoryEntity : list) {
			map.put(directoryEntity.getId(), directoryEntity);
		}
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			DirectoryEntity directoryEntity = (DirectoryEntity) iterator.next();
			if(!directoryEntity.getPId().equals(subjectId)&&map.get(directoryEntity.getPId())==null){
				iterator.remove();
			}
		}
		return list;
	}
	@Override
	public void findUpDirs(String dirId,List<String> list) {
		DirectoryEntity  directoryEntity=getEntity(DirectoryEntity.class, dirId);
		if(directoryEntity.getParentId()!=null&&!directoryEntity.getParentId().equals("")){
			list.add(directoryEntity.getParentId());
			findUpDirs(directoryEntity.getParentId(),list);
		}
	}

}