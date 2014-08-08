package com.eht.tag.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eht.common.annotation.RecordOperate;
import com.eht.common.constant.Constants;
import com.eht.common.constant.SynchConstants;
import com.eht.common.enumeration.DataSynchAction;
import com.eht.common.enumeration.DataType;
import com.eht.common.util.AppContextUtils;
import com.eht.common.util.TreeUtils;
import com.eht.group.entity.Group;
import com.eht.group.service.GroupService;
import com.eht.resource.entity.ClassName;
import com.eht.resource.service.ResourceActionService;
import com.eht.subject.entity.DirectoryEntity;
import com.eht.subject.entity.SubjectEntity;
import com.eht.system.bean.TreeData;
import com.eht.tag.entity.TagEntity;
import com.eht.tag.service.TagServiceI;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.jeecgframework.core.common.service.impl.CommonServiceImpl;
import org.jeecgframework.core.util.StringUtil;

@Service("tagService")
@Transactional
public class TagServiceImpl extends CommonServiceImpl implements TagServiceI {

	@Autowired
	private ResourceActionService resourceActionService;
	
	@Autowired
	private GroupService groupService;
	
	@Override
	@RecordOperate(dataClass=DataType.TAG, action=DataSynchAction.ADD, keyIndex=0, keyMethod="getId", timeStamp="createTime")
	public String addTag(TagEntity tag) {
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
		delete(tag);
	}

	@Override
	public void deleteTagById(String id) {
		TagEntity tag = getTag(id);
		deleteTag(tag);
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
	public String findCoutNoteforTags(String account) {
		String hql="select count(*) from  NoteEntity n where   n.tagId=?";
		Long count=(Long) findHql(hql,new Object[]{account}).get(0);
		return count+"";
	}
	
	
}