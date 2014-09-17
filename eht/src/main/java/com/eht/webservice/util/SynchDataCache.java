package com.eht.webservice.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eht.comment.entity.CommentEntity;
import com.eht.group.entity.GroupUser;
import com.eht.message.entity.MessageEntity;
import com.eht.note.entity.AttachmentEntity;
import com.eht.note.entity.NoteEntity;
import com.eht.resource.entity.ClassName;
import com.eht.role.entity.Role;
import com.eht.role.entity.RoleUser;
import com.eht.subject.entity.DirectoryEntity;
import com.eht.subject.entity.SubjectEntity;
import com.eht.system.bean.ClientEntity;
import com.eht.tag.entity.TagEntity;
import com.eht.template.entity.TemplateEntity;
import com.eht.user.entity.AccountEntity;
import com.eht.webservice.bean.Step;

public class SynchDataCache {
	
	private static Map<String, ClassName> classNameMap = new HashMap<String, ClassName>();
	
	/**
	 * 同步数据类型与相应实体
	 */
	private static Map<String, Class<?>> dataClassMap = new HashMap<String, Class<?>>();
	
	/**
	 * 同步顺序
	 */
	private static List<Step> stepList = null;
	
	/**
	 * 客户端获取同步数据类型的顺序 -- 添加、修改
	 */
	private static String[] datasSort = null;
	
	/**
	 * 客户端获取同步数据类型的顺序 -- 删除
	 */
	private static String[] datasDeleteSort = null;
	
	/**
	 * 客户端获取同步数据操作的顺序
	 */
	private static String[] actionSort = new String[]{"A", "U", "D"};
	
	static{
		dataClassMap.put("TEMPLATE", TemplateEntity.class);
		dataClassMap.put("SUBJECT", SubjectEntity.class);
		dataClassMap.put("DIRECTORY", DirectoryEntity.class);
		dataClassMap.put("TAG", TagEntity.class);
		dataClassMap.put("NOTE", NoteEntity.class);
		dataClassMap.put("ATTACHMENT", AttachmentEntity.class);
		dataClassMap.put("USER", AccountEntity.class);
		dataClassMap.put("CLIENT", ClientEntity.class);
		dataClassMap.put("ROLE", Role.class);
		dataClassMap.put("TAG", TagEntity.class);
		dataClassMap.put("TEMPLATE", TemplateEntity.class);
		dataClassMap.put("COMMENT", CommentEntity.class);
		dataClassMap.put("MESSAGE", MessageEntity.class);
		dataClassMap.put("NOTEBLACK", GroupUser.class);
		dataClassMap.put("DIRECTORYBLACK", GroupUser.class);
		dataClassMap.put("SUBJECTUSER", RoleUser.class);
	}
	
	public static String[] getDatasSort() {
		return datasSort;
	}
	
	/**
	 * 数据类型同步顺序倒序
	 * @return
	 */
	public static String[] getReverseDatasSort() {
		return datasDeleteSort;
	}
	
	public static String[] getActionSort() {
		return actionSort;
	}

	public static ClassName getResourceByName(String resourceName){
		return classNameMap.get(resourceName);
	}
	
	public static Class<?> getDataClass(String dataType){
		return dataClassMap.get(dataType);
	}

	public static void setDatasSort(String[] datasSort) {
		SynchDataCache.datasSort = datasSort;
	}

	public static void setDatasDeleteSort(String[] datasDeleteSort) {
		SynchDataCache.datasDeleteSort = datasDeleteSort;
	}

	public static List<Step> getStepList() {
		return stepList;
	}

	public static void setStepList(List<Step> stepList) {
		SynchDataCache.stepList = stepList;
	}
}
