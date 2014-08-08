package com.eht.comment.service;

import java.util.List;

import org.jeecgframework.core.common.service.CommonService;

import com.eht.comment.entity.CommentEntity;

public interface CommentServiceI extends CommonService{
	
	/**
	 * 查询评论
	 * @param commentId
	 * @return
	 */
	public CommentEntity getComment(String commentId);
	
	/**
	 * 根据条目ID查询评论
	 * @param noteId
	 * @return
	 */
	public List<CommentEntity> findCommentByNote(String noteId);
	/**
	 * 添加评论
	 * @param comment
	 * @return
	 */
	public String addComment(CommentEntity comment);
	/**
	 * 更新评论
	 * @param comment
	 */
	public void updateComment(CommentEntity comment);
	/**
	 * 删除评论
	 * @param comment
	 * @return
	 */
	public boolean deleteComment(CommentEntity comment);
	/**
	 * 删除评论
	 * @param commentId
	 * @return
	 */
	public boolean deleteComment(String commentId);
	/**
	 * 删除某个条目的评论
	 * @param commentId
	 * @return
	 */
	public boolean deleteComments(String noteId);
}
