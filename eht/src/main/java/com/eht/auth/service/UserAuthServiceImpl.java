package com.eht.auth.service;

import org.jeecgframework.core.common.service.impl.CommonServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.provider.ClientDetailsService;

import com.eht.common.constant.Constants;
import com.eht.user.entity.AccountEntity;
import com.eht.user.service.AccountServiceI;
/**
 * 前台用户service
 * @author cuixy
 *
 */
public class UserAuthServiceImpl extends CommonServiceImpl implements UserDetailsService{
	
	@Autowired
	private AccountServiceI accountService;
	
	private ClientDetailsService clientDetailsService;
	
	public UserAuthServiceImpl(){
		super();
	}
	public UserAuthServiceImpl(ClientDetailsService clientDetailsService){
		this.clientDetailsService = clientDetailsService;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		AccountEntity account = accountService.findUserByAccount(username);
		if(account.getStatus() == Constants.ENABLED && account.getDeleted() == Constants.DATA_NOT_DELETED){
			return account;
		}
		return null;
	}

}
