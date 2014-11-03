package com.pvnsys.ttts.tttsGwtClient.client.model.charts;

import java.util.List;

import com.google.web.bindery.requestfactory.shared.ProxyFor;
import com.google.web.bindery.requestfactory.shared.ValueProxy;
import com.pvnsys.ttts.tttsGwtClient.shared.ta.MacdVO;

@ProxyFor(MacdVO.class)
public interface MacdVOProxy extends ValueProxy {

	public List<Double> getOutMACD();
	public List<Double> getOutMACDSignal();
	public List<Double> getOutMACDHist();
	public int getBegin();
	public int getLength();
	
}
