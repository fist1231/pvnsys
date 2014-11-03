package com.pvnsys.ttts.tttsGwtClient.client.service.ta;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.pvnsys.ttts.tttsGwtClient.shared.ta.MacdVO;

/**
 * The client-side stub for the RPC service.
 */
@RemoteServiceRelativePath("taUtil")
public interface TAService extends RemoteService {
	MacdVO getMacd(Double[] close) throws IllegalArgumentException;
}
