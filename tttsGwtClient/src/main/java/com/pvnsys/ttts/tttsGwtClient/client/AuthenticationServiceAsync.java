package com.pvnsys.ttts.tttsGwtClient.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>AuthenticationService</code>.
 */
public interface AuthenticationServiceAsync {
	void authenticate(String username, String password, AsyncCallback<String> callback)
			throws IllegalArgumentException;
	void isLoggedIn(AsyncCallback<Boolean> callback);
}
