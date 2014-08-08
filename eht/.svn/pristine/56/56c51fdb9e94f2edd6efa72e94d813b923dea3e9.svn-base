package com.eht.auth.exception;

import org.springframework.security.core.Authentication;

@SuppressWarnings("serial")
public class UserRegisterException extends RuntimeException {

	private Authentication authentication;
	private transient Object extraInformation;

	public UserRegisterException(String msg) {
		super(msg);
	}

	public UserRegisterException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public UserRegisterException(String msg, Object extraInformation) {
		super(msg);
		/*if (extraInformation instanceof CredentialsContainer) {
			((CredentialsContainer) extraInformation).eraseCredentials();
		}*/
		this.extraInformation = extraInformation;
	}

	public Authentication getAuthentication() {
		return authentication;
	}

	public void setAuthentication(Authentication authentication) {
		this.authentication = authentication;
	}

	public Object getExtraInformation() {
		return extraInformation;
	}

	public void setExtraInformation(Object extraInformation) {
		this.extraInformation = extraInformation;
	}
	
}
