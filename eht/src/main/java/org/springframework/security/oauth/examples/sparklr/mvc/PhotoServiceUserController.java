package org.springframework.security.oauth.examples.sparklr.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Michael Lavelle
 * 
 * Added to provide an endpoint from which Spring Social can obtain authentication details
 */
@RequestMapping("/me")
@Controller
public class PhotoServiceUserController {

/*	private UserDetailsService userDetailsService;

	
	@ResponseBody
	@RequestMapping("")
	public PhotoServiceUser getPhotoServiceUser(Principal principal)
	{
		UserDetails userDetails = userDetailsService.loadUserByUsername(principal.getName());
		return new PhotoServiceUser(userDetails.getUsername(),userDetails.getUsername());
	}

	public void setUserDetailsService(UserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}*/
	
}
