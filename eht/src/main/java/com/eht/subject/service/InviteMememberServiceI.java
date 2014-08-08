package com.eht.subject.service;

import org.jeecgframework.core.common.service.CommonService;
import com.eht.subject.entity.InviteMememberEntity;

public interface InviteMememberServiceI extends CommonService{
  
	/**
	 * 
	 * @param inviteMemember
	 * @return 返回ID
	 */
	public String addInviteMemember(InviteMememberEntity  inviteMemember) throws Exception;
}
