package com.eht.subject.service;

import java.util.List;

import org.jeecgframework.core.common.service.CommonService;
import com.eht.subject.entity.InviteMememberEntity;

public interface InviteMememberServiceI extends CommonService{
  
	/**
	 * 
	 * @param inviteMemember
	 * @return 返回ID
	 */
	public String addInviteMemember(InviteMememberEntity  inviteMemember) throws Exception;
	
	
	public List<InviteMememberEntity> findInviteMemember(String subjectId);
}
