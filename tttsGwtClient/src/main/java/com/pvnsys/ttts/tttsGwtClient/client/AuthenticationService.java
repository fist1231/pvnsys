package com.pvnsys.ttts.tttsGwtClient.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client-side stub for the RPC service.
 */
@RemoteServiceRelativePath("authenticate")
public interface AuthenticationService extends RemoteService {
	String authenticate(String username, String password) throws IllegalArgumentException;
	Boolean isLoggedIn();
}
