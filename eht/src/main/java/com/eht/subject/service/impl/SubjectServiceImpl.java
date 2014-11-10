package com.eht.subject.service.impl;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServletRequest;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.jeecgframework.core.common.service.impl.CommonServiceImpl;
import org.jeecgframework.core.util.JSONHelper;
import org.jeecgframework.core.util.SendMailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.eht.common.annotation.RecordOperate;
import com.eht.common.constant.ActionName;
import com.eht.common.constant.Constants;
import com.eht.common.constant.RoleName;
import com.eht.common.enumeration.DataSynchAction;
import com.eht.common.enumeration.DataType;
import com.eht.common.util.AppContextUtils;
import com.eht.common.util.AppRequstUtiles;
import com.eht.common.util.FilePathUtil;
import com.eht.common.util.FileToolkit;
import com.eht.common.util.UUIDGenerator;
import com.eht.group.entity.Group;
import com.eht.group.service.GroupService;
import com.eht.log.service.SynchLogServiceI;
import com.eht.message.entity.MessageEntity;
import com.eht.message.entity.MessageUserEntity;
import com.eht.message.service.MessageServiceI;
import com.eht.note.entity.AttachmentEntity;
import com.eht.note.entity.NoteEntity;
import com.eht.note.service.AttachmentServiceI;
import com.eht.note.service.NoteServiceI;
import com.eht.resource.entity.ClassName;
import com.eht.resource.entity.ResourceAction;
import com.eht.resource.service.ResourceActionService;
import com.eht.resource.service.ResourcePermissionService;
import com.eht.role.entity.Role;
import com.eht.role.entity.RoleUser;
import com.eht.role.service.RoleService;
import com.eht.subject.entity.DirectoryEntity;
import com.eht.subject.entity.InviteMememberEntity;
import com.eht.subject.entity.SubjectEntity;
import com.eht.subject.entity.SubjectMht;
import com.eht.subject.entity.SujectSchedule;
import com.eht.subject.entity.ZipEntity;
import com.eht.subject.service.DirectoryServiceI;
import com.eht.subject.service.InviteMememberServiceI;
import com.eht.subject.service.SubjectServiceI;
import com.eht.system.bean.TreeData;
import com.eht.tag.entity.TagEntity;
import com.eht.tag.service.TagServiceI;
import com.eht.template.entity.TemplateEntity;
import com.eht.template.service.TemplateServiceI;
import com.eht.user.entity.AccountEntity;

@Service("subjectService")
@Transactional
public class SubjectServiceImpl extends CommonServiceImpl implements
		SubjectServiceI {

	@Autowired
	private ResourceActionService resourceActionService;

	@Autowired
	private ResourcePermissionService resourcePermissionService;

	@Autowired
	private GroupService groupService;

	@Autowired
	private RoleService roleService;

	@Autowired
	private DirectoryServiceI directoryService;

	@Autowired
	private InviteMememberServiceI inviteMememberServiceI;

	@Autowired
	private NoteServiceI noteService;

	@Autowired
	private TagServiceI tagServiceI;

	@Autowired
	private AttachmentServiceI attachmentServiceI;

	@Autowired
	private TemplateServiceI TemplateServiceI;

	@Autowired
	private  MessageServiceI MessageServiceI;
	
	@Autowired
	private  SynchLogServiceI synchLogService;
	
	@Override
	@RecordOperate(dataClass = DataType.SUBJECT, action = DataSynchAction.ADD, keyIndex = 0, keyMethod = "getId", timeStamp = "createTime", targetUser=1)
	public Serializable addSubject(SubjectEntity subject, String creator) {
		subject.setStatus(Constants.ENABLED);
		subject.setDeleted(Constants.DATA_NOT_DELETED);
		save(subject);
		Role ownerRole = roleService.findRoleByName(RoleName.OWNER);
		roleService.addRoleUser(subject.getId(), subject.getCreateUser(),
				ownerRole.getId(), creator, System.currentTimeMillis());
		ClassName c = resourceActionService
				.findResourceByName(SubjectEntity.class.getName());
		if (c == null) {
			c = new ClassName();
			c.setClassName(SubjectEntity.class.getName());
			resourceActionService.addResource(c);
		}
		Group group = groupService.addGroup(c.getClassNameId(),
				subject.getId(), subject.getSubjectName(), subject.getId(), 0L);
		// 给新增专题设置权限
		grantSubjectPermissions(subject);
		if (subject.getOldId() == null) {// 判断是否是导入专题
			// 给新增专题添加文档资料文件夹
			DirectoryEntity dir = new DirectoryEntity();
			dir.setId(UUIDGenerator.uuid());
			dir.setDeleted(0);
			dir.setCreateTime(new Date());
			dir.setCreateUser(subject.getCreateUser());
			dir.setDirName(Constants.SUBJECT_DOCUMENT_DIRNAME);
			dir.setSubjectId(subject.getId());
			dir.setParentId(null);
			directoryService.addDirectory(dir);
			// if(subject.getSubjectType() == Constants.SUBJECT_TYPE_M){
			// 给新增多人专题添加标签文件夹
			// directoryService.addDirectory(subject, null,
			// Constants.SUBJECT_TAG_DIRNAME);
			// 给新增多人专题添加回收站文件夹
			// directoryService.addDirectory(subject, null,
			// Constants.SUBJECT_RECYCLE_DIRNAME);
			// }
		}
		if (subject.getTemplateId() != null
				&& !subject.getTemplateId().equals("")) {
			TemplateEntity t = TemplateServiceI.getTemplate(subject
					.getTemplateId());
			if (t != null && !subject.getJosns().equals("")) {
				if (subject.getJosns().equals(t.getContent())) {
				} else {
					long cxout = TemplateServiceI.findUserTemplatesCout(subject
							.getCreateUser());
					cxout = cxout + 1;
					TemplateEntity dt = new TemplateEntity();
					dt.setCreateTime(subject.getCreateTime());
					dt.setTemplateName("自定义模板" + cxout);
					dt.setCreateUser(subject.getCreateUser());
					dt.setDeleted(Constants.DATA_NOT_DELETED);
					dt.setTemplateType(Constants.TEMPLATE_USER_DEFINED);
					dt.setContent(subject.getJosns());
					TemplateServiceI.addTemplate(dt);
				}
				saveJosnTemplate(subject.getJosns(), subject);
			}
		}
		return group.getGroupId();
	}

	private void saveJosnTemplate(String json, SubjectEntity subject) {
		JSONArray jsonArray = JSONArray.fromObject(json);
		List<DirectoryEntity> list = new ArrayList<DirectoryEntity>();
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject jObject = jsonArray.getJSONObject(i);
			DirectoryEntity ad = new DirectoryEntity();
			if (!jObject.getString("pId").equals("null")) {
				ad.setParentId(jObject.getString("pId"));
			}
			ad.setOldId(jObject.getString("id"));
			ad.setDirName(jObject.getString("name"));
			ad.setDeleted(Constants.DATA_NOT_DELETED);
			ad.setCreateUser(subject.getCreateUser());
			ad.setCreateTime(subject.getCreateTime());
			list.add(ad);
		}
		int k=1;
		for (DirectoryEntity directoryEntity : list) {
			 Date date=	new Date();
			 Calendar calendar = Calendar.getInstance();    
			 calendar.setTime(date);    
			 calendar.add(Calendar.SECOND, k);
			directoryEntity.setCreateTime( calendar.getTime());
			directoryEntity.setSubjectId(subject.getId());
			directoryEntity.setId(UUIDGenerator.uuid());
			directoryService.addDirectory(directoryEntity);
			k++;
		}
		for (DirectoryEntity directoryEntity : list) {
			if (directoryEntity.getParentId() != null) {
				if (directoryEntity.getParentId().equals("")) {
					directoryEntity.setParentId(null);
					continue;
				}
				for (DirectoryEntity directory : list) {
					if (directory.getOldId().equals(
							directoryEntity.getParentId())) {
						directoryEntity.setParentId(directory.getId());
					}
				}
			}
		}

	}

	/**
	 * 为添加的专题授权
	 * 
	 * @param subject
	 */
	private void grantSubjectPermissions(SubjectEntity subject) {
		String resourceName = SubjectEntity.class.getName();
		List<ResourceAction> list = resourceActionService
				.findActionsByName(resourceName);

		// 给owner角色授权,所有权限
		Role ownerRole = roleService.findRoleByName(RoleName.OWNER);
		int value = 0;
		for (ResourceAction ra : list) {
			value |= ra.getBitwiseValue();
		}
		resourcePermissionService.grantPermissions(resourceName,
				subject.getId(), ownerRole.getId(), value);

		// 给admin角色授权,所有权限除了删除专题权限
		Role adminRole = roleService.findRoleByName(RoleName.ADMIN);
		value = 0;
		for (ResourceAction ra : list) {
			if (!ra.getAction().equals(ActionName.DELETE)) {
				value |= ra.getBitwiseValue();
			}
		}
		resourcePermissionService.grantPermissions(resourceName,
				subject.getId(), adminRole.getId(), value);

		// 给editor角色授权
		Role editorRole = roleService.findRoleByName(RoleName.EDITOR);
		resourcePermissionService.grantPermissions(resourceName,
				subject.getId(), editorRole.getId(), ActionName.VIEW);

		// 给author角色授权
		Role authorRole = roleService.findRoleByName(RoleName.AUTHOR);
		resourcePermissionService.grantPermissions(resourceName,
				subject.getId(), authorRole.getId(), ActionName.VIEW);

		// 给reader角色授权
		Role readerRole = roleService.findRoleByName(RoleName.READER);
		resourcePermissionService.grantPermissions(resourceName,
				subject.getId(), readerRole.getId(), ActionName.VIEW);
	}

	/**
	 * 删除禁用专题、更新相关权限信息
	 * 
	 * @param subject
	 */
	private void revokeSubjectPermissions(SubjectEntity subject) {
		String resourceName = SubjectEntity.class.getName();
		resourcePermissionService.deletePermissionByPK(resourceName,
				subject.getId());
	}

	@Override
	@RecordOperate(dataClass = DataType.SUBJECT, action = DataSynchAction.UPDATE, keyIndex = 0, keyMethod = "getId", timeStamp = "updateTime")
	public void updateSubject(SubjectEntity subject) {
		updateEntitie(subject);
	}

	@Override
	public void updateSubject(SubjectEntity subject, boolean delNoteTag) {
		updateSubject(subject);
		if(delNoteTag){
			super.executeHql("update NoteEntity set tagId = null where subjectId = ? ", new Object[]{subject.getId()});
		}
		
	}

	
	@Override
	@RecordOperate(dataClass = DataType.SUBJECT, action = DataSynchAction.DELETE, keyIndex = 0, keyMethod = "getId", timeStamp = "updateTime")
	public void markDelSubject(SubjectEntity subject) {
		subject.setDeleted(Constants.DATA_DELETED);
		updateEntitie(subject);
	}

	@Override
	public void deleteSubject(String id) {
		SubjectEntity subject = getSubject(id);
		deleteSubject(subject);
	}

	@Override
	@RecordOperate(dataClass = DataType.SUBJECT, action = DataSynchAction.TRUNCATE, keyIndex = 0, keyMethod = "getId")
	public void deleteSubject(SubjectEntity subject) {
		//删除专题下的所有目录
		List<DirectoryEntity> dirList = directoryService.findByDetached(DetachedCriteria.forClass(DirectoryEntity.class).add(Restrictions.eq("subjectId", subject.getId())));
		for(DirectoryEntity dir : dirList){
			directoryService.deleteOnlyDirectory(dir);
			//删除与目录相关但与条目无关的文档资料
			super.executeHql("delete from AttachmentEntity  where noteId is null and directoryId = ?  ", new Object[]{dir.getId()});
		}
		
		//个人专题
		if(subject.getSubjectType() ==  Constants.SUBJECT_TYPE_P){
			//查找默认专题
			List<SubjectEntity> list = this.findByDetached(DetachedCriteria.forClass(SubjectEntity.class).add(Restrictions.eq("createUser", subject.getCreateUser())).add(Restrictions.eq("subjectName", "默认专题")));
			SubjectEntity defaultPersonSubject = null;
			if(list != null && !list.isEmpty()){
				defaultPersonSubject = list.get(0);
				//将专题下所条目状态修改为1
				super.executeHql("update AttachmentEntity set directoryId = null where noteId in (select id from NoteEntity where subjectId = ? )" ,  new Object[]{subject.getId()});
				super.executeHql("update NoteEntity set deleted = ? ,subjectId = ? , dirId = null where subjectId = ? ", new Object[]{Constants.DATA_DELETED,defaultPersonSubject.getId(),subject.getId()});
			}
		}else{
			//多人专题
			//把条目放到每个创建人员的回收站
			//查找所有创建人员的默认专题
			List<SubjectEntity> defaultSubjects = super.findHql("select distinct s from SubjectEntity s , NoteEntity n where s.createUser = n.createUser and  n.subjectId = ?  and  s.subjectName = '默认专题'", new Object[]{subject.getId()});
			for(SubjectEntity defaultSubject : defaultSubjects){
				//把某个用户在该专题下创建的条目，放入某个用户的回收站下
				super.executeHql("update AttachmentEntity set directoryId = null where noteId in (select id from NoteEntity where subjectId = ? and createUser = ? )" ,  new Object[]{subject.getId(),defaultSubject.getCreateUser()});
				super.executeHql("update NoteEntity set deleted = ? ,subjectId = ? , dirId = null where subjectId = ? and createUser = ? ", new Object[]{Constants.DATA_DELETED,defaultSubject.getId(),subject.getId(),defaultSubject.getCreateUser()});
			}
		}
		
		//删除专题
		super.delete(subject);
		Group g = groupService.findGroup(SubjectEntity.class.getName(),subject.getId());
		groupService.removeGUByGroupId(g.getGroupId());
		groupService.deleteGroup(g);
		revokeSubjectPermissions(subject);
	}

	@Override
	/*@RecordOperate(dataClass = DataType.SUBJECTUSER, action = DataSynchAction.ADD, keyIndex = {
			0, 0 }, targetUser = { 1, -1 }, timeStamp = { "", "" }, keyMethod = {
			"", "" })*/
	public void addSubjectMember(String subjectId, String userId, String roleId, String creator, long timestamp) {
		Group group = groupService.findGroup(SubjectEntity.class.getName(),
				subjectId);
		groupService.addGroupUser(group.getGroupId(), userId);
		String id = roleService.addRoleUser(subjectId, userId, roleId, creator, timestamp);
		
		synchLogService.generateAddSubjectUserLogs(id, subjectId, userId, roleId, DataSynchAction.ADD.toString(), creator, timestamp);
		synchLogService.generateUserSubjectAddLog(subjectId, userId, roleId);
	}

	@Override
	/*@RecordOperate(dataClass = { DataType.SUBJECT, DataType.SUBJECTUSER }, action = {
			DataSynchAction.DELETE,
			DataSynchAction.DELETE }, keyIndex = { 0, 0 }, targetUser = {
			1, -1 }, timeStamp = { "", "" }, keyMethod = { "", "" })*/
	public void removeSubjectMember(String subjectId, String userRoleId) {
		Group group = groupService.findGroup(SubjectEntity.class.getName(),subjectId);
		RoleUser u=roleService.get(RoleUser.class, userRoleId);
		groupService.removeGroupUser(group.getGroupId(), u.getUserId());
		roleService.removeRoleUser(u);
		
		synchLogService.generateDelSubjectUserLogs(userRoleId, subjectId, u.getUserId(), DataSynchAction.TRUNCATE.toString());
		synchLogService.generateUserSubjectDelLog(subjectId, u.getUserId());
	}

	@Override
	public SubjectEntity getSubject(Serializable id) {
		return get(SubjectEntity.class, id);
	}

	@Override
	public List<SubjectEntity> findSubjectByName(String subjectName) {
		DetachedCriteria dc = DetachedCriteria.forClass(SubjectEntity.class);
		dc.add(Restrictions.eq("deleted", Constants.DATA_NOT_DELETED));
		dc.add(Restrictions.eq("status", Constants.ENABLED));
		dc.add(Restrictions.eq("subjectName", subjectName));
		List<SubjectEntity> list = findByDetached(dc);
		return list;
	}
	
	@Override
	public List<SubjectEntity> findSubjectByParam(String subjectName, String userId, int subjectType) {
		DetachedCriteria dc = DetachedCriteria.forClass(SubjectEntity.class);
		dc.add(Restrictions.eq("deleted", Constants.DATA_NOT_DELETED));
		dc.add(Restrictions.eq("status", Constants.ENABLED));
		dc.add(Restrictions.eq("createUser", userId));
		dc.add(Restrictions.eq("subjectType", subjectType));
		dc.add(Restrictions.eq("subjectName", subjectName));
		
		List<SubjectEntity> list = findByDetached(dc);
		return list;
	}
	
	@Override
	public List<SubjectEntity> findSubjectByType(Integer subjectType) {
		DetachedCriteria dc = DetachedCriteria.forClass(SubjectEntity.class);
		dc.add(Restrictions.eq("deleted", Constants.DATA_NOT_DELETED));
		dc.add(Restrictions.eq("status", Constants.ENABLED));
		dc.add(Restrictions.eq("subjectType", subjectType));
		List<SubjectEntity> list = findByDetached(dc);
		return list;
	}

	@Override
	public Integer existsSubjectName(String subjectName, String subjectId) {
		List<SubjectEntity> list = findSubjectByName(subjectName);
		if (list == null || list.isEmpty()) {
			return -1;
		} else {
			if (subjectId != null && subjectId.equals(list.get(0).getId())) {
				return -1;
			}
			return list.get(0).getSubjectType();
		}
	}

	@Override
	public List<SubjectEntity> findUsersSubject(String userId) {
		List<SubjectEntity> mList = findHql(
				"select s from SubjectEntity s , RoleUser r where s.id=r.subjectId and s.deleted="
						+ Constants.DATA_NOT_DELETED
						+ " and s.status="
						+ Constants.ENABLED + " and r.userId=?",
				new Object[] { userId });
		return mList;
	}
	
	@Override
	public List<SubjectEntity> findUsersSubjectByType(String userId, int subjectType) {
		List<SubjectEntity> mList = findHql(
				"select s from SubjectEntity s , RoleUser r where s.id=r.subjectId and s.deleted="
						+ Constants.DATA_NOT_DELETED
						+ " and s.status="
						+ Constants.ENABLED
						+ " and s.subjectType=?"
						+ " and r.userId=?",
				new Object[] { subjectType, userId });
		return mList;
	}

	@Override
	public List<SubjectEntity> findPermissionSubject(String userId) {
		List<SubjectEntity> mList = findHql(
				"select s from SubjectEntity s , RoleUser r where s.id=r.subjectId and s.deleted="
						+ Constants.DATA_NOT_DELETED
						+ " and s.subjectType="
						+ Constants.SUBJECT_TYPE_M
						+ " and s.status="
						+ Constants.ENABLED + " and r.userId=?",
				new Object[] { userId });
		return mList;
	}

	@Override
	public List<SubjectEntity> findUserOwnSubject(String userId) {
		DetachedCriteria dc = DetachedCriteria.forClass(SubjectEntity.class);
		dc.add(Restrictions.eq("deleted", Constants.DATA_NOT_DELETED));
		dc.add(Restrictions.eq("status", Constants.ENABLED));
		dc.add(Restrictions.eq("createUser", userId));
		dc.add(Restrictions.eq("subjectType", Constants.SUBJECT_TYPE_P));
		List<SubjectEntity> list = findByDetached(dc);
		return list;
	}

	@Override
	public void inviteMemember(String email[], String types[],
			HttpServletRequest request, SubjectEntity SubjectEntity, String inviter)
			throws Exception {
		if (email != null) {
			String basePath = AppRequstUtiles.getAppUrl(request);
			for (int i = 0; i < email.length; i++) {
				InviteMememberEntity inviteMemember = new InviteMememberEntity();
				inviteMemember.setCreateUserId(inviter);
				inviteMemember.setCreateTimeStamp(System.currentTimeMillis());
				inviteMemember.setEmail(email[i]);
				Role ownerRole = null;
				String type=types[i];
				if(type==null){
					continue;
				}
				if (type.equals("1")) {
					ownerRole = roleService.findRoleByName(RoleName.ADMIN);
				} else if (type.equals("2")) {
					ownerRole = roleService.findRoleByName(RoleName.EDITOR);
				} else if (type.equals("3")) {
					ownerRole = roleService.findRoleByName(RoleName.AUTHOR);
				} else {
					ownerRole = roleService.findRoleByName(RoleName.READER);
				}
				inviteMemember.setRoleid(ownerRole.getId());
				inviteMemember.setSubjectid(SubjectEntity.getId());
				String id = inviteMememberServiceI.addInviteMemember(inviteMemember);
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("subject", SendMailUtil.getSubject());
				map.put("content", SendMailUtil.getContent());
				map.put("link",
						"<a href='"
								+ basePath
								+ "/subjectController/center/acceptInvitemember.dht?id="
								+ id + "'>" + SubjectEntity.getSubjectName()
								+ "专题邀请</a>");
				SendMailUtil.sendFtlMail(email[i], "邀请成员",
						"mailtemplate/testmail.ftl", map);
			}
		}
	}
  
	@Override
	public void inviteMemember(String  inviteMememberId,HttpServletRequest request)throws Exception {
			InviteMememberEntity InviteMememberEntity=get(InviteMememberEntity.class, inviteMememberId);
			if(InviteMememberEntity!=null){
				SubjectEntity SubjectEntity=get(SubjectEntity.class, InviteMememberEntity.getSubjectid());
				if(SubjectEntity!=null){
					String basePath = AppRequstUtiles.getAppUrl(request);
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("subject", SendMailUtil.getSubject());
					map.put("content", SendMailUtil.getContent());
					map.put("link",
							"<a href='"
									+ basePath
									+ "/subjectController/center/acceptInvitemember.dht?id="
									+ inviteMememberId + "'>" + SubjectEntity.getSubjectName()
									+ "专题邀请</a>");
					SendMailUtil.sendFtlMail(InviteMememberEntity.getEmail(), "邀请成员",
							"mailtemplate/testmail.ftl", map);
				}
			}
	}
	
	@Override
	public void acceptInviteMember(InviteMememberEntity inviteMememberEntity,
			AccountEntity user) throws Exception {
		String roleid = inviteMememberEntity.getRoleid();
		SubjectEntity SubjectEntity = getSubject(inviteMememberEntity.getSubjectid());
		if(SubjectEntity != null){
			RoleUser ru = roleService.findUserRole(user.getId(),SubjectEntity.getId());
			if (ru != null) {
				ru.setRoleId(roleid);
				roleService.updateRoleUser(ru);
			} else {
				addSubjectMember(SubjectEntity.getId(), user.getId(), roleid, inviteMememberEntity.getCreateUserId(), inviteMememberEntity.getCreateTimeStamp());
			}
		}
		delete(inviteMememberEntity);
	}

	@Override
	public void delInviteMember(String[] ids,String subjectId) throws Exception {
		for (int i = 0; i < ids.length; i++) {
			removeSubjectMember(subjectId, ids[i]);
		}
	}

	@Override
	public void updateInviteMemberRole(String[] ids, String type)
			throws Exception {
		Role ownerRole = null;
		if (type.equals("1")) {
			ownerRole = roleService.findRoleByName(RoleName.ADMIN);
		} else if (type.equals("2")) {
			ownerRole = roleService.findRoleByName(RoleName.EDITOR);
		} else if (type.equals("3")) {
			ownerRole = roleService.findRoleByName(RoleName.AUTHOR);
		} else {
			ownerRole = roleService.findRoleByName(RoleName.READER);
		}
		if (ownerRole != null) {
			for (int i = 0; i < ids.length; i++) {
				RoleUser u = get(RoleUser.class, ids[i]);
				String roleId = u.getRoleId();
				String newRoleId = ownerRole.getId();
				
				u.setRoleId(ownerRole.getId());
				roleService.updateEntitie(u);
				
				synchLogService.generateRecycleLogs(u.getSubjectId(), u.getUserId(), roleId, newRoleId);
			}
		}

	}
	
	
	//===============================================导出mht开始
	public SubjectMht  SubjectforMht(String subjectid,AccountEntity user){
		SubjectEntity subjectEntity = get(SubjectEntity.class, subjectid);
		SubjectMht mht=new SubjectMht();
		subjectEntity.setSubjectNameTitle(subjectEntity.getSubjectName()+"报告");
		List<DirectoryEntity> directoryList = directoryService.findDirsBySubject(subjectid, user.getId());
		List<NoteEntity> noteEntitylist =noteService.findNotesBySubject(subjectid, user.getId());
		List<NoteEntity> subjectNoteslist=getSubjectNotes( subjectEntity, noteEntitylist);
		List<DirectoryEntity> sortList=	getSubjectDirSort( subjectEntity, directoryList, noteEntitylist);
		mht.setSortList(sortList);
		mht.setSubjectEntity(subjectEntity);
		mht.setSubjectNoteslist(subjectNoteslist);
		return mht;
	}
	//让专题对章节进行排序 比如 1.1   1.2  1.3
	private List<DirectoryEntity>  getSubjectDirSort(SubjectEntity SubjectEntity,List<DirectoryEntity> directoryList,List<NoteEntity> noteEntitylist){
		List<DirectoryEntity> sortList= new ArrayList<DirectoryEntity>();
		List<DirectoryEntity> rootDirectoryList = new ArrayList<DirectoryEntity>();
		for (DirectoryEntity directoryEntity : directoryList) {
			if (directoryEntity.getPId().equals(SubjectEntity.getId())) {
				rootDirectoryList.add(directoryEntity);
			}
		}
		int title = 1;
		for (DirectoryEntity directoryEntity : rootDirectoryList) {
			if (directoryEntity.getDirName().equals(Constants.SUBJECT_DOCUMENT_DIRNAME)){ 
				setDirectorySort( directoryEntity, directoryList, noteEntitylist,title+"",true,sortList);
			}else{
				setDirectorySort( directoryEntity, directoryList, noteEntitylist,title+"",false,sortList);
			}
			title++;
		}
		return sortList;
	}
	
private void setDirectorySort(DirectoryEntity directoryEntity,List<DirectoryEntity> directoryList,List<NoteEntity> noteEntitylist, 
		String title,boolean iswendang,List<DirectoryEntity> sortList) {
		if (iswendang) {
			directoryEntity.setDirNameTitle(title+" "+directoryEntity.getDirName());
			List<AttachmentEntity> list = attachmentServiceI.findAttachmentByDir(directoryEntity.getId());
			directoryEntity.setAttachmentEntitylist(list);
		} else {
			directoryEntity.setDirNameTitle(title+" "+directoryEntity.getDirName());
			List<NoteEntity> noteEntityList = new ArrayList<NoteEntity>();
			for (NoteEntity noteEntity : noteEntitylist) {
				if (directoryEntity.getId().equals(noteEntity.getDirId())) {
					List<AttachmentEntity> lists = attachmentServiceI.findAttachmentByNote(noteEntity.getId(),Constants.FILE_TYPE_NORMAL);
					noteEntity.setAttachmentEntitylist(lists);
					noteEntityList.add(noteEntity);
				}
			}
			directoryEntity.setNoteEntitylist(noteEntityList);
		}
		sortList.add(directoryEntity);
		int cout = 1;
		for (DirectoryEntity directory : directoryList) {
			if (directoryEntity.getId().equals(directory.getPId())) {
				setDirectorySort(directory, directoryList, noteEntitylist,
						title + "." + (cout++), iswendang,sortList);
			}
		}
	}
	
	
	
	//取得专题下的条目 因为有的条目不属于某个目录
	private List<NoteEntity>  getSubjectNotes(SubjectEntity SubjectEntity,List<NoteEntity> noteEntitylist){
		List<NoteEntity> list=new ArrayList<NoteEntity>();
		for (NoteEntity noteEntity : noteEntitylist) {
			if (noteEntity.getDirId() == null|| noteEntity.getDirId().equals("")){
				List<AttachmentEntity> lists = attachmentServiceI.findAttachmentByNote(noteEntity.getId(),Constants.FILE_TYPE_NORMAL);
				noteEntity.setAttachmentEntitylist(lists);
				list.add(noteEntity);
			}
		}
		return list;
	}
//===============================================导出mht结束
	@Override
	public void showCatalogueSubject(SubjectEntity SubjectEntity,
			AccountEntity user, StringBuffer sb) {
		List<DirectoryEntity> directoryList = directoryService
				.findDirsBySubject(SubjectEntity.getId(), user.getId());
		List<NoteEntity> noteEntitylist = noteService.findNotesBySubject(
				SubjectEntity.getId(), user.getId());
		getSujectNotehtm(SubjectEntity, noteEntitylist, sb);
		List<DirectoryEntity> rootDirectoryList = new ArrayList<DirectoryEntity>();
		for (DirectoryEntity directoryEntity : directoryList) {
			if (directoryEntity.getPId().equals(SubjectEntity.getId())) {
				rootDirectoryList.add(directoryEntity);
			}
		}
		int title = 1;
		for (DirectoryEntity directoryEntity : rootDirectoryList) {
			if (directoryEntity.getDirName().equals(
					Constants.SUBJECT_DOCUMENT_DIRNAME)) {
				showDirectoryNote(directoryEntity, directoryList,
						noteEntitylist, sb, title + "", true);
			} else {
				showDirectoryNote(directoryEntity, directoryList,
						noteEntitylist, sb, title + "", false);
			}
			title++;
		}
	}

	private void showDirectoryNote(DirectoryEntity directoryEntity,
			List<DirectoryEntity> directoryList,
			List<NoteEntity> noteEntitylist, StringBuffer sb, String title,
			boolean iswendang) {
		if (iswendang) {
			getDirectoryNote(directoryEntity, sb, title);
		} else {
			getDirectoryNote(directoryEntity, noteEntitylist, sb, title);
		}
		int cout = 1;
		for (DirectoryEntity directory : directoryList) {
			if (directoryEntity.getId().equals(directory.getPId())) {
				showDirectoryNote(directory, directoryList, noteEntitylist, sb,
						title + "." + (cout++), iswendang);
			}
		}
	}

	private void getDirectoryNote(DirectoryEntity directoryEntity,
			List<NoteEntity> noteEntitylist, StringBuffer sb, String title) {
		sb.append("<tr>");
		sb.append("<td><h1>");
		sb.append(title + directoryEntity.getDirName());
		sb.append("</h1>");
		int firstcout = 0;
		for (NoteEntity noteEntity : noteEntitylist) {
			if (directoryEntity.getId().equals(noteEntity.getDirId())) {
				if (firstcout == 0) {
					sb.append("<table width='100%' border='0' cellspacing='0' cellpadding='0'>");
					sb.append("<tr>");
					sb.append("<td><div class='Table'>");
					sb.append("<table width='100%' border='0' cellspacing='0' cellpadding='0'>");
				}
				sb.append("<tr>");
				sb.append("<td><h2>" + noteEntity.getTitle() + "</h2>");
				sb.append(noteEntity.getContent());
				sb.append("</td>");
				sb.append("</tr>");
				List<AttachmentEntity> lists = attachmentServiceI
						.findAttachmentByNote(noteEntity.getId(),
								Constants.FILE_TYPE_NORMAL);
				if (lists.size() > 0) {
					String pathContent = AppContextUtils.getContextPath();
					sb.append("<tr><td>");
					for (AttachmentEntity attachmentEntity : lists) {
						sb.append("<a href='"
								+ pathContent
								+ "/noteController/front/downloadNodeAttach.dht?id="
								+ attachmentEntity.getId()
								+ "' target='_blank' class='link1b'>"
								+ attachmentEntity.getFileName()
								+ "</a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
					}
					sb.append("</td></tr>");
				}
				firstcout++;
			}
		}
		if (firstcout != 0) {
			sb.append("</table>");
			sb.append("</div>");
			sb.append("</td>");
			sb.append("</tr>");
			sb.append("</table>");
		}
		sb.append("</td>");
		sb.append("</tr>");
	}

	private void getDirectoryNote(DirectoryEntity directoryEntity,
			StringBuffer sb, String title) {
		List<AttachmentEntity> list = attachmentServiceI
				.findAttachmentByDir(directoryEntity.getId());
		String pathContent = AppContextUtils.getContextPath();
		sb.append("<tr>");
		sb.append("<td><h1>");
		sb.append(title);
		sb.append(directoryEntity.getDirName());
		sb.append("</h1>");
		for (AttachmentEntity attachmentEntity : list) {
			sb.append("<a href='" + pathContent
					+ "/noteController/front/downloadNodeAttach.dht?id="
					+ attachmentEntity.getId()
					+ "' target='_blank' class='link1b'>"
					+ attachmentEntity.getFileName()
					+ "</a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		}
		sb.append("</td>");
		sb.append("</tr>");

	}

	// 取得专题下的条目
	private void getSujectNotehtm(SubjectEntity SubjectEntity,
			List<NoteEntity> noteEntitylist, StringBuffer sb) {
		for (NoteEntity noteEntity : noteEntitylist) {
			if (noteEntity.getDirId() == null
					|| noteEntity.getDirId().equals("")) {
				sb.append("<tr>");
				sb.append("<td><h2>");
				sb.append(noteEntity.getTitle());
				sb.append("</h2>");
				sb.append(noteEntity.getContent());
				sb.append("</td>");
				sb.append("</tr>");
				List<AttachmentEntity> lists = attachmentServiceI.findAttachmentByNote(noteEntity.getId(),Constants.FILE_TYPE_NORMAL);
				if (lists.size() > 0) {
					String pathContent = AppContextUtils.getContextPath();
					sb.append("<tr><td>");
					for (AttachmentEntity attachmentEntity : lists) {
						sb.append("<a href='"
								+ pathContent
								+ "/noteController/front/downloadNodeAttach.dht?id="
								+ attachmentEntity.getId()
								+ "' target='_blank' class='link1b'>"
								+ attachmentEntity.getFileName()
								+ "</a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
					}
					sb.append("</td></tr>");
				}
			}
		}
	}
//=========================================================================页面展现报告结束
	@Override
	public void exportSuject(String subjectId,String path,String basePath, AccountEntity user,String ids[])  {
		String  message=null;
		ZipOutputStream zos =null;
		FileOutputStream f=null;
		SubjectEntity SubjectEntity=null;
		
		String uuid=UUIDGenerator.uuid();
		String zipPath=new StringBuffer(path).append("/zip/").append(uuid).append("/").append(uuid).append(".zip").toString();
		Document document=createDocument();
		Element root = createRootXml(document);
		File file=new File(zipPath);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();// 创建根目录
		}
		try {
			f=new FileOutputStream(file);
			zos = new ZipOutputStream(f);
			SubjectEntity = get(SubjectEntity.class, subjectId);
			List<DirectoryEntity> directoryList = directoryService.findDirsByIds(ids);
			List<NoteEntity> noteEntitylist = noteService.findNotesBySubject(SubjectEntity.getId(), user.getId());
			List<TagEntity> list = tagServiceI.findTagBySubject(subjectId);
			createSubjectXml(root, SubjectEntity);
			List<DirectoryEntity> listk = createDirectoryXml(root, directoryList,zos, path,user);
			for (Iterator iterator = noteEntitylist.iterator(); iterator.hasNext();) {
				NoteEntity noteEntity = (NoteEntity) iterator.next();
				if (noteEntity.getDirId() != null&& !noteEntity.getDirId().equals("")) {
					boolean remove = true;
					for (DirectoryEntity directoryEntity : listk) {
						if (directoryEntity.getId().equals(noteEntity.getDirId())) {
							remove = false;
							break;
						}
					}
					if (remove) {
						iterator.remove();
					}
				}
			}
			createNoteXml(root, noteEntitylist, zos, path);
			createTagXml(root, list);
			createXml(document, zos);
			ZipEntity z=new ZipEntity();
			z.setId(uuid);
			z.setPath(zipPath);
			z.setCreateUser(user.getId());
			save(z);
			 message="<a  target='_blank' href='"
					+ basePath
					+ "/subjectController/front/downzip.dht?id="
					+ uuid + "'>" + SubjectEntity.getSubjectName()
					+ "下载</a>";
		} catch (Exception e) {
			SujectSchedule.remveSchedule(user.getId());
			message=SubjectEntity.getSubjectName()+"下载失败";
		}finally{
			 if(zos!=null){
				 try {
					zos.flush();
				} catch (IOException e) {
				}
			 }
			if(zos!=null){
				try {
					zos.close();
				} catch (IOException e) {
				}
			}
			if(f!=null){
				try {
					f.close();
				} catch (IOException e) {
				}
			}
		}
		MessageEntity  m=new MessageEntity();
		 m.setId(UUIDGenerator.uuid());
		 m.setContent( message);
		 m.setCreateTime(new Date());
		 m.setCreateUser(user.getId());
		 m.setMsgType(Constants.MSG_USER_TYPE);
		 m.setUserIsRead(Constants.NOT_READ_OBJECT);
		 MessageServiceI.save(m);
		 MessageUserEntity k=new MessageUserEntity();
		 k.setIsRead(Constants.NOT_READ_OBJECT);
		 k.setMessageId(m.getId());
		 k.setUserId(user.getId());
		 MessageServiceI.save(k);
		 SujectSchedule.remveSchedule(user.getId());
	}
	
	private void createXml(Document document, ZipOutputStream zos)throws IOException {
		OutputFormat opf;
		StringWriter sw = new StringWriter();
		XMLWriter xmlWriter = new XMLWriter(sw);
		xmlWriter.write(document);
		xmlWriter.close();
		opf = OutputFormat.createCompactFormat();
		opf.setEncoding("UTF-8");
		InputStream is = null;
		try {
			zos.putNextEntry(new ZipEntry("suject.xml"));
			byte[] by = sw.toString().getBytes("UTF-8");
			zos.write(by, 0, by.length);
			zos.flush();
		} catch (Exception e) {
			throw new IOException();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (sw != null) {
				sw.close();
			}
		}

	}
    
	private Document createDocument() {
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("UTF-8");
		return document;
	}
	
	private Element createRootXml(Document document) {
		Element root = document.addElement("root");
		return root;
	}

	private void createSubjectXml(Element root, SubjectEntity SubjectEntity) {
		Element suject = root.addElement("suject");
		suject.addElement("type").addText(SubjectEntity.getSubjectType() + "");
		suject.addElement("subjectName").addCDATA(
				SubjectEntity.getSubjectName());
		suject.addElement("id").addText(SubjectEntity.getId());
	}

	private List<DirectoryEntity> createDirectoryXml(Element root,List<DirectoryEntity> directoryList, ZipOutputStream zos,String cotextpath,AccountEntity user) {
		List<DirectoryEntity> listk = new ArrayList<DirectoryEntity>();
		List<DirectoryEntity> rootDirectoryList = new ArrayList<DirectoryEntity>();
		for (DirectoryEntity directoryEntity : directoryList) {
			if (directoryEntity.getParentId() == null
					|| directoryEntity.getParentId().equals("")) {
				rootDirectoryList.add(directoryEntity);
			}
		}
		for (DirectoryEntity directoryEntity : rootDirectoryList) {
			if (directoryEntity.getDirName().equals(
					Constants.SUBJECT_DOCUMENT_DIRNAME)) {
				createDirectoryXml(root, directoryList, zos, cotextpath,
						directoryEntity, true, listk,user);
			} else {
				createDirectoryXml(root, directoryList, zos, cotextpath,
						directoryEntity, false, listk,user);
			}
		}
		return listk;
	}

	private void createDirectoryXml(Element root,
			List<DirectoryEntity> directoryList, ZipOutputStream zos,
			String cotextpath, DirectoryEntity directoryEntity,
			boolean iswendang, List<DirectoryEntity> listk,AccountEntity user) {
		listk.add(directoryEntity);
		Element directory = root.addElement("directory");
		directory.addElement("dirName").addCDATA(directoryEntity.getDirName());
		if (directoryEntity.getParentId() == null) {
			directory.addElement("parentId").addText("");
		} else {
			directory.addElement("parentId").addText(
					directoryEntity.getParentId());
		}
		directory.addElement("id").addText(directoryEntity.getId());
		if (iswendang) {
			List<AttachmentEntity> list = attachmentServiceI
					.findAttachmentByDir(directoryEntity.getId());
			createAttachmentXml(root, list, zos, cotextpath);
		}
		SujectSchedule.putSchedule(user.getId());
		for (DirectoryEntity directorylist : directoryList) {
			if (directorylist.getPId().equals(directoryEntity.getId())) {
				createDirectoryXml(root, directoryList, zos, cotextpath,
						directorylist, iswendang, listk,user);
			}
		}
		
	}

	private void createTagXml(Element root, List<TagEntity> list) {
		for (TagEntity tagEntity : list) {
			Element tag = root.addElement("tag");
			tag.addElement("name").addCDATA(tagEntity.getName());
			tag.addElement("id").addText(tagEntity.getId());
			if (tagEntity.getParentId() != null) {
				tag.addElement("parentId").addText(tagEntity.getParentId());
			} else {
				tag.addElement("parentId").addText("");
			}

		}
	}

	private void createNoteXml(Element root, List<NoteEntity> noteEntitylist,
			ZipOutputStream zos, String cotextpath) throws IOException {
		for (NoteEntity noteEntity : noteEntitylist) {
			Element note = root.addElement("note");
			note.addElement("id").addText(noteEntity.getId());
			note.addElement("title").addCDATA(noteEntity.getTitle());
			if (noteEntity.getDirId() != null) {
				note.addElement("dirId").addText(noteEntity.getDirId());
			} else {
				note.addElement("dirId").addText("");
			}
			note.addElement("md5").addText(noteEntity.getMd5());
			if (noteEntity.getTagId() != null) {
				note.addElement("tagId").addText(noteEntity.getTagId());
			} else {
				note.addElement("tagId").addText("");
			}
			String uuidFiename = UUIDGenerator.uuid() + ".txt";
			note.addElement("filepath").addText(uuidFiename);
			byte[] buf = noteEntity.getContent().getBytes();
			zos.putNextEntry(new ZipEntry(uuidFiename));
			zos.write(buf);
			zos.flush();
			List<AttachmentEntity> list = attachmentServiceI
					.findAttachmentByNote(noteEntity.getId(),
							Constants.FILE_TYPE_NORMAL);
			createAttachmentXml(root, list, zos, cotextpath);
		}
	}

	private void createAttachmentXml(Element root, List<AttachmentEntity> list,
			ZipOutputStream zos, String cotextpath) {
		for (AttachmentEntity attachmentEntity : list) {
			String uuidFiename = UUIDGenerator.uuid() + ".zip";
			String filePath = attachmentEntity.getFilePath() + "/"
					+ attachmentEntity.getFileName().substring(0,attachmentEntity.getFileName().lastIndexOf("."))+".zip";
			InputStream is = null;
			try {
				byte[] buf = new byte[1024];
				int length = 0;
				is = new FileInputStream(filePath);
				BufferedInputStream bis = new BufferedInputStream(is);
				zos.putNextEntry(new ZipEntry(uuidFiename));
				while ((length = bis.read(buf)) > 0) {
					zos.write(buf, 0, length);
				}
				zos.flush();
				Element attachment = root.addElement("attachment");
				attachment.addElement("fileName").addCDATA(
						attachmentEntity.getFileName());
				attachment.addElement("suffix").addText(
						attachmentEntity.getSuffix());
				if (attachmentEntity.getNoteId() != null) {
					attachment.addElement("noteId").addText(
							attachmentEntity.getNoteId());
				} else {
					attachment.addElement("noteId").addText("");
				}
				//attachment.addElement("md5").addText(attachmentEntity.getMd5());
				if (attachmentEntity.getDirectoryId() != null) {
					attachment.addElement("directoryId").addText(
							attachmentEntity.getDirectoryId());
				} else {
					attachment.addElement("directoryId").addText("");
				}
				attachment.addElement("filepath").addText(uuidFiename);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
//===================导出结束
	@Override
	public void leadinginSuject(HttpServletRequest request) throws Exception {
		String uuid = UUIDGenerator.uuid();
		String realPath = request.getSession().getServletContext()
				.getRealPath("/")
				+ "/";
		AccountEntity user = (AccountEntity) request.getSession(false)
				.getAttribute(Constants.SESSION_USER_ATTRIBUTE);
		String userId = user.getId();
		File savefile = new File(realPath + Constants.PATH + uuid + "/1.zip");
		if (!savefile.getParentFile().exists()) {
			savefile.getParentFile().mkdirs();// 创建根目录
		}
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
		for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
			MultipartFile mf = entity.getValue();// 获取上传文件对象
			FileToolkit.copyFileFromStream(mf.getInputStream(), savefile, true);
		}
		ZipFile zipFile = null;
		ZipEntry zipEntry = null;
		try {
			zipFile = new ZipFile(savefile);
			zipEntry = zipFile.getEntry("suject.xml");
			if (zipEntry != null) {
				InputStream inputStream = zipFile.getInputStream(zipEntry);
				parserXml(inputStream, zipFile, userId, realPath,request);
			}
			savefile.delete();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception();
		}finally{
			if(zipFile!=null){
				zipFile.close();
			}
		}

	}

	public void parserXml(InputStream inputStream, ZipFile zipFile,
			String userId, String realPath,HttpServletRequest request) throws Exception {
		SAXReader saxReader = new SAXReader();
		try {
			Document document = saxReader.read(inputStream);
			Element root = document.getRootElement();
			// 读取专题
			SubjectEntity subjectEntity = readSubjectXml(root, userId, realPath);
			List<DirectoryEntity> directoryList = readDirectoryXml(root,
					userId, realPath);
			List<NoteEntity> noteList = readNoteXml(root, userId, realPath,
					zipFile);
			List<TagEntity> tagList = readTagXml(root, userId);
			List<AttachmentEntity> attachmentList = readAttachmentXml(root,
					userId, realPath, zipFile);
			subjectEntity.setId(UUIDGenerator.uuid());
			subjectEntity.setSubjectName(request.getParameter("subjectName"));
			addSubject(subjectEntity, subjectEntity.getCreateUser());
			for (DirectoryEntity directoryEntity : directoryList) {
				directoryEntity.setSubjectId(subjectEntity.getId());
				directoryEntity.setId(UUIDGenerator.uuid());
			}
			try {
				int k=0;
				for (DirectoryEntity directoryEntity : directoryList) {
					 Date date=	new Date();
					 Calendar calendar = Calendar.getInstance();    
					 calendar.setTime(date);    
					 calendar.add(Calendar.SECOND, k);
					directoryEntity.setCreateTime( calendar.getTime());
					if (directoryEntity.getParentId() != null) {
						if (directoryEntity.getParentId().equals("")) {
							directoryEntity.setParentId(null);
							directoryService.addDirectory(directoryEntity);
							continue;
						}
						for (DirectoryEntity directory : directoryList) {
							if (directory.getOldId().equals(
									directoryEntity.getParentId())) {
								directoryEntity.setParentId(directory.getId());
							}
						}
					}
					directoryService.addDirectory(directoryEntity);
				}
				for (TagEntity tagEntity : tagList) {
					tagEntity.setSubjectId(subjectEntity.getId());
					tagEntity.setId(UUIDGenerator.uuid());
				}
				for (TagEntity tagEntity : tagList) {
					if (tagEntity.getParentId() != null) {
						if (tagEntity.getParentId().equals("")) {
							tagEntity.setParentId(null);
							tagServiceI.addTag(tagEntity);
							continue;
						}
						for (TagEntity tag : tagList) {
							if (tag.getOldId().equals(tagEntity.getParentId())) {
								tagEntity.setParentId(tag.getId());
							}
						}
					}
					tagServiceI.addTag(tagEntity);
				}
				for (NoteEntity noteEntity : noteList) {
					noteEntity.setSubjectId(subjectEntity.getId());
					noteEntity.setId(UUIDGenerator.uuid());
					if (noteEntity.getDirId() != null) {
						if (!noteEntity.getDirId().equals("")) {
							for (DirectoryEntity directoryEntity : directoryList) {
								if (directoryEntity.getOldId().equals(
										noteEntity.getDirId())) {
									noteEntity.setDirId(directoryEntity.getId());
								}
							}
						} else {
							noteEntity.setDirId(null);
						}
					}
					if (noteEntity.getTagId() != null) {
						if (!noteEntity.getTagId().equals("")) {
							boolean savetagId=false;
							for (TagEntity tagEntity : tagList) {
								if (tagEntity.getOldId().equals(
										noteEntity.getTagId())) {
									savetagId=true;
									noteEntity.setTagId(tagEntity.getId());
								}
							}
							if(!savetagId){
								noteEntity.setTagId(null);
							}
						} else {
							noteEntity.setTagId(null);
						}

					}
					noteEntity.setCreateUser(userId);
					noteEntity.setCreateTime(new Date());
					noteService.saveNoteHtml(noteEntity);
					noteService.addNote(noteEntity);
				}

				for (AttachmentEntity attachmentEntity : attachmentList) {
					if (attachmentEntity.getNoteId() != null) {
						if (!attachmentEntity.getNoteId().equals("")) {
							for (NoteEntity noteEntity : noteList) {
								if (noteEntity.getOldId().equals(
										attachmentEntity.getNoteId())) {
									attachmentEntity.setNoteId(noteEntity
											.getId());
								}
							}
						} else {
							attachmentEntity.setNoteId(null);
						}

					}
					if (attachmentEntity.getDirectoryId() != null) {
						if (!attachmentEntity.getDirectoryId().equals("")) {
							for (DirectoryEntity directoryEntity : directoryList) {
								if (directoryEntity.getOldId().equals(attachmentEntity.getDirectoryId())) {
									attachmentEntity.setDirectoryId(directoryEntity.getId());
								}
							}
						} else {
							attachmentEntity.setDirectoryId(null);

						}
					}
					attachmentEntity.setId(UUIDGenerator.uuid());
					readAttachmentwriteFile(attachmentEntity, zipFile,
							subjectEntity.getId(),
							attachmentEntity.getDirectoryId(),
							attachmentEntity.getNoteId());
					attachmentServiceI.addAttachment(attachmentEntity);
				}
			} catch (Exception e) {
				throw new IOException();
			}

		} catch (DocumentException e) {
			throw new IOException();
		}

	}

	private SubjectEntity readSubjectXml(Element root, String userId,
			String realPath) {
		Element suject = root.element("suject");
		String sujectName = suject.element("subjectName").getText();
		String type = suject.element("type").getText();
		String oldId = suject.element("id").getText();
		SubjectEntity subjectEntity = new SubjectEntity();
		subjectEntity.setCreateTime(new Date());
		subjectEntity.setUpdateTime(new Date());
		subjectEntity.setDeleted(Constants.DATA_NOT_DELETED);
		subjectEntity.setStatus(Constants.ENABLED);
		subjectEntity.setSubjectType(Integer.valueOf(type));
		subjectEntity.setSubjectName(sujectName);
		subjectEntity.setCreateUser(userId);
		subjectEntity.setUpdateUser(userId);
		subjectEntity.setOldId(oldId);
		return subjectEntity;
	}

	private List<DirectoryEntity> readDirectoryXml(Element root, String userId,
			String realPath) {
		List<DirectoryEntity> list = new ArrayList<DirectoryEntity>();
		for (Iterator<Element> iterator = root.elementIterator("directory"); iterator
				.hasNext();) {
			Element elementDirectory = iterator.next();
			String parentId = elementDirectory.element("parentId").getText();
			String dirName = elementDirectory.element("dirName").getText();
			String id = elementDirectory.element("id").getText();
			DirectoryEntity d = new DirectoryEntity();
			d.setCreateTime(new Date());
			d.setUpdateTime(new Date());
			d.setCreateUser(userId);
			d.setUpdateUser(userId);
			d.setDeleted(Constants.DATA_NOT_DELETED);
			d.setDirName(dirName);
			d.setParentId(parentId);
			d.setOldId(id);
			list.add(d);
		}
		return list;
	}

	private List<NoteEntity> readNoteXml(Element root, String userId,
			String realPath, ZipFile zipFile) {
		List<NoteEntity> list = new ArrayList<NoteEntity>();
		for (Iterator<Element> iterator = root.elementIterator("note"); iterator
				.hasNext();) {
			Element elementnote = iterator.next();
			String id = elementnote.element("id").getText();
			String title = elementnote.element("title").getText();
			String dirId = elementnote.element("dirId").getText();
			String md5 = elementnote.element("md5").getText();
			String tagId = elementnote.element("tagId").getText();
			String filepath = elementnote.element("filepath").getText();
			NoteEntity noteEntity = new NoteEntity();
			noteEntity.setTitle(title);
			noteEntity.setDirId(dirId);
			noteEntity.setMd5(md5);
			noteEntity.setTagId(tagId);
			noteEntity.setDeleted(Constants.DATA_NOT_DELETED);
			noteEntity.setOldId(id);
			ZipEntry zipEntry = zipFile.getEntry(filepath);
			StringBuffer sb = new StringBuffer("");
			if (zipEntry != null) {
				try {
					InputStream inputStream = zipFile.getInputStream(zipEntry);
					InputStreamReader read = new InputStreamReader(inputStream);
					BufferedReader bufferedReader = new BufferedReader(read);
					String lineTxt = null;
					while ((lineTxt = bufferedReader.readLine()) != null) {
						sb.append(lineTxt);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
			noteEntity.setContent(sb.toString());
			list.add(noteEntity);
		}
		return list;
	}

	private List<TagEntity> readTagXml(Element root, String userId) {
		List<TagEntity> list = new ArrayList<TagEntity>();
		for (Iterator<Element> iterator = root.elementIterator("tag"); iterator
				.hasNext();) {
			Element elementtag = iterator.next();
			String name = elementtag.element("name").getText();
			String id = elementtag.element("id").getText();
			String parentId = elementtag.element("parentId").getText();
			TagEntity tagEntity = new TagEntity();
			tagEntity.setCreateTime(new Date());
			tagEntity.setUpdateTime(new Date());
			tagEntity.setCreateUser(userId);
			tagEntity.setUpdateUser(userId);
			tagEntity.setName(name);
			tagEntity.setOldId(id);
			tagEntity.setDeleted(Constants.DATA_NOT_DELETED);
			tagEntity.setParentId(parentId);
			list.add(tagEntity);
		}
		return list;
	}

	private List<AttachmentEntity> readAttachmentXml(Element root,
			String userId, String realPath, ZipFile zipFile) throws IOException {
		List<AttachmentEntity> list = new ArrayList<AttachmentEntity>();
		for (Iterator<Element> iterator = root.elementIterator("attachment"); iterator
				.hasNext();) {
			Element elementtag = iterator.next();
			String fileName = elementtag.element("fileName").getText();
			String suffix = elementtag.element("suffix").getText();
			String noteId = elementtag.element("noteId").getText();
			String directoryId = elementtag.element("directoryId").getText();
			String filepath = elementtag.element("filepath").getText();
			AttachmentEntity AttachmentEntity = new AttachmentEntity();
			AttachmentEntity.setCreateTime(new Date());
			AttachmentEntity.setUpdateTime(new Date());
			AttachmentEntity.setCreateUser(userId);
			AttachmentEntity.setUpdateUser(userId);
			AttachmentEntity.setFileName(fileName);
			AttachmentEntity.setSuffix(suffix);
			AttachmentEntity.setNoteId(noteId);
			AttachmentEntity.setStatus(Constants.FILE_TRANS_COMPLETED);
			AttachmentEntity.setDirectoryId(directoryId);
			AttachmentEntity.setDeleted(Constants.DATA_NOT_DELETED);
			AttachmentEntity.setTempFilePath(filepath);
			list.add(AttachmentEntity);
		}
		return list;
	}

	private void readAttachmentwriteFile(AttachmentEntity AttachmentEntity,
			ZipFile zipFile, String subjectId, String dirId, String nodeId)
			throws IOException {
		ZipEntry zipEntry = zipFile
				.getEntry(AttachmentEntity.getTempFilePath());
		if (zipEntry != null) {
			String realPathyuhao = FilePathUtil.getFileUploadPath(subjectId,
					dirId, nodeId);
			String filesavePath = realPathyuhao + "/"
					+ AttachmentEntity.getFileName().substring(0,AttachmentEntity.getFileName().lastIndexOf("."))+".zip";
			File saveFile = new File(filesavePath);
			if (!saveFile.getParentFile().exists()) {
				saveFile.getParentFile().mkdirs();
			}
			OutputStream outputStream = null;
			InputStream inputStream = null;
			try {
				byte b[] = new byte[1024];
				int length;
				outputStream = new FileOutputStream(saveFile);
				inputStream = zipFile.getInputStream(zipEntry);
				while ((length = inputStream.read(b)) > 0) {
					outputStream.write(b, 0, length);
				}
				outputStream.flush();
				AttachmentEntity.setFilePath(realPathyuhao);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (outputStream != null) {
					try {
						outputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			}

		}

	}

	@Override
	public String treeSubject(String subjectId,String userId,boolean remvdocument) {
		SubjectEntity subjectEntity=getEntity(SubjectEntity.class, subjectId);
		List<TreeData> listTreeData =new ArrayList<TreeData>();
		TreeData treeData = new TreeData();
		treeData.setName(subjectEntity.getSubjectName());
		treeData.setId(subjectEntity.getId());
		treeData.setIcon(AppContextUtils.getContextPath() + "/webpage/front/images/tree/subject.png");
		treeData.setpId("-1");
		treeData.setOpen("true");
		treeData.setBranchId(subjectEntity.getSubjectType()+"");
		treeData.setChecked("true");
		treeData.setDataType("SUBJECT");
		listTreeData.add(treeData);
		List<DirectoryEntity> list=	directoryService.findDirsBySubjectOderByTime(subjectId,true,false);
		if(remvdocument){
			findRemoveDirDOCUMENT(list,subjectId);
		}
		for (DirectoryEntity directoryEntity : list) {
			TreeData t = new TreeData();
			t.setName(directoryEntity.getDirName());
			t.setId(directoryEntity.getId());
			t.setpId(directoryEntity.getPId());
			t.setChecked("true");
			listTreeData.add(t);
		}
		return JSONHelper.collection2json(listTreeData);
	}
	
	private void findRemoveDirDOCUMENT(List<DirectoryEntity> list,String subjectId){
		List<DirectoryEntity> removeList =new ArrayList<DirectoryEntity>();
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			DirectoryEntity directoryEntity = (DirectoryEntity) iterator.next();
			if(directoryEntity.getDirName().equals(Constants.SUBJECT_DOCUMENT_DIRNAME)&&subjectId.equals(directoryEntity.getPId())){
				removeDir(list,directoryEntity.getId(),removeList);
				removeList.add(directoryEntity);
				break;
			}
		}
		list.removeAll(removeList);
	}
	private void removeDir(List<DirectoryEntity> list,String pid,List<DirectoryEntity> removeList){
		Iterator<DirectoryEntity> k=list.iterator();
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			DirectoryEntity directoryEntity = (DirectoryEntity) iterator.next();
			if(directoryEntity.getPId().equals(pid)){
				removeDir(list,directoryEntity.getId(),removeList);
				removeList.add(directoryEntity);
			}
		}
	}
 
}