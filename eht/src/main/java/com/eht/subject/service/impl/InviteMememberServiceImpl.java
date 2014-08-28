package com.eht.subject.service.impl;

import java.util.List;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.jeecgframework.core.common.service.impl.CommonServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.eht.subject.entity.InviteMememberEntity;
import com.eht.subject.service.InviteMememberServiceI;
import com.eht.user.entity.AccountEntity;
import com.eht.user.service.AccountServiceI;

@Service("inviteMememberService")
@Transactional
public class InviteMememberServiceImpl extends CommonServiceImpl  implements InviteMememberServiceI{
  
	@Autowired
	private AccountServiceI accountService;
	
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
	
	@Override
	public List<InviteMememberEntity> findInviteMemember(String subjectId) {
		DetachedCriteria dc = DetachedCriteria.forClass(InviteMememberEntity.class);
		dc.add(Restrictions.eq("subjectid", subjectId));
		List<InviteMememberEntity> list = findByDetached(dc);
		for (InviteMememberEntity inviteMememberEntity : list) {
			AccountEntity  acct=accountService.findUserByEmail(inviteMememberEntity.getEmail());
			if(acct!=null){
				inviteMememberEntity.setUsername(acct.getUserName());
			}
		}
		return list;
	}

}
