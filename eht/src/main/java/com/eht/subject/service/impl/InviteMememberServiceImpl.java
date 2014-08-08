package com.eht.subject.service.impl;

import java.util.List;

import org.jeecgframework.core.common.service.impl.CommonServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.eht.subject.entity.InviteMememberEntity;
import com.eht.subject.service.InviteMememberServiceI;

@Service("inviteMememberService")
@Transactional
public class InviteMememberServiceImpl extends CommonServiceImpl  implements InviteMememberServiceI{

	@Override
	public String addInviteMemember(InviteMememberEntity inviteMemember)throws Exception {
		List<InviteMememberEntity> list = findHql("from InviteMememberEntity i where i.subjectid =?  and i.email=?", new Object[]{inviteMemember.getSubjectid(),inviteMemember.getEmail()});
		if(list.size()>0){
			InviteMememberEntity invite=list.get(0);
			if(invite.getRoleid().equals(inviteMemember.getRoleid())){
				return invite.getId();
			}else{
				saveOrUpdate(invite);
				invite.setRoleid(inviteMemember.getRoleid());
				return invite.getId();
			}
		}else{
			save(inviteMemember);
		}
		return inviteMemember.getId();
	}

}
