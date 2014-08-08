package com.eht.common.util;

import java.util.List;

import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import com.eht.common.constant.Constants;
import com.eht.resource.service.ResourceActionService;
import com.eht.resource.service.ResourcePermissionService;
import com.eht.role.entity.RoleUser;
import com.eht.role.service.RoleService;
import com.eht.user.entity.AccountEntity;

public class PermissionTag extends TagSupport {

	private static final long serialVersionUID = 1L;
	
	private boolean result;
	
	//鉴权资源名称
	private String resource;
	
	//鉴权资源action
	private String action;
	
	//资源数据主键，模块的是classNameId
	private String primaryKey;
	
	private String subjectId;

	public PermissionTag(){
		init();
	}
	
	public int doStartTag() throws JspException {
		HttpSession session = this.pageContext.getSession();
		Object obj = session.getAttribute(Constants.SESSION_USER_ATTRIBUTE);
		
		if(obj != null){
			AccountEntity user = (AccountEntity) obj;
			ResourcePermissionService service = (ResourcePermissionService) AppContextUtils.getBean("resourcePermissionService");
			ResourceActionService resourceService = (ResourceActionService) AppContextUtils.getBean("resourceActionService");
			RoleService roleService = (RoleService) AppContextUtils.getBean("roleService");
			RoleUser ru = roleService.findUserRole(user.getId(), subjectId);
			if(ru != null){
				primaryKey = resourceService.findResourceByName(resource).getClassNameId() + "";
				result = service.hasPermissionRole(resource, action, primaryKey, ru.getRoleId());
				if(result){
					return 1;
				}
				return 0;
			}else{
				return 0;
			}
			
		}else{
			return 0;
		}
		
	}
	
	@Override
	public void release() {
		super.release();
		init();
	}

	private void init(){
		this.result = false;
		this.resource = null;
		this.action = null;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(String primaryKey) {
		this.primaryKey = primaryKey;
	}

	public String getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	} 
}
