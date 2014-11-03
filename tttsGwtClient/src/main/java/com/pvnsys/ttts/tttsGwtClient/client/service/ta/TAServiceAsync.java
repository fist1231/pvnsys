package com.pvnsys.ttts.tttsGwtClient.client.service.ta;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.pvnsys.ttts.tttsGwtClient.shared.ta.MacdVO;

/**
 * The async counterpart of <code>AuthenticationService</code>.
 */
public interface TAServiceAsync {
	void getMacd(Double[] close, AsyncCallback<MacdVO> callback) throws IllegalArgumentException;
}
