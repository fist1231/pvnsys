package com.pvnsys.ttts.tttsGwtClient.client.model.charts;

import java.util.List;

import com.google.web.bindery.requestfactory.shared.ProxyFor;
import com.google.web.bindery.requestfactory.shared.ValueProxy;
import com.pvnsys.ttts.tttsGwtClient.client.model.stocks.StockVOProxy;
import com.pvnsys.ttts.tttsGwtClient.shared.dto.ChartsDTO;

@ProxyFor(ChartsDTO.class)
public interface ChartsDTOProxy extends ValueProxy {

	public List<StockVOProxy> getData();
	public MacdVOProxy getMacd();

}
