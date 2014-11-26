package com.eht.tag.service;

import java.util.List;

import org.jeecgframework.core.common.service.CommonService;

import com.eht.note.entity.NoteTag;
import com.eht.system.bean.TreeData;
import com.eht.tag.entity.TagEntity;

public interface TagServiceI extends CommonService{
	/**
	 * 标签树
	 * @param tagid
	 * @return
	 */
	public List<TreeData> buildTagTreeJson(String tagid, List<TagEntity> tagList);
	
	/**
	 * 查询标签树
	 * @param tagid
	 * @param tagList
	 * @param checkedList 选中标签集合
	 * @return
	 */
	public List<TreeData> buildTagTreeJson(String tagid, List<TagEntity> tagList, List<String> checkedList);
	
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
	public long findCoutNoteforTags(String tagId);
	
	/**
	 * 保存条目标签关系
	 * @param noteId
	 * @param tagIds
	 * @return
	 */
	public String saveNoteTag(String uuid, String noteId, String tagId, String userId);
	
	/**
	 * 保存条目标签关系
	 * @param noteId
	 * @param tagIds
	 * @return
	 */
	public String saveNoteTags(String noteId, String[] tagIds, String userId);
	
	/**
	 * 删除条目标签关系
	 * @param id
	 * @return
	 */
	public String deleteNoteTag(String noteId, String tagId);
	
	/**
	 * 删除条目标签关系
	 * @param id
	 * @return
	 */
	public String deleteNoteTag(NoteTag noteTag);
	
	/**
	 * 删除条目标签关系
	 * @param id
	 * @return
	 */
	public String deleteNoteTag(String id);
	
	/**
	 * 删除条目标签关系
	 * @param id
	 * @return
	 */
	public void deleteNoteTagByNoteId(String noteId);
	
	/**
	 * 删除条目标签关系
	 * @param id
	 * @return
	 */
	public void deleteNoteTagByTagId(String tagId);
	
	/**
	 * 根据条目ID、标签ID查找关系
	 * @param noteId
	 * @param tagId
	 * @return
	 */
	public NoteTag findNoteTag(String noteId, String tagId);
	
	/**
	 * 根据条目所有标签
	 * @param noteId
	 * @param tagId
	 * @return
	 */
	public List<String> findTagIdsByNote(String noteId);

	public List<TagEntity> findTagByNote(String noteId);

	public String saveNoteTag(NoteTag noteTag);
	
	
	/**
	 * 查询标签下有多少条目
	 * @return
	 */
	public  long findCoutNoteforRemenber(String subejectId,String userId);

	/**
	 * 查询子标签
	 * @param parentId
	 * @param tagList  返回结果集合
	 */
	public void findChildTagsByParentId(String parentId, List<TagEntity> tagList);

	public void deleteTagAll(TagEntity parentTag);
	
	/**
	 * 查询条目标签关系
	 * @param noteId
	 * @return
	 */
	public List<NoteTag> findNoteTagsByNote(String noteId);

	public NoteTag getNoteTag(String id);
}
