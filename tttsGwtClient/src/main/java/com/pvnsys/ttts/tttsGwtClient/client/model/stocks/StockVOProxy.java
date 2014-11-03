package com.pvnsys.ttts.tttsGwtClient.client.model.stocks;

import java.util.Date;

import com.google.web.bindery.requestfactory.shared.ProxyFor;
import com.google.web.bindery.requestfactory.shared.ValueProxy;
import com.pvnsys.ttts.tttsGwtClient.shared.StockVO;

@ProxyFor(StockVO.class)
public interface StockVOProxy extends ValueProxy {

	public Long getId();
	public String getName();
	public double getOpen();
	public double getClose();
	public double getHigh();
	public double getLow();
	public double getAdjClose();
	public long getVolume();
	public Date getStockDate();
	
}
