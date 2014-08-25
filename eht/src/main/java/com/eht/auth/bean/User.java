package com.eht.auth.bean;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.eht.common.constant.Constants;
import com.eht.common.constant.RoleName;
import com.eht.user.entity.AccountEntity;

public class User implements UserDetails{

	private static final long serialVersionUID = 1L;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Collection<SimpleGrantedAuthority> c = new ArrayList<SimpleGrantedAuthority>();
		SimpleGrantedAuthority auth = new SimpleGrantedAuthority(RoleName.OAUTH2_ROLE_USER);
		c.add(auth);
		return c;
	}

	@Override
	public String getPassword() {
		return ((AccountEntity)this).getPassword();
	}

	@Override
	public String getUsername() {
		return ((AccountEntity)this).getUserName();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return ((AccountEntity)this).getStatus() == Constants.ENABLED ? true : false;
	}

}
