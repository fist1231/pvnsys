package com.pvnsys.ttts.tttsGwtClient.server.ta;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.pvnsys.ttts.tttsGwtClient.client.service.ta.TAService;
import com.pvnsys.ttts.tttsGwtClient.shared.ta.MacdVO;
import com.pvnsys.ttts.tttsGwtClient.shared.ta.TAUtil;

/**
 * The server-side implementation of the RPC service.
 */

public class TAServiceImpl extends RemoteServiceServlet implements TAService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8709158955210505825L;

	public MacdVO getMacd(Double[] close) {
		return TAUtil.getMacd(close);
	}

}
