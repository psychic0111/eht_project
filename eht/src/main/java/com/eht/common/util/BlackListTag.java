package com.eht.common.util;

import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import com.eht.common.constant.Constants;
import com.eht.note.service.NoteServiceI;
import com.eht.resource.service.ResourceActionService;
import com.eht.subject.service.DirectoryServiceI;
import com.eht.user.entity.AccountEntity;

public class BlackListTag extends TagSupport {

	private static final long serialVersionUID = 1L;
	
	private boolean result;
	
	//资源名称
	private String resource;
	
	//资源数据主键，模块的是classNameId
	private String primaryKey;
	
	public BlackListTag(){
		init();
	}
	
	public int doStartTag() throws JspException {
		HttpSession session = this.pageContext.getSession();
		Object obj = session.getAttribute(Constants.SESSION_USER_ATTRIBUTE);
		
		if(obj != null){
			AccountEntity user = (AccountEntity) obj;
			if(resource.equalsIgnoreCase("directory")){
				DirectoryServiceI service = (DirectoryServiceI) AppContextUtils.getBean("directoryService");
				result = service.inDirBlackList(user.getId(), primaryKey);
			}
			if(resource.equalsIgnoreCase("note")){
				NoteServiceI service = (NoteServiceI) AppContextUtils.getBean("noteService");
				result = service.inNoteBlackList(user.getId(), primaryKey);
			}
		}
		if (!this.result) {
			return 1;
		}
		return 0;
	}
	
	@Override
	public void release() {
		super.release();
		init();
	}

	private void init(){
		this.result = false;
		this.resource = null;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public String getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(String primaryKey) {
		this.primaryKey = primaryKey;
	} 
}
