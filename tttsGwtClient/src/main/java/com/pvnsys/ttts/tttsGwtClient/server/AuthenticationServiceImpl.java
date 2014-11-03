package com.pvnsys.ttts.tttsGwtClient.server;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.User;

import com.pvnsys.ttts.tttsGwtClient.client.AuthenticationService;
import com.pvnsys.ttts.tttsGwtClient.server.authentication.UserAuthenticationProvider;
import com.pvnsys.ttts.tttsGwtClient.server.common.BaseRemoteServiceServlet;

/**
 * The server-side implementation of the RPC service.
 */

public class AuthenticationServiceImpl extends BaseRemoteServiceServlet implements AuthenticationService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7434971412029328352L;
	
	@Autowired
	private UserAuthenticationProvider userAuthenticationProvider;

	public String authenticate(String username, String password) {
		User user = new User(username, password, true, true, true, true, new ArrayList<GrantedAuthority>());
		Authentication auth = new UsernamePasswordAuthenticationToken(username, password, new ArrayList<GrantedAuthority>());
		try {
		    auth = getUserAuthenticationProvider().authenticate(auth);
		} catch (BadCredentialsException ex) {
		    throw new AuthenticationServiceException(ex.getMessage(), ex);
		}
		SecurityContext sc = new SecurityContextImpl();
		sc.setAuthentication(auth);

		SecurityContextHolder.setContext(sc);
		
		return "success";
	}

	public Boolean isLoggedIn() {
		SecurityContext sc = SecurityContextHolder.getContext();
		if(sc != null) {
			Authentication auth = sc.getAuthentication();
			if(auth != null) {
				Object principal = auth.getPrincipal();
				if(principal != null) {
					if(auth instanceof AnonymousAuthenticationToken && "anonymousUser".equals(principal)) {
					
					} else {
						return true;
					}
				}
				
			}
		}
		return false;
	}
	
	public UserAuthenticationProvider getUserAuthenticationProvider() {
		return userAuthenticationProvider;
	}

	public void setUserAuthenticationProvider(
			UserAuthenticationProvider userAuthenticationProvider) {
		this.userAuthenticationProvider = userAuthenticationProvider;
	}

}
