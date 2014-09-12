package com.eht.note.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.jeecgframework.core.common.service.impl.CommonServiceImpl;
import org.jeecgframework.core.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

import com.eht.comment.service.CommentServiceI;
import com.eht.common.annotation.RecordOperate;
import com.eht.common.constant.Constants;
import com.eht.common.enumeration.DataSynchAction;
import com.eht.common.enumeration.DataType;
import com.eht.common.page.PageResult;
import com.eht.common.util.CollectionUtil;
import com.eht.common.util.FilePathUtil;
import com.eht.common.util.HtmlParser;
import com.eht.common.util.MD5FileUtil;
import com.eht.group.entity.Group;
import com.eht.group.entity.GroupUser;
import com.eht.group.service.GroupService;
import com.eht.note.entity.NoteEntity;
import com.eht.note.entity.NoteUserEntity;
import com.eht.note.entity.NoteVersionEntity;
import com.eht.note.service.AttachmentServiceI;
import com.eht.note.service.NoteServiceI;
import com.eht.resource.entity.ClassName;
import com.eht.resource.service.ResourceActionService;
import com.eht.subject.entity.DirectoryEntity;
import com.eht.subject.entity.SubjectEntity;
import com.eht.subject.service.DirectoryServiceI;
import com.eht.user.entity.AccountEntity;
import com.eht.user.service.AccountServiceI;
import com.eht.webservice.service.util.DataSynchizeUtil;

@Service("noteService")
@Transactional
public class NoteServiceImpl extends CommonServiceImpl implements NoteServiceI {

	@Autowired
	private AccountServiceI accountService;

	@Autowired
	private ResourceActionService resourceActionService;

	@Autowired
	private GroupService groupService;

	@Autowired
	private DirectoryServiceI directoryService;

	@Autowired
	private AttachmentServiceI attachmentService;
	
	@Autowired
	private CommentServiceI commentService;
	
	@Override
	public List<NoteEntity> findNotesByIds(String[] ids) { 
		String query = "from NoteEntity n where id=-1 ";
		if(ids!=null&&ids.length>0){
			for(String id:ids){
				query += " or n.id = '"+id+"' "; 
			}
		}
		List<NoteEntity> list = findByQueryString(query);
		return list;
	}
	
	@Override
	public List<AccountEntity> getShareEmail() {
		List<AccountEntity> accounts = new ArrayList<AccountEntity>();
		AccountEntity account = accountService.getUser4Session();

		String query = "select u.* from  eht_user u ,(select DISTINCT(r1.userid)userid  from eht_user_role r1 ,eht_user_role r2 " + " where r2.groupid = r1.groupid and r2.userid='" + account.getId() + "' and " + " r1.userid!='" + account.getId()
				+ "') og where og.userid=u.id ";
		accounts = this.commonDao.findListbySql(query, AccountEntity.class);
		return accounts;
	}
	
	@Override
	public List<AccountEntity> getShareEmail(String searchField) {
		List<AccountEntity> accounts = new ArrayList<AccountEntity>();
		AccountEntity account = accountService.getUser4Session();

		String query = "select u.* from  eht_user u ,(select DISTINCT(r1.userid)userid  from eht_user_role r1 ,eht_user_role r2 " + " where r2.groupid = r1.groupid and r2.userid='" + account.getId() + "' and " + " r1.userid!='" + account.getId()
				+ "') og where og.userid=u.id and (u.email like '" + searchField + "%' or u.username like '" + searchField + "%')  limit 5";
		accounts = this.commonDao.findListbySql(query, AccountEntity.class);
		return accounts;
	}

	@Override
	public List<AccountEntity> getShareEmailbyPage(String searchField, int start, int end) {
		List<AccountEntity> accounts = new ArrayList<AccountEntity>();
		AccountEntity account = accountService.getUser4Session();

		String query = "select u.* from  eht_user u ,(select DISTINCT(r1.userid)userid  from eht_user_role r1 ,eht_user_role r2 " + " where r2.groupid = r1.groupid and r2.userid='" + account.getId() + "' and " + " r1.userid!='" + account.getId()
				+ "') og where og.userid=u.id and u.email like '" + searchField + "%'  limit " + start + "," + end;
		accounts = this.commonDao.findListbySql(query, AccountEntity.class);
		return accounts;
	}

	@Override
	@RecordOperate(dataClass = DataType.NOTE, action = DataSynchAction.ADD, keyIndex = 0, keyMethod = "getId", timeStamp = "createTime")
	public Serializable addNote(NoteEntity note) {
		save(note);
		noteRead(note.getId(), note.getCreateUser());
		
		ClassName c = resourceActionService.findResourceByName(NoteEntity.class.getName());
		if (c == null) {
			c = new ClassName();
			c.setClassName(NoteEntity.class.getName());
			resourceActionService.addResource(c);
		}
		long parentGroupId = 0L;
		if(!StringUtil.isEmpty(note.getSubjectId())){
			Group group = groupService.findGroup(SubjectEntity.class.getName(), note.getSubjectId());
			parentGroupId = group.getGroupId();
		}
		Group group = groupService.addGroup(c.getClassNameId(), note.getId(), note.getTitle(), note.getId(), parentGroupId);
		return group.getGroupId();
	}

	@Override
	@RecordOperate(dataClass = DataType.NOTE, action = DataSynchAction.UPDATE, keyIndex = 0, keyMethod = "getId", timeStamp = "updateTime")
	public void updateNote(NoteEntity note, boolean updateContent) {
		if (!updateContent) {
			NoteEntity oldNote = getNote(note.getId());
			note.setContent(oldNote.getContent());
		}
		updateEntitie(note);
	}

	@Override
	@RecordOperate(dataClass = DataType.NOTE, action = DataSynchAction.DELETE, keyIndex = 0, keyMethod = "getId", timeStamp = "updateTime")
	public void markDelNote(NoteEntity note) {
		note.setDeleted(Constants.DATA_DELETED);
		updateEntitie(note);
	}

	@Override
	public void deleteNote(NoteEntity note) {
		attachmentService.deleteAttachment(note.getId());
		commentService.deleteComments(note.getId());
		delete(note);
		Group g = groupService.findGroup(NoteEntity.class.getName(), note.getId());
		if (g != null) {
			groupService.removeGUByGroupId(g.getGroupId());
			groupService.deleteGroup(g);
		}
	}

	@Override
	public void deleteNote(Serializable id) {
		deleteNote(getNote(id));
	}

	public void deleteNoteByDir(Serializable dirId){
		Object[] param = new Object[]{dirId};
		super.executeHql("delete from AttachmentEntity  where noteId in (select id from NoteEntity n where n.dirId = ? ) ", param);
		super.executeHql("delete from CommentEntity c  where noteId in (select id from NoteEntity n where n.dirId = ? ) ", param);
		super.executeHql("delete from GroupUser where GroupId in (select g.id from Group g ,NoteEntity n  where  g.classPk = n.id and  n.dirId = ?) ",param);
		super.executeHql("delete from Group g where classPk in(select id from NoteEntity n where n.dirId = ? )",param);
		super.executeHql("delete from NoteEntity where dirId = ? ",param);
	}

	
	@Override
	public NoteEntity getNote(Serializable id) {
		return get(NoteEntity.class, id);
	}

	@Override
	public List<NoteEntity> findNotesByDir(String dirId) {
		DetachedCriteria dc = DetachedCriteria.forClass(NoteEntity.class);
		dc.add(Restrictions.eq("deleted", Constants.DATA_NOT_DELETED));
		dc.add(Restrictions.eq("dirId", dirId));
		List<NoteEntity> list = findByDetached(dc);
		return list;
	}

	@Override
	public List<NoteEntity> findNotesBySubject(String subjectId) {
		DetachedCriteria dc = DetachedCriteria.forClass(NoteEntity.class);
		dc.add(Restrictions.eq("deleted", Constants.DATA_NOT_DELETED));
		dc.add(Restrictions.eq("subjectId", subjectId));
		List<NoteEntity> list = findByDetached(dc);
		return list;
	}

	@Override
	public boolean noteIsRead(String noteId, String userId) {
		DetachedCriteria dc = DetachedCriteria.forClass(NoteUserEntity.class);
		dc.add(Restrictions.eq("noteId", noteId));
		dc.add(Restrictions.eq("userId", userId));
		List<NoteEntity> list = findByDetached(dc);
		if (CollectionUtil.isValidateCollection(list)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean noteRead(String noteId, String userId) {
		NoteUserEntity noteUser = new NoteUserEntity();
		noteUser.setNoteId(noteId);
		noteUser.setUserId(userId);
		save(noteUser);
		return false;
	}

	@Override
	public long countNoReadNoteBySubject(String subjectId, String userId) {
		String sql = "SELECT count(*) FROM eht_note WHERE subjectId=? and id in(SELECT noteid from eht_note_user WHERE userid=?)";
		long count = getCountForJdbcParam(sql, new String[] {subjectId, userId});
		return count;
	}

	@Override
	public long countNoReadNoteByDir(String dirId, String userName) {
		String sql = "SELECT count(*) FROM eht_note WHERE dirId=? and id in(SELECT noteid from eht_note_user WHERE userid=?)";
		long count = getCountForJdbcParam(sql, new String[] {dirId, userName});
		return count;
	}

	@Override
	public List<NoteEntity> findNotesByParams(String userId,String subjectId, String dirId, String title, String tagId, String orderField) {
		DetachedCriteria dc = DetachedCriteria.forClass(NoteEntity.class, "note");
		dc.add(Restrictions.eq("deleted", Constants.DATA_NOT_DELETED));
		dc.add(Restrictions.eq("note.createUser", userId));
		if (!StringUtil.isEmpty(title)) {
			dc.add(Restrictions.or( Restrictions.like("note.title", title, MatchMode.ANYWHERE),Restrictions.like("note.content", title, MatchMode.ANYWHERE)));
		}
		
		if (!StringUtil.isEmpty(subjectId)) {
			dc.add(Restrictions.eq("note.subjectId", subjectId));
		}
		if (!StringUtil.isEmpty(dirId)) {
			String[] ids = dirId.split(",");
			if (ids[0].equals("*")) { // 专题下的条目（不属于某个目录下）
				if (ids.length > 2) {
					dc.add(Restrictions.or(Property.forName("note.dirId").isNull(), Property.forName("note.dirId").in(ids)));
				} else if (ids.length == 2) {
					dc.add(Restrictions.or(Property.forName("note.dirId").isNull(), Property.forName("note.dirId").eq(ids[1])));
				}
			} else {
				if (ids.length > 1) {
					dc.add(Restrictions.in("dirId", ids));
				} else if (ids.length == 1) {
					dc.add(Restrictions.eq("dirId", ids[0]));
				}
			}
		}

		if (!StringUtil.isEmpty(tagId)) {
			String[] ids = tagId.split(",");
			String hql = "select noteId from NoteTag where tagId in(";
			for(String id : ids){
				hql += "'" + id + "',";
			}
			hql = hql.substring(0,hql.length() - 1);
			hql += ")";
			List<String> list = findByQueryString(hql);
			if(!list.isEmpty()){
				dc.add(Restrictions.in("id", list));
			}else{
				return new ArrayList<NoteEntity>();
			}
			/*if (ids.length > 1) {
				dc.add(Restrictions.in("tagId", ids));
			} else if (ids.length == 1) {
				dc.add(Restrictions.eq("tagId", ids[0]));
			}*/
		}

		dc.addOrder(Order.desc(orderField));
		List<NoteEntity> list = findByDetached(dc);
		return list;
	}

	@Override
	public List<NoteEntity> findNotesInRecycleByParams(String userId, String subjectId, String dirId, String title, String tagId, String orderField, int subjectType) {
		DetachedCriteria dc = DetachedCriteria.forClass(NoteEntity.class, "note");
		if (!StringUtil.isEmpty(title)) {
			dc.add(Restrictions.like("note.title", title, MatchMode.ANYWHERE));
		}
		
		//点击回收站节点，查询专题下所有已删除条目
		if (!StringUtil.isEmpty(subjectId)) {
			Criterion criterionSub = null;
			String[] subjectIds = subjectId.split(",");
			if (subjectIds.length > 1) {
				criterionSub = Restrictions.and(Restrictions.in("note.subjectId", subjectIds), Restrictions.eq("note.deleted", Constants.DATA_DELETED));
			} else {
				criterionSub = Restrictions.and(Restrictions.eq("note.subjectId", subjectIds[0]), Restrictions.eq("note.deleted", Constants.DATA_DELETED));
			}
			Criterion criterionDir = null;
			if (!StringUtil.isEmpty(dirId)) {
				String[] ids = dirId.split(",");
				if (ids.length > 1) {
					criterionDir = Property.forName("note.dirId").in(ids);
				} else if (ids.length == 1) {
					criterionDir = Property.forName("note.dirId").eq(ids[0]);
				}
			}
			if(criterionDir != null){
				dc.add(Restrictions.or(criterionSub, criterionDir));
			}else{
				dc.add(criterionSub);
			}
		} else { // 专题ID为空，查询点击目录下所有已删除条目
			if (!StringUtil.isEmpty(dirId)) {
				String[] ids = dirId.split(",");
				Criterion criterionDir = null;
				if(ids[0].equals("recycle_personal")){  // 点击个人回收站节点
					criterionDir =  Restrictions.and(Restrictions.eq("note.createUser", userId), Restrictions.eq("note.deleted", Constants.DATA_DELETED));
					dc.createCriteria("subjectEntity", "s", JoinType.INNER_JOIN);
					dc.add(Restrictions.eq("s.subjectType", 1));
				}else{
					criterionDir = Restrictions.and(Restrictions.in("note.dirId", ids), Restrictions.eq("note.deleted", Constants.DATA_DELETED));
				}
				dc.add(criterionDir);
			}
		}

		if (!StringUtil.isEmpty(tagId)) {
			String[] ids = tagId.split(",");
			if (ids.length > 1) {
				dc.add(Restrictions.in("note.tagId", ids));
			} else {
				dc.add(Restrictions.eq("note.tagId", ids[0]));
			}
		}

		dc.addOrder(Order.desc(orderField));
		List<NoteEntity> list = findByDetached(dc);
		
		if(subjectType == Constants.SUBJECT_TYPE_M){
			List<NoteEntity> resultList = new ArrayList<NoteEntity>();
			for(NoteEntity note : list){
				if(!inNoteBlackList(userId, note.getId())){
					resultList.add(note);
				}
			}
			return resultList;
		}
		return list;
	}

	@Override
	public boolean inNoteBlackList(String userId, String noteId) {
		Group group = groupService.findGroup(NoteEntity.class.getName(), noteId);
		GroupUser gu = groupService.findGroupUser(group.getGroupId(), userId);
		return gu != null;
	}

	@Override
	@RecordOperate(dataClass = {DataType.NOTE, DataType.NOTEBLACK}, action = {DataSynchAction.DELETE, DataSynchAction.ADD}, keyIndex = {1, 1}, targetUser = {0, 0}, timeStamp = {"", ""}, keyMethod = {"", ""})
	public void blacklistedUser(String userId, String noteId) {
		Group group = groupService.findGroup(NoteEntity.class.getName(), noteId);
		// 将用户放入group-user关系表
		groupService.addGroupUser(group.getGroupId(), userId);
	}

	@Override
	@RecordOperate(dataClass = {DataType.NOTE, DataType.NOTEBLACK}, action = {DataSynchAction.ADD, DataSynchAction.DELETE}, keyIndex = {1, 1}, targetUser = {0, 0}, timeStamp = {"", ""}, keyMethod = {"", ""})
	public void removeUser4blacklist(String userId, String noteId) {
		Group group = groupService.findGroup(NoteEntity.class.getName(), noteId);
		groupService.removeGroupUser(group.getGroupId(), userId);
	}

	@Override
	public void getHistoryNote(String nodeId, PageResult pageResult) {
		if (pageResult == null) {
			pageResult = new PageResult();
		}
		pageResult.setPageSize(10);
		DetachedCriteria count = DetachedCriteria.forClass(NoteVersionEntity.class);
		count.add(Restrictions.eq("noteid", nodeId));
		Long total = (Long) count.getExecutableCriteria(getSession()).setProjection(Projections.rowCount()).uniqueResult();
		pageResult.setTotal(total);
		DetachedCriteria dc = DetachedCriteria.forClass(NoteVersionEntity.class);
		dc.add(Restrictions.eq("noteid", nodeId));
		dc.addOrder(Order.desc("version"));
		int firstRow = (pageResult.getPageNo() - 1) * pageResult.getPageSize();
		pageResult.setRows(pageList(dc, firstRow, pageResult.getPageSize()));
	}

	@Override
	public String shapeNote(String noteId, String userid) throws IOException {
		NoteVersionEntity noteVersionEntity = getEntity(NoteVersionEntity.class, noteId);
		// 历史版本的ZIP包
		String versionZipName = FilePathUtil.getNoteZipFileName(noteId, noteVersionEntity.getVersion());
		File versionZipFile = new File(versionZipName);
		
		NoteEntity noteEntity = getNote(noteVersionEntity.getNoteid());
		// 当前条目保存为历史版本
		NoteVersionEntity newVersion = saveNoteHistory(noteEntity, userid);
		
		// 从历史版本中恢复
		noteEntity.setContent(noteVersionEntity.getContent());
		noteEntity.setUpdateTime(new Date());
		noteEntity.setUpdateUser(userid);
		updateNote(noteEntity, true);
		
		// 恢复条目HTML文件
		String htmlPath = FilePathUtil.getNoteHtmlPath(noteEntity);
		String zipFilePath = htmlPath + FilePathUtil.getNoteZipFileName(noteEntity.getId(), newVersion.getVersion());
		File zipFile = new File(zipFilePath);
		String htmlName = htmlPath + noteEntity.getId() + ".html";
		File htmlFile = new File(htmlName);
		if(!htmlFile.exists()){
			saveNoteHtml(noteEntity);
		}
		// 将现有条目打包为历史版本
		DataSynchizeUtil.zipNoteHtml(zipFile, htmlFile);
		
		DataSynchizeUtil.unZipNoteHtml(versionZipFile, htmlPath);
		return noteVersionEntity.getContent();
	}
	
	public void saveNoteHistory(NoteVersionEntity noteHistory){
		save(noteHistory);
	}
	
	public NoteVersionEntity saveNoteHistory(NoteEntity note, String createUserId){
		NoteVersionEntity noteHistory = new NoteVersionEntity();
		noteHistory.setContent(note.getContent());
		noteHistory.setCreatetime(new Date());
		noteHistory.setCreateuser(createUserId);
		noteHistory.setNoteid(note.getId());
		Object obj = super.singleResult("select CASE when max(version) is null THEN 0 else max(version) end from NoteVersionEntity where noteid='"+ note.getId() +"'");
		noteHistory.setVersion(Integer.parseInt(obj.toString()) + 1);
		saveNoteHistory(noteHistory);
		return noteHistory;
	}
	
	@Override
	public List<NoteEntity> findDeletedNotes(String userId, int subjectType) {
		DetachedCriteria dc = DetachedCriteria.forClass(NoteEntity.class, "note");
		dc.createCriteria("subjectEntity", "s", JoinType.INNER_JOIN);

		dc.add(Restrictions.eq("s.subjectType", subjectType));
		dc.add(Restrictions.eq("note.createUser", userId));
		dc.add(Restrictions.eq("note.deleted", Constants.DATA_DELETED));
		List<NoteEntity> list = findByDetached(dc);
		return list;
	}

	@Override
	public List<NoteEntity> findDeletedNotesBySubjectId(String userId, String subjectId) {
		DetachedCriteria dc = DetachedCriteria.forClass(NoteEntity.class, "note");
		dc.add(Restrictions.eq("note.subjectId", subjectId));
		dc.add(Restrictions.eq("note.createUser", userId));
		dc.add(Restrictions.eq("note.deleted", Constants.DATA_DELETED));
		List<NoteEntity> list = findByDetached(dc);
		return list;
	}

	@Override
	public List<NoteEntity> findDeletedNotesByDirId(String userId, String dirId) {
		DetachedCriteria dc = DetachedCriteria.forClass(NoteEntity.class, "note");
		dc.add(Restrictions.eq("note.dirId", dirId));
		dc.add(Restrictions.eq("note.createUser", userId));
		dc.add(Restrictions.eq("note.deleted", Constants.DATA_DELETED));
		List<NoteEntity> list = findByDetached(dc);
		return list;
	}

	@Override
	public DirectoryEntity restoreNote(NoteEntity note,List <String>list) {
		if (note.getDeleted() == Constants.DATA_DELETED) {
			markNoteUnDeleted(note);
		}
		String dirId = note.getDirId();
		if (!StringUtil.isEmpty(dirId)) {
			DirectoryEntity dir = directoryService.getDirectory(dirId);
			dir.setUpdateTime(note.getUpdateTime());
			dir.setUpdateUser(note.getUpdateUser());
			return	directoryService.restoreDirectory(dir, list,false);
		}
		return null;
	}

	@RecordOperate(dataClass = DataType.NOTE, action = DataSynchAction.ADD, keyIndex = 0, keyMethod = "getId", timeStamp = "updateTime")
	public void markNoteUnDeleted(NoteEntity note) {
		note.setDeleted(Constants.DATA_NOT_DELETED);
		updateEntitie(note);
	}

	@Override
	public String getNoteResidential(DirectoryEntity directoryEntity, SubjectEntity subject) {
		if(directoryEntity==null){
			String type="";
			if(subject.getSubjectType()==1){
				type="个人专题";
			}else{
				type="多人专题";
			}
			return type+"/"+subject.getSubjectName()+"/";
		}else{
			String pid=directoryEntity.getParentId();
			if(pid == null || pid.equals("")){
				return getNoteResidential(null,subject)+directoryEntity.getDirName()+"/";
			}else{
				DirectoryEntity	directory =directoryService.getDirectory(pid);
				return getNoteResidential(directory,subject)+directoryEntity.getDirName()+"/";
			}
		}
	}

	@Override
	public long countNotesBySubjectUser(String subjectId, String userId) {
		String sql = "SELECT count(*) FROM eht_note WHERE subjectId=? and createUser=?";
		long count = getCountForJdbcParam(sql, new String[] {subjectId, userId});
		return count;
	}
	
	@Override
	public List<NoteEntity> findNotesBySubject(String subjectId, String userId) {
		String query="select d.* from eht_note d where  d.subjectId='"+subjectId+"' and d.deleted="+Constants.DATA_NOT_DELETED+"  and d.id not in(select p.classpk from eht_group p , eht_group_user u where u.userid='"+userId +"' and  u.groupid=p.groupid )";
		return  this.commonDao.findListbySql(query, NoteEntity.class);
	}

	@Override
	public List<NoteEntity> findMNotesByParams(String subjectId, String dirId,String title, String tagId, String orderField, String userId,String userIdl) {
		DetachedCriteria dc = DetachedCriteria.forClass(NoteEntity.class, "note");
		dc.add(Restrictions.eq("deleted", Constants.DATA_NOT_DELETED));
		if (!StringUtil.isEmpty(title)) {
			dc.add(Restrictions.or( Restrictions.like("note.title", title, MatchMode.ANYWHERE),Restrictions.like("note.content", title, MatchMode.ANYWHERE)));
		}
		if (!StringUtil.isEmpty(userIdl))
		{
			dc.add(Restrictions.eq("note.createUser",userIdl));	
		}
		dc.add(Restrictions.eq("note.subjectId", subjectId));
		
		if (!StringUtil.isEmpty(dirId)) {
			String[] ids = dirId.split(",");
			if (ids.length > 1) {
				dc.add(Restrictions.in("dirId", ids));
			} else if (ids.length == 1) {
				dc.add(Restrictions.eq("dirId", ids[0]));
			}
		}

		if (!StringUtil.isEmpty(tagId)) {
			String[] ids = tagId.split(",");
			if(ids.length>0){
				if(ids[0].startsWith("tag_subject_")){
					if(ids.length>1){
						String[] newIds = new String[ids.length-1];
						System.arraycopy(ids, 1, newIds, 0, ids.length-1);
					}else{
						ids = new String[]{};
					}
				}
			}
			if(ids.length > 0){
				String hql = "select noteId from NoteTag where tagId in(";
				for(String id : ids){
					hql += "'" + id + "',";
				}
				hql = hql.substring(0,hql.length() - 1);
				hql += ")";
				List<String> list = findByQueryString(hql);
				if(!list.isEmpty()){
					dc.add(Restrictions.in("id", list));
				}else{
					return new ArrayList<NoteEntity>();
				}
				/*if (ids.length > 1) {
					dc.createCriteria("NoteTag", "t", JoinType.INNER_JOIN);
					dc.add(Restrictions.eq("note.id", "t.noteId"));
					dc.add(Restrictions.in("t.tagId", ids));
				} else if (ids.length == 1) {
					dc.createCriteria("NoteTag", "t", JoinType.INNER_JOIN);
					dc.add(Restrictions.eq("note.id", "t.noteId"));
					dc.add(Restrictions.eq("t.tagId", ids[0]));
				}*/
			}
		}

		dc.addOrder(Order.desc(orderField));
		List<NoteEntity> list = findByDetached(dc);
		
		//去除黑名单中的条目
		List<NoteEntity> resultList = new ArrayList<NoteEntity>();
		for(NoteEntity note : list){
			if(!inNoteBlackList(userId, note.getId())){
				resultList.add(note);
			}
		}
		return resultList;
	}

	@Override
	public void saveNoteHtml(NoteEntity note) throws IOException {
		String savePath = FilePathUtil.getNoteHtmlPath(note);
		File savefile = new File(savePath);
		// 判断是否已存在
		if (!savefile.exists()) {
			savefile.mkdirs();
		}
		savefile = new File(savePath + note.getId() + ".html");
		String pcontet=HtmlParser.repleceHtmlImg(note.getContent(), "../../notes/"+note.getSubjectId()+"/"+note.getId()+"/", "");
		FileCopyUtils.copy(pcontet.getBytes("UTF-8"), savefile);
	}
	
	@Override
	public String generateMD5Html(NoteEntity note, boolean update){
		NoteThread thread = new NoteThread(note, update);
		thread.start();
		return null;
	}
	
	class NoteThread extends Thread{
		
		private Logger logger = Logger.getLogger(NoteThread.class);
		
		private NoteEntity note;
		
		/**
		 * 是否为更新操作
		 */
		private boolean update;
		
		NoteThread(NoteEntity note){
			this.note = note;
			this.update = true;
		}
		
		NoteThread(NoteEntity note, boolean update){
			this.note = note;
			this.update = update;
		}
		
		@Override
		public void run() {
			String md5 = MD5FileUtil.getMD5String(note.getContent() == null ? "" : note.getContent());
			if(!update){
				note.setMd5(md5);
				updateEntitie(note);
				try {
					saveNoteHtml(note);
				} catch (IOException e) {
					logger.error("保存条目HTML文件异常！", e);
				}
			}else{
				NoteEntity oldNote = getNote(note.getId());
				String oldMD5 = oldNote.getMd5();
				if(StringUtil.isEmpty(oldMD5) || !oldMD5.equals(md5)){
					oldNote.setMd5(md5);
					updateEntitie(note);
					try {
						saveNoteHtml(note);
					} catch (IOException e) {
						logger.error("保存条目HTML文件异常！", e);
					}
				}
			}
		}
	}
}