package com.eht.auth.bean;

import java.util.ArrayList;
import java.util.Collection;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.eht.common.constant.Constants;
import com.eht.common.constant.RoleName;
import com.eht.user.entity.AccountEntity;

public class User implements UserDetails{

	private static final long serialVersionUID = 1L;

	@Override
	@JsonIgnore
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Collection<SimpleGrantedAuthority> c = new ArrayList<SimpleGrantedAuthority>();
		SimpleGrantedAuthority auth = new SimpleGrantedAuthority(RoleName.OAUTH2_ROLE_USER);
		c.add(auth);
		return c;
	}

	@Override
	@JsonIgnore
	public String getPassword() {
		return ((AccountEntity)this).getPassword();
	}

	@Override
	@JsonIgnore
	public String getUsername() {
		return ((AccountEntity)this).getUserName();
	}

	@Override
	@JsonIgnore
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	@JsonIgnore
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	@JsonIgnore
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	@JsonIgnore
	public boolean isEnabled() {
		return ((AccountEntity)this).getStatus() == Constants.ENABLED ? true : false;
	}

}
