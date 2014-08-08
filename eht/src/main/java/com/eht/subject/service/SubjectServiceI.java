package com.eht.subject.service;

import java.io.Serializable;
import java.util.List;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.jeecgframework.core.common.service.CommonService;
import com.eht.subject.entity.InviteMememberEntity;
import com.eht.subject.entity.SubjectEntity;
import com.eht.user.entity.AccountEntity;

public interface SubjectServiceI extends CommonService{
	/**
	 * 
	 * @param subject
	 * @return 返回专题groupId
	 */
	public Serializable addSubject(SubjectEntity subject);
	/**
	 * 更新专题
	 * @param subject
	 */
	public void updateSubject(SubjectEntity subject);
	/**
	 * 删除专题
	 * @param id
	 */
	public void deleteSubject(String id);
	/**
	 * 删除专题
	 * @param subject
	 */
	public void deleteSubject(SubjectEntity subject);
	
	/**
	 * 根据ID查询专题
	 * @param id
	 * @return
	 */
	public SubjectEntity getSubject(Serializable id);
	
	/**
	 * 添加专题成员
	 * @param 默认读者角色
	 * @return
	 */
	public void addSubjectMember(String subjectId, String userId, String roleId);
	
	/**
	 * 删除专题成员
	 * @param user
	 * @return
	 */
	public void removeSubjectMember(String subjectId, String userId);
	
	/**
	 * 根据名称查询专题
	 * @param subjectName
	 */
	public List<SubjectEntity> findSubjectByName(String subjectName);
	
	/**
	 * 根据类型查询专题
	 * @param subjectType
	 * @return
	 */
	public List<SubjectEntity> findSubjectByType(Integer subjectType);
	
	/**
	 * 查询用户个人专题
	 * @param userId
	 * @return
	 */
	public List<SubjectEntity> findUserOwnSubject(String userId);
	
	/**
	 * 查询用户个人、多人专题
	 * @param userId
	 * @return
	 */
	public List<SubjectEntity> findUsersSubject(String userId);
	
	/**
	 * 查询用户加入的多人专题
	 * @param userId
	 * @return
	 */
	public List<SubjectEntity> findPermissionSubject(String userId);
	
	/**
	 * 检查专题名称是否存在
	 * @param subjectName
	 * @return 专题类型
	 */
	public Integer existsSubjectName(String subjectName,String subjectId);
	
	
	/**
	 * 多人专题邀请成员
	 * @return
	 */
	public void inviteMemember(String email[],int type,HttpServletRequest request,SubjectEntity SubjectEntity)throws Exception;
	
	/**
	 * 多人专题接受成员
	 * @return
	 */
	public void acceptInviteMember(InviteMememberEntity inviteMememberEntity,AccountEntity user)throws Exception;
	
	/**
	 * 删除专题成员
	 * @return
	 */
	public void delInviteMember(String []ids,String subjectId)throws Exception;
	
	/**
	 * 更新专题成员角色
	 * @return
	 */
	public void updateInviteMemberRole(String []ids,String type)throws Exception;
	
	/**
	 * 查看专题生成大纲
	 * @return
	 */
    public void showCatalogueSubject(SubjectEntity SubjectEntity,AccountEntity user,XWPFDocument doc);
    
    /**
	 * 查看专题生成大纲
	 * @return
	 */
    public void showCatalogueSubject(SubjectEntity SubjectEntity,AccountEntity user,StringBuffer sb);
    
    /**
	 * 导出专题生成ZIP
	 * @return
	 */
    public void exportSuject(ZipOutputStream zos,String subjectId,HttpServletRequest request,AccountEntity user) throws Exception;
    
    /**
	 * 导入专题
	 * @return
	 */
    public void leadinginSuject(HttpServletRequest request) throws Exception;
	
    /**
     * 标记删除专题
     * @param subject
     */
    public void markDelSubject(SubjectEntity subject);
    
}