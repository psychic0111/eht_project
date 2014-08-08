package com.eht.tag.service;

import java.util.List;

import org.jeecgframework.core.common.service.CommonService;

import com.eht.system.bean.TreeData;
import com.eht.tag.entity.TagEntity;

public interface TagServiceI extends CommonService{
	/**
	 * 标签树
	 * @param tagid
	 * @return
	 */
	public List<TreeData> buildTagTreeJson(String tagid,List<TagEntity>  tagList); 
	/**
	 * 条目选择标签
	 * @param tag
	 */
	public List<TagEntity> findTagByisGroup(int bool,String userid,String subjectId);
	/**
	 * 查询标签
	 * @param tag
	 */
	public TagEntity getTag(String tagId);
	/**
	 * 添加标签
	 * @param tag
	 */
	public String addTag(TagEntity tag);
	/**
	 * 修改标签
	 * @param tag
	 */
	public void updateTag(TagEntity tag);
	/**
	 * 删除标签
	 * @param tag
	 */
	public void deleteTag(TagEntity tag);
	/**
	 * 删除标签
	 * @param id
	 */
	public void deleteTagById(String id);
	/**
	 * 查询专题标签
	 * @return
	 */
	public List<TagEntity> findTagBySubject(String subjectId);
	/**
	 * 查询用户个人标签
	 * @return
	 */
	public List<TagEntity> findUserTags(String account);
	
	/**
	 * 查询标签下有多少条目
	 * @return
	 */
	public  String findCoutNoteforTags(String account);
}