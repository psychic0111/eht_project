package com.eht.common.webinterceptor;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

public class UserAuthorizeFilter extends AbstractPreAuthenticatedProcessingFilter{
	
	private UserDetailsService userAuthService;

	@Override
	protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
		Enumeration<String> enu = request.getHeaderNames();
		while(enu.hasMoreElements()){
			String name = enu.nextElement();
			System.out.println(name + "=============" + request.getHeader(name));
		}
		Object principal = request.getUserPrincipal() == null ? null : request.getUserPrincipal().getName();
		if (logger.isDebugEnabled()) {
            logger.debug("PreAuthenticated UserAuthorizeFilter principal: " + principal);
        }
        return principal;
	}

	@Override
	protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
		return null;
	}

	public UserDetailsService getUserAuthService() {
		return userAuthService;
	}

	public void setUserAuthService(UserDetailsService userAuthService) {
		this.userAuthService = userAuthService;
	} 
	
	/*
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
		if (postOnly && !request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }

        String username = request.getParameter(usernameParameter);
        String password = request.getParameter(passwordParameter);

        if (username == null) {
            username = "";
        }

        if (password == null) {
            password = "";
        }

        username = username.trim();

        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);

        // Allow subclasses to set the "details" property
        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));

        return this.getAuthenticationManager().authenticate(authRequest);
	}
	*/
	
	
}
