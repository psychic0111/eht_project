package com.eht.tag.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.jeecgframework.core.common.service.impl.CommonServiceImpl;
import org.jeecgframework.core.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eht.common.annotation.RecordOperate;
import com.eht.common.constant.Constants;
import com.eht.common.enumeration.DataSynchAction;
import com.eht.common.enumeration.DataType;
import com.eht.common.util.AppContextUtils;
import com.eht.common.util.TreeUtils;
import com.eht.common.util.UUIDGenerator;
import com.eht.group.entity.Group;
import com.eht.group.service.GroupService;
import com.eht.note.entity.NoteTag;
import com.eht.note.service.NoteServiceI;
import com.eht.resource.entity.ClassName;
import com.eht.resource.service.ResourceActionService;
import com.eht.subject.entity.SubjectEntity;
import com.eht.system.bean.TreeData;
import com.eht.tag.entity.TagEntity;
import com.eht.tag.service.TagServiceI;

@Service("tagService")
@Transactional
public class TagServiceImpl extends CommonServiceImpl implements TagServiceI {

	@Autowired
	private ResourceActionService resourceActionService;
	
	@Autowired
	private NoteServiceI noteService;
	
	@Autowired
	private GroupService groupService;
	
	@Override
	@RecordOperate(dataClass=DataType.TAG, action=DataSynchAction.ADD, keyIndex=0, keyMethod="getId", timeStamp="createTime")
	public String addTag(TagEntity tag) {
		if(StringUtil.isEmptyOrBlank(tag.getParentId())){
			tag.setParentId(null);
		}
		save(tag);
		
		ClassName c = resourceActionService.findResourceByName(TagEntity.class.getName());
		if(c == null){
			c = new ClassName();
			c.setClassName(TagEntity.class.getName());
			resourceActionService.addResource(c);
		}
		long parentGroupId = 0L;
		if(!StringUtil.isEmpty(tag.getSubjectId())){
			Group group = groupService.findGroup(SubjectEntity.class.getName(), tag.getSubjectId());
			parentGroupId = group.getGroupId();
		}
		groupService.addGroup(c.getClassNameId(), tag.getId(), tag.getName(), tag.getId(), parentGroupId);
		return tag.getId();
	}

	@Override
	@RecordOperate(dataClass=DataType.TAG, action=DataSynchAction.UPDATE, keyIndex=0, keyMethod="getId", timeStamp="updateTime")
	public void updateTag(TagEntity tag) {
		updateEntitie(tag);
	}

	@Override
	@RecordOperate(dataClass=DataType.TAG, action=DataSynchAction.DELETE, keyIndex=0, keyMethod="getId")
	public void deleteTag(TagEntity tag) {
		deleteNoteTagByTagId(tag.getId());
		delete(tag);
	}
	
	/**
	 * 删除标签及子标签
	 * @param parentTag
	 */
	@Override
	public void deleteTagAll(TagEntity parentTag) {
		List<TagEntity> tagList = new ArrayList<TagEntity>();
		findChildTagsByParentId(parentTag.getId(), tagList);
		if(tagList != null && !tagList.isEmpty()){
			for(TagEntity tag : tagList){
				deleteTag(tag);
			}
		}
		deleteTag(parentTag);
	}
	
	@Override
	public void deleteTagById(String id) {
		TagEntity tag = getTag(id);
		deleteTagAll(tag);
	}

	@Override
	public List<TagEntity> findTagBySubject(String subjectId) {
		DetachedCriteria dc = DetachedCriteria.forClass(TagEntity.class);
		dc.add(Restrictions.eq("deleted", Constants.DATA_NOT_DELETED));
		dc.add(Restrictions.eq("subjectId", subjectId));
		List<TagEntity> list = findByDetached(dc);
		return list;
	}
	
	@Override
	public void findChildTagsByParentId(String parentId, List<TagEntity> tagList) {
		List<TagEntity> list = findByProperty(TagEntity.class, "parentId", parentId);
		if(list != null && !list.isEmpty()){
			tagList.addAll(list);
			for(TagEntity tag : tagList){
				findChildTagsByParentId(tag.getId(), tagList);
			}
		}
	}
	
	@Override
	public List<TreeData> buildTagTreeJson(String tagid,List<TagEntity>  tagList) {
		List<TreeData> dataList = new ArrayList<TreeData>(); 
		Map<String, String> tagMap = new HashMap<String, String>();
		tagMap.put("dataType", "TAG");
		tagMap.put("branchId", "tag_personal");
		tagMap.put("icon", AppContextUtils.getContextPath() + "/webpage/front/images/tree/tag_purple.png"); 
		// 转换为TreeData格式 标签
		if(tagList != null && !tagList.isEmpty()){
			dataList.addAll(TreeUtils.transformObjectList2TreeDataList(tagList, "id", "name", "parentId", tagMap));
		} 
		return dataList;
	}
	
	@Override
	public List<TreeData> buildTagTreeJson(String tagid, List<TagEntity> tagList, List<String> checkedList) {
		List<TreeData> dataList = new ArrayList<TreeData>(); 
		Map<String, String> tagMap = new HashMap<String, String>();
		tagMap.put("dataType", "TAG");
		tagMap.put("branchId", "tag_personal");
		tagMap.put("icon", AppContextUtils.getContextPath() + "/webpage/front/images/tree/tag_purple.png"); 
		tagMap.put("open", "true");
		// 转换为TreeData格式 标签
		if(tagList != null && !tagList.isEmpty()){
			dataList.addAll(TreeUtils.transformObjectList2TreeDataList(tagList, "id", "name", "parentId", tagMap, checkedList));
		} 
		return dataList;
	}
	
	@Override
	public List<TagEntity> findTagByisGroup(int subjecttype,String userid,String subjectId) {
		DetachedCriteria dc = DetachedCriteria.forClass(TagEntity.class);
		dc.add(Restrictions.eq("deleted", Constants.DATA_NOT_DELETED));
		if(subjecttype==Constants.SUBJECT_TYPE_M){
			//dc.add(Restrictions.or(Restrictions.isNotNull("subjectId"), Restrictions.isNotEmpty("subjectId")));     
			dc.add(Restrictions.eq("subjectId",subjectId));
		}else{
			dc.add(Restrictions.or(Restrictions.isNull("subjectId"), Restrictions.eq("subjectId", "")));   
			dc.add(Restrictions.eq("createUser", userid));     
		}
		dc.addOrder(Order.desc("updateTime"));
		/*if(parentid==null||parentid.equals("")){
			//dc.add(Restrictions.or(Restrictions.isNull("parentId"), Restrictions.eq("parentId", "")));     
		}else{
			dc.add(Restrictions.eq("parentId", parentid));
		}*/
		List<TagEntity> list = findByDetached(dc);
		return list;
	}
	@Override
	public List<TagEntity> findUserTags(String userId) {
		DetachedCriteria dc = DetachedCriteria.forClass(TagEntity.class);
		dc.add(Restrictions.eq("deleted", Constants.DATA_NOT_DELETED));
		dc.add(Restrictions.or(Restrictions.isNull("subjectId"), Restrictions.eq("subjectId", "")));       //用户个人标签与专题无关
		dc.add(Restrictions.eq("createUser", userId));
		List<TagEntity> list = findByDetached(dc);
		return list;
	}

	@Override
	public TagEntity getTag(String tagId) {
		return get(TagEntity.class, tagId);
	}

	@Override
	public long findCoutNoteforTags(String tagId) {
		String hql="select count(*) from  NoteTag n where  n.note.deleted="+Constants.DATA_NOT_DELETED+" and n.tagId=?  ";
		Long count=(Long) findHql(hql,new Object[]{tagId}).get(0);
//		DetachedCriteria dc = DetachedCriteria.forClass(TagEntity.class);
//		dc.add(Restrictions.eq("deleted", Constants.DATA_NOT_DELETED));
//		dc.add(Restrictions.eq("parentId", tagId));
//		List<TagEntity> list = findByDetached(dc);
//		for (TagEntity tagEntity : list) {
//			count+=findCoutNoteforTags(tagEntity.getId());
//		}
		return count;
	}

	@Override
	public long findCoutNoteforRemenber(String subejectId, String userId) {
		String hql="select count(*) from  NoteEntity n where  n.deleted="+Constants.DATA_NOT_DELETED+" and n.subjectId=? and n.createUser=? ";
		Long count=(Long) findHql(hql,new Object[]{subejectId,userId}).get(0);
		return count;
	}

	@Override
	public String saveNoteTag(String uuid, String noteId, String tagId) {
		NoteTag noteTag = new NoteTag();
		noteTag.setId(uuid);
		noteTag.setNoteId(noteId);
		noteTag.setTagId(tagId);
		saveNoteTag(noteTag);
		return noteTag.getId();
	}
	
	@Override
	@RecordOperate(dataClass=DataType.NOTETAG, action=DataSynchAction.ADD, keyIndex=0, keyMethod = "getId")
	public String saveNoteTag(NoteTag noteTag) {
		save(noteTag);
		return noteTag.getId();
	}
	
	@Override
	public String saveNoteTags(String noteId, String[] tagIds) {
		//原标签集合
		List<String> tagList = findTagIdsByNote(noteId);
		
		if(tagList != null && !tagList.isEmpty()){
			if(tagIds != null && tagIds.length > 0){
				//新标签集合
				List<String> newList = new ArrayList<String>(tagIds.length);
				for(String tagId : tagIds){
					newList.add(tagId);
				}
				
				//需删除的标签
				for(String tagId : tagList){
					if(!newList.contains(tagId)){
						deleteNoteTag(noteId, tagId);
					}
				}
				
				//需添加的标签
				for(String tagId : newList){
					if(!tagList.contains(tagId)){
						saveNoteTag(UUIDGenerator.uuid(), noteId, tagId);
					}
				}
			}else{
				for(String tagId : tagList){
					deleteNoteTag(noteId, tagId);
				}
			}
		}else{
			for(String tagId : tagIds){
				saveNoteTag(UUIDGenerator.uuid(), noteId, tagId);
			}
		}
		return null;
	}

	@Override
	public String deleteNoteTag(String noteId, String tagId) {
		NoteTag noteTag = findNoteTag(noteId, tagId);
		if(noteTag != null){
			deleteNoteTag(noteTag);
			return noteTag.getId();
		}
		return null;
	}

	@Override
	@RecordOperate(dataClass=DataType.NOTETAG, action=DataSynchAction.DELETE, keyIndex=0, keyMethod="getId")
	public String deleteNoteTag(NoteTag noteTag) {
		delete(noteTag);
		return noteTag.getId();
	}

	@Override
	public String deleteNoteTag(String id) {
		NoteTag noteTag =get(NoteTag.class, id);
		return deleteNoteTag(noteTag);
	}

	@Override
	public NoteTag findNoteTag(String noteId, String tagId) {
		DetachedCriteria dc = DetachedCriteria.forClass(NoteTag.class);
		dc.add(Restrictions.eq("noteId", noteId));
		dc.add(Restrictions.eq("tagId", tagId));
		List<NoteTag> list = findByDetached(dc);
		if(list != null && !list.isEmpty()){
			return list.get(0);
		}
		return null;
	}

	@Override
	public void deleteNoteTagByNoteId(String noteId) {
		DetachedCriteria dc = DetachedCriteria.forClass(NoteTag.class);
		dc.add(Restrictions.eq("noteId", noteId));
		List<NoteTag> list = findByDetached(dc);
		if(list != null && !list.isEmpty()){
			for(NoteTag noteTag : list){
				deleteNoteTag(noteTag);
			}
		}
	}

	@Override
	public void deleteNoteTagByTagId(String tagId) {
		DetachedCriteria dc = DetachedCriteria.forClass(NoteTag.class);
		dc.add(Restrictions.eq("tagId", tagId));
		List<NoteTag> list = findByDetached(dc);
		if(list != null && !list.isEmpty()){
			for(NoteTag noteTag : list){
				deleteNoteTag(noteTag);
			}
		}
	}
	
	@Override
	public List<String> findTagIdsByNote(String noteId){
		DetachedCriteria dc = DetachedCriteria.forClass(NoteTag.class);
		dc.add(Restrictions.eq("noteId", noteId));
		List<NoteTag> list = findByDetached(dc);
		List<String> tagList = new ArrayList<String>();
		for(NoteTag noteTag : list){
			tagList.add(noteTag.getTagId());
		}
		
		return tagList;
	}
	
	@Override
	public List<TagEntity> findTagByNote(String noteId){
		List<String> tagList = findTagIdsByNote(noteId);
		
		if(tagList != null && !tagList.isEmpty()){
			StringBuilder sb = new StringBuilder("from TagEntity where name=name and id in(");
			for(String tagId : tagList){
				sb.append("'").append(tagId).append("',");
			}
			sb.setLength(sb.length() - 1);
			sb.append(")");
			/*DetachedCriteria deta = DetachedCriteria.forClass(TagEntity.class);
			deta.add(Restrictions.in("id", tagList));
			return findByDetached(deta);*/
			return findByQueryString(sb.toString());
		}
		return null;
	}
}