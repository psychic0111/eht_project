package com.eht.system.service;

import java.util.List;

import com.eht.subject.entity.SubjectEntity;
import com.eht.system.bean.TreeData;


public interface TreeMenuService {
	
	/**
	 * 消息中心菜单
	 * @param userId
	 * @return
	 */
	public List<TreeData> buildMessageCenter(String userId);
	/**
	 * 个人专题菜单
	 * @param account
	 * @return
	 */
	public List<TreeData> buildPersonalSubject(String userId);
	/**
	 * 多人专题部分
	 * @param account
	 * @return
	 */
	public List<TreeData> buildSharedSubject(String userId);
	
	/**
	 * 加载某个专题节点
	 * @param subjectId
	 * @return
	 */
	public List<TreeData> loadSubjectNode(String userId, SubjectEntity subject, boolean order);
	
	/**
	 * 加载回收站
	 * @param userId
	 * @param subjectType
	 * @param recycleBranchId
	 * @param recycleNodeId
	 * @return
	 */
	public List<TreeData> loadRecycleNode(String userId, int subjectType, String recycleBranchId, String recycleNodeId);
}
