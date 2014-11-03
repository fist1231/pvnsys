package com.pvnsys.ttts.tttsGwtClient.client.service.stocks;

import java.util.List;

import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.Service;
import com.pvnsys.ttts.tttsGwtClient.client.model.charts.ChartsDTOProxy;
import com.pvnsys.ttts.tttsGwtClient.client.model.stocks.StockVOProxy;
import com.pvnsys.ttts.tttsGwtClient.server.stocks.StockLocator;
import com.pvnsys.ttts.tttsGwtClient.server.stocks.StockServiceImpl;

@Service(value=StockServiceImpl.class, locator=StockLocator.class)
public interface StockRequest extends RequestContext {

	Request<List<StockVOProxy>> searchStocks(String input);
	Request<StockVOProxy> getLastStockQuote(String input);
	Request<List<String>> searchAllStockNames(String input);
	Request<String> authenticate(String user, String password);
	Request<List<StockVOProxy>> searchStockByName(String name);
	Request<ChartsDTOProxy> getChartsData(String name);
}
