package com.eht.system.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.eht.common.constant.Constants;
import com.eht.common.util.AppContextUtils;
import com.eht.common.util.TreeUtils;
import com.eht.message.service.MessageServiceI;
import com.eht.note.service.NoteServiceI;
import com.eht.role.entity.RoleUser;
import com.eht.role.service.RoleService;
import com.eht.subject.entity.DirectoryEntity;
import com.eht.subject.entity.SubjectEntity;
import com.eht.subject.service.DirectoryServiceI;
import com.eht.subject.service.SubjectServiceI;
import com.eht.system.bean.TreeData;
import com.eht.system.service.TreeMenuService;
import com.eht.tag.entity.TagEntity;
import com.eht.tag.service.TagServiceI;

@Service("treeMenuService")
@Transactional
public class TreeMenuServiceImpl implements TreeMenuService {
	
	@Autowired
	private MessageServiceI messageService;
	
	@Autowired
	private TagServiceI tagService;
	
	@Autowired
	private SubjectServiceI subjectService;
	
	@Autowired
	private DirectoryServiceI directoryService;
	
	@Autowired
	private RoleService roleService;
	
	
	@Autowired
	private NoteServiceI noteService;
	
	@Override
	public List<TreeData> buildMessageCenter(String userId) {
		List<TreeData> dataList = new ArrayList<TreeData>();
		
		long sysMsgCount = messageService.getNoReadMessageCount(userId, Constants.MSG_SYSTEM_TYPE);
		long userCount = messageService.getNoReadMessageCount(userId, Constants.MSG_USER_TYPE);
		long totalCount = sysMsgCount + userCount;
		
		TreeData rootMsg = new TreeData();
		rootMsg.setDataType("MSG");
		rootMsg.setId(Constants.MSG_NODEID_R);
		rootMsg.setName("消息中心");
		rootMsg.setIsParent("true");
		rootMsg.setOpen("true");
		rootMsg.setIconSkin("msg");
		
		TreeData userMsgNode = new TreeData();
		userMsgNode.setDataType("MSG");
		userMsgNode.setId(Constants.MSG_NODEID_U);
		//未读消息大于0才显示数量
		if(userCount > 0){
			userMsgNode.setName("用户消息（"+userCount+"）");
		}else{
			userMsgNode.setName("用户消息");
		}
		userMsgNode.setpId(rootMsg.getId());
		userMsgNode.setIconSkin("user");
		
		TreeData sysMsgNode = new TreeData();
		sysMsgNode.setDataType("MSG");
		sysMsgNode.setId(Constants.MSG_NODEID_SYS);
		//未读消息大于0才显示数量
		if(sysMsgCount > 0){
			sysMsgNode.setName("系统消息（"+sysMsgCount+"）");
		}else{
			sysMsgNode.setName("系统消息");
		}
		
		sysMsgNode.setpId(rootMsg.getId());
		sysMsgNode.setIconSkin("sys");
		
		TreeData notReadMsgNode = new TreeData();
		notReadMsgNode.setDataType("MSG");
		notReadMsgNode.setId(Constants.MSG_NODEID_NR);
		//未读消息大于0才显示数量
		if(totalCount > 0){
			notReadMsgNode.setName("未读消息（"+totalCount+"）");
		}else{
			notReadMsgNode.setName("未读消息");
		}
		notReadMsgNode.setpId(rootMsg.getId());
		notReadMsgNode.setIconSkin("noread");
		
		dataList.add(rootMsg);
		dataList.add(notReadMsgNode);
		dataList.add(sysMsgNode);
		dataList.add(userMsgNode);
		
		return dataList;
	}

	@Override
	public List<TreeData> buildPersonalSubject(String userId) {
		List<TreeData> dataList = new ArrayList<TreeData>();
		
		//----------------设置系统定义标签---------------------------
		TreeData rootP = new TreeData();
		rootP.setDataType("SUBJECT");
		rootP.setId(Constants.SUBJECT_PID_P);
		rootP.setName("个人专题");
		rootP.setIsParent("true");
		rootP.setIconSkin("psub");
		
		TreeData tagP = new TreeData();
		tagP.setDataType("TAG");
		tagP.setId("tag_personal");
		tagP.setName("我的标签");
		tagP.setpId(Constants.SUBJECT_PID_P);
		tagP.setIcon(AppContextUtils.getContextPath() + "/webpage/front/images/tree/tag.png");
		
		TreeData recycleP = new TreeData();
		recycleP.setDataType("RECYCLEP");
		recycleP.setId("recycle_personal");
		recycleP.setName("回收站");
		recycleP.setIsParent("true");
		recycleP.setpId(Constants.SUBJECT_PID_P);
		recycleP.setIcon(AppContextUtils.getContextPath() + "/webpage/front/images/tree/arrow_refresh.png");
		//--------------------------------------------------------------------
		Map<String, String> tagMap = new HashMap<String, String>();
		tagMap.put("dataType", "TAG");
		tagMap.put("branchId", "tag_personal");
		tagMap.put("icon", AppContextUtils.getContextPath() + "/webpage/front/images/tree/tag_purple.png");
		//查询用户个人标签
		List<TagEntity> tagList = tagService.findUserTags(userId);
		
		// 转换为TreeData格式 标签
		if(tagList != null && !tagList.isEmpty()){
			dataList.addAll(TreeUtils.transformObjectList2TreeDataList(tagList, "id", "name", "pId", tagMap));
		}
		
		dataList.add(rootP);
		dataList.add(tagP);
		dataList.add(recycleP);
		
		//专题转TreeData自定义字段
		Map<String, String> subjectMap = new HashMap<String, String>();
		subjectMap.put("dataType", "SUBJECT");
		subjectMap.put("icon", AppContextUtils.getContextPath() + "/webpage/front/images/tree/subject.png");
		
		//目录转TreeData自定义字段
		Map<String, String> dirMap = new HashMap<String, String>();
		dirMap.put("dataType", "DIRECTORY");
		dirMap.put("icon", AppContextUtils.getContextPath() + "/webpage/front/images/tree/folder.png");
		//个人专题集合
		List<SubjectEntity> subPList = subjectService.findUserOwnSubject(userId);
		if(subPList != null && !subPList.isEmpty()){
			subjectMap.put("branchId", Constants.SUBJECT_PID_P);  //标识个人专题分支
			dataList.addAll(TreeUtils.transformObjectList2TreeDataList(subPList, "id", "subjectName", "parentId", subjectMap));
		}
		
		for(SubjectEntity sub : subPList){
			/*List<DirectoryEntity> dirList = directoryService.findDirsBySubject(sub.getId());
			dirMap.put("branchId", sub.getId());
			// 转换为TreeData格式
			if(dirList != null && !dirList.isEmpty()){
				dataList.addAll(TreeUtils.transformObjectList2TreeDataList(dirList, "id", "dirName", "pId", dirMap));
			}*/
			dataList.addAll(loadSubjectNode(userId, sub, false));
		}
		// 回收站目录
		List<DirectoryEntity> dirDelList = directoryService.findDeletedDirs(userId, Constants.SUBJECT_TYPE_P);
		if(dirDelList != null && !dirDelList.isEmpty()){
			dirMap.put("branchId", "RECYCLEP");
			dirMap.put("pId", recycleP.getId());
			dataList.addAll(TreeUtils.transformObjectList2TreeDataList(dirDelList, "id", "dirName", "pId", dirMap));
		}
		
		return dataList;
	}
	
	@Override
	public List<TreeData> loadSubjectNode(String userId, SubjectEntity subject, boolean order) {
		List<TreeData> dataList = new ArrayList<TreeData>();
		Map<String, String> dirMap = new HashMap<String, String>();
		dirMap.put("dataType", "DIRECTORY");
		dirMap.put("icon", AppContextUtils.getContextPath() + "/webpage/front/images/tree/folder.png");
		
		List<DirectoryEntity> dirList = directoryService.findDirsBySubjectOderByTime(subject.getId(),false);
		List<TreeData> dirDataList = new ArrayList<TreeData>();
		dirMap.put("branchId", subject.getId());
		// 转换为TreeData格式
		if(dirList != null && !dirList.isEmpty()){
			dirDataList = TreeUtils.transformObjectList2TreeDataList(dirList, "id", "dirName", "pId", dirMap);
		}
		if(order){
			dataList.addAll(dirDataList);
		}
		if(subject.getSubjectType() == Constants.SUBJECT_TYPE_M){
			Map<String, String> tagMap = new HashMap<String, String>();
			tagMap.put("dataType", "TAG");
			tagMap.put("icon", AppContextUtils.getContextPath() + "/webpage/front/images/tree/tag_purple.png");
			tagMap.put("branchId", subject.getId());
			
			// 每个多人专题一组标签
			TreeData remenber = new TreeData();
			remenber.setDataType("REMENBER");
			remenber.setId("remenber_subject_" + subject.getId());
			remenber.setName("团队成员");
			remenber.setBranchId(subject.getId());
			remenber.setpId(subject.getId());
			remenber.setSubjectId(subject.getId());
			remenber.setIcon(AppContextUtils.getContextPath() + "/webpage/front/images/tree/remenber.png");
			dataList.add(remenber);
			
			List<RoleUser> roleUserList=roleService.findSubjectUsers(subject.getId());
			for (RoleUser roleUser : roleUserList) {
				TreeData remenberchild = new TreeData();
				remenberchild.setDataType("REMENBERCHILD");
				remenberchild.setId(roleUser.getUserId());
				remenberchild.setName(roleUser.getAccountEntity().getUserName());
				remenberchild.setBranchId(subject.getId());
				remenberchild.setpId(remenber.getId());
				remenberchild.setSubjectId(subject.getId());
				remenberchild.setIcon(AppContextUtils.getContextPath() + "/webpage/front/images/tree/remenberchild.png");
				dataList.add(remenberchild);
			}
			
			// 每个多人专题一组标签
			TreeData tag = new TreeData();
			tag.setDataType("TAG");
			tag.setId("tag_subject_" + subject.getId());
			tag.setName("专题标签");
			tag.setBranchId(subject.getId());
			tag.setpId(subject.getId());
			tag.setSubjectId(subject.getId());
			tag.setIcon(AppContextUtils.getContextPath() + "/webpage/front/images/tree/tag.png");
			
			TreeData recycle = new TreeData();
			recycle.setDataType("RECYCLE");
			recycle.setId("recycle_subject_" + subject.getId());
			recycle.setName(Constants.RECYCLE_NODE_NAME);
			recycle.setIsParent("true");
			recycle.setpId(subject.getId());
			recycle.setSubjectId(subject.getId());
			recycle.setIcon(AppContextUtils.getContextPath() + "/webpage/front/images/tree/arrow_refresh.png");
			
			dataList.add(tag);
			dataList.add(recycle);//
			
			// 回收站目录
			List<DirectoryEntity> dirDelList = directoryService.findDeletedDirs(userId, subject.getId());
			if(dirDelList != null && !dirDelList.isEmpty()){
				dirMap.put("branchId", "RECYCLE");
				dirMap.put("pId", recycle.getId());
				dataList.addAll(TreeUtils.transformObjectList2TreeDataList(dirDelList, "id", "dirName", "pId", dirMap));
			}
			
			
			List<TagEntity> subTagList = tagService.findTagBySubject(subject.getId());
			// 转换为TreeData格式 标签
			if(subTagList != null && !subTagList.isEmpty()){
				dataList.addAll(TreeUtils.transformObjectList2TreeDataList(subTagList, "id", "name", "pId", tagMap));
			}
		}
		
		if(!order){
			dataList.addAll(dirDataList);
		}
		return dataList;
	}
	
	@Override
	public List<TreeData> loadRecycleNode(String userId, int subjectType, String recycleBranchId, String recycleNodeId) {
		List<TreeData> dataList = new ArrayList<TreeData>();
		
		List<DirectoryEntity> dirDelList = null;
		if(subjectType == Constants.SUBJECT_TYPE_P){
			dirDelList = directoryService.findDeletedDirs(userId, subjectType);
		}else{
			String subjectId = recycleNodeId.substring(recycleNodeId.lastIndexOf("_") + 1);
			dirDelList = directoryService.findDeletedDirs(userId, subjectId);
		}
		Map<String, String> dirMap = new HashMap<String, String>();
		dirMap.put("dataType", "DIRECTORY");
		dirMap.put("icon", AppContextUtils.getContextPath() + "/webpage/front/images/tree/folder.png");
		if(dirDelList != null && !dirDelList.isEmpty()){
			dirMap.put("branchId", recycleBranchId);
			dirMap.put("pId", recycleNodeId);
			dataList.addAll(TreeUtils.transformObjectList2TreeDataList(dirDelList, "id", "dirName", "pId", dirMap));
		}
		
		return dataList;
	}
	
	@Override
	public List<TreeData> buildSharedSubject(String userId) {
		List<TreeData> dataList = new ArrayList<TreeData>();
		//多人专题根节点
		TreeData rootM = new TreeData();
		rootM.setDataType("SUBJECT");
		rootM.setId(Constants.SUBJECT_PID_M);
		rootM.setName("多人专题");
		rootM.setIsParent("true");
		rootM.setIconSkin("msub");
		
		dataList.add(rootM);
		
		//专题转TreeData自定义字段
		Map<String, String> subjectMap = new HashMap<String, String>();
		subjectMap.put("dataType", "SUBJECT");
		subjectMap.put("icon", AppContextUtils.getContextPath() + "/webpage/front/images/tree/subject.png");
		
		//多个专题集合
		List<SubjectEntity> subMList = subjectService.findPermissionSubject(userId);
		if(subMList != null && !subMList.isEmpty()){
			subjectMap.put("branchId", Constants.SUBJECT_PID_M);  //标识多人专题分支
			dataList.addAll(TreeUtils.transformObjectList2TreeDataList(subMList, "id", "subjectName", "parentId", subjectMap));
		}
		
		for(SubjectEntity sub : subMList){
			dataList.addAll(loadSubjectNode(userId, sub, false));
		}
		return dataList;
	}

}
