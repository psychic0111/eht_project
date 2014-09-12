package com.eht.note.service;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.jeecgframework.core.common.service.CommonService;
import com.eht.common.page.PageResult;
import com.eht.note.entity.NoteEntity;
import com.eht.note.entity.NoteVersionEntity;
import com.eht.subject.entity.DirectoryEntity;
import com.eht.subject.entity.SubjectEntity;
import com.eht.user.entity.AccountEntity;


public interface NoteServiceI extends CommonService{
	
	/**
	 * 
	 * @param ids
	 * @return
	 */
	
	public List<NoteEntity> findNotesByIds(String[] ids); 
	/**
	 * 查找专题被共享用户(分页)
	 */
	public List<AccountEntity> getShareEmailbyPage(String searchField,int start,int end); 
	
	/**
	 * 查找专题被共享用户
	 */
	public List<AccountEntity> getShareEmail();
	
	/**
	 * 查找专题被共享用户
	 */
	public List<AccountEntity> getShareEmail(String searchField);
	
	/**
	 * 添加条目
	 * @param note
	 * @return 条目groupId
	 */
	public Serializable addNote(NoteEntity note);
	/**
	 * 更新条目
	 * @param note
	 */
	public void updateNote(NoteEntity note, boolean updateContent);
	
	/**
	 * 删除条目
	 * @param note
	 */
	public void deleteNote(NoteEntity note);
	
	/**
	 * 删除条目
	 * @param id
	 */
	public void deleteNote(Serializable id);
	
	/**
	 * 删除某目录下的条目
	 * @param dirId
	 */
	public void deleteNoteByDir(Serializable dirId);
	
	/**
	 * 查询条目
	 * @param id
	 * @return
	 */
	public NoteEntity getNote(Serializable id);
	
	/**
	 * 查询目录下条目，不包括子目录下
	 * @param dirId
	 * @return
	 */
	public List<NoteEntity> findNotesByDir(String dirId);
	
	/**
	 * 查询专题下条目
	 * @param subjectId
	 * @return
	 */
	public List<NoteEntity> findNotesBySubject(String subjectId);
	
	/**
	 * 查询专题下条目
	 * @param subjectId
	 * @return
	 */
	public List<NoteEntity> findNotesBySubject(String subjectId,String userId);
	
	/**
	 * 查询回收站下条目，标题包含指定字符串
	 * @param subjectId
	 * @param dirId
	 * @param title
	 * @param tagId
	 * @param deleted
	 * @param orderField
	 * @return
	 */
	public List<NoteEntity> findNotesInRecycleByParams(String userId, String subjectId, String dirId, String title, String tagId, String orderField, int subjectType);
	
	/**
	 * 查询专题目录下条目，标题包含指定字符串
	 * @param subjectId
	 * @param dirId
	 * @param title
	 * @param tagId
	 * @param orderField
	 * @return
	 */
	public List<NoteEntity> findNotesByParams(String userId, String subjectId, String dirId, String title, String tagId, String orderField);
	
	/**
	 * 查询条目是否已读
	 * @param noteId
	 * @param userId
	 * @return
	 */
	public boolean noteIsRead(String noteId, String userId);
	
	/**
	 * 条目置为已读
	 * @param noteId
	 * @param userId
	 * @return
	 */
	public boolean noteRead(String noteId, String userId);
	
	/**
	 * 查询专题下未读条目数量
	 * @param subjectId
	 * @param userId
	 * @return
	 */
	public long countNoReadNoteBySubject(String subjectId, String userName);
	
	/**
	 * 查询目录下未读条目数量
	 * @param dirId
	 * @param userId
	 * @return
	 */
	public long countNoReadNoteByDir(String dirId, String userName);
	
	/**
	 * 将用户加入黑名单，用户将不能访问该条目
	 * @param userId
	 * @param noteId
	 * @return
	 */
	public void blacklistedUser(String userId, String noteId);
	
	/**
	 * 将用户从黑名单移除
	 * @param userId
	 * @param noteId
	 * @return
	 */
	public void removeUser4blacklist(String userId, String noteId);
	
	/**
	 * 是否在条目黑名单
	 * @param groupId
	 * @param userId
	 * @return
	 */
	public boolean inNoteBlackList(String userId, String noteId);
	
	/**
	 * 查询条目的历史版本（分页）
	 * @param nodeId
	 * @param pageResult
	 * @return
	 */
	public void getHistoryNote(String nodeId,PageResult pageResult);

	/**
	 * 标记删除条目
	 * @param note
	 */
	public void markDelNote(NoteEntity note);
	
	/**
	 * 还原历史版本
	 * @param nodeId
	 * @param userid
	 * @return
	 * @throws IOException 
	 */
	public String shapeNote(String nodeId,String  userid) throws IOException;
	
	/**
	 * 保存条目历史版本
	 * @param noteHistory
	 * @return
	 */
	public void saveNoteHistory(NoteVersionEntity noteHistory);
	
	/**
	 * 保存条目历史版本
	 * @param note
	 * @return 历史版本对象
	 */
	public NoteVersionEntity saveNoteHistory(NoteEntity note, String createUserId);
	
	/**
	 * 查询标记删除的条目
	 * @param userId
	 * @param subjectType
	 * @return
	 */
	public List<NoteEntity> findDeletedNotes(String userId, int subjectType);
	/**
	 * 查询标记删除的条目
	 * @param userId
	 * @param subjectId
	 * @return
	 */
	public List<NoteEntity> findDeletedNotesBySubjectId(String userId, String subjectId);
	
	/**
	 * 查询标记删除的条目
	 * @param userId
	 * @param subjectId
	 * @return
	 */
	public List<NoteEntity> findDeletedNotesByDirId(String userId, String dirId);
	
	/**
	 * 还原删除的条目
	 * @param note
	 */
	public DirectoryEntity restoreNote(NoteEntity note,List <String>list);
	
	/**
	 * 取得条目位置
	 * @param noteId
	 */
	public String getNoteResidential(DirectoryEntity directoryEntity,SubjectEntity subject);
	
	/**
	 * 查找符合条件的多人专题条目
	 * @param subjectId
	 * @param dirId
	 * @param searchInput
	 * @param tagId
	 * @param orderField
	 * @return
	 */
	public List<NoteEntity> findMNotesByParams(String subjectId, String dirId,String searchInput, String tagId, String orderField, String userId,String userIdl);
	
	
	/**
	 * 保存条目的html
	 * @return
	 */
	public void saveNoteHtml(NoteEntity note) throws IOException;
	
	/**
	 * 生成条目内容MD5
	 * @param note
	 * @return
	 */
	public String generateMD5Html(NoteEntity note, boolean update);
	
	/**
	 * 查询用户在专题中创建的条目数量
	 * @param subjectId
	 * @param userId
	 * @return
	 */
	public long countNotesBySubjectUser(String subjectId, String userId);
}
