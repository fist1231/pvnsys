package com.pvnsys.ttts.tttsGwtClient.client.controller.charts;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;
import com.pvnsys.ttts.tttsGwtClient.client.controller.common.Controller;
import com.pvnsys.ttts.tttsGwtClient.client.model.charts.ChartsDTOProxy;
import com.pvnsys.ttts.tttsGwtClient.client.model.stocks.StockVOProxy;
import com.pvnsys.ttts.tttsGwtClient.client.service.stocks.StockRequestFactory;
import com.pvnsys.ttts.tttsGwtClient.client.view.charts.ChartsView;
import com.pvnsys.ttts.tttsGwtClient.shared.ConstantsUtils;
import com.pvnsys.ttts.tttsGwtClient.shared.StockVO;
import com.pvnsys.ttts.tttsGwtClient.shared.dto.ChartsDTO;
import com.pvnsys.ttts.tttsGwtClient.shared.ta.MacdVO;

public class ChartsController implements Controller,
		ChartsView.Controller<StockVO> {

	private StockRequestFactory stockFactory;
	private final EventBus eventBus;
	private final ChartsView<StockVO> view;
	private String ticker;

	public ChartsController(StockRequestFactory stockFactory, EventBus eventBus, ChartsView<StockVO> view, String ticker) {
		this.eventBus = eventBus;
		this.stockFactory = stockFactory;
		this.view = view;
		this.view.setController(this);
		this.ticker = ticker;
		// getStocks(text);
	}

	public void go(final HasWidgets container) {
		container.clear();

		container.add(view.asWidget());
		getStockData(ticker);
	}

	@Override
	public void onSearchButtonClicked(String text) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSuggestionEntered(String text) {
		// TODO Auto-generated method stub

	}

	  private void getStockData(String text) {

		  stockFactory.stockRequest().getChartsData(text).fire(
				  new Receiver<ChartsDTOProxy>() {

					@Override
					public void onSuccess(ChartsDTOProxy chartsDtoProxy) {
						if(chartsDtoProxy != null && chartsDtoProxy.getData() != null && chartsDtoProxy.getData().size() > 1) {
							
							view.setData(toChartsDTO(chartsDtoProxy));
//						    RootPanel.get("loadingCharts").setVisible(false);
						}
						
					}

					@Override
					public void onFailure(ServerFailure error) {
						if(!ConstantsUtils.UNAUTHENTICATED_EXCEPTION_TYPE.equals(error.getExceptionType())) {
							Window.alert("We're all doomed in Charts: " + error.getMessage());
						}
					}
				});
	  }
	  
	  private ChartsDTO toChartsDTO(ChartsDTOProxy chartsDtoProxy) {
		
		  List<StockVO> stockVOList = new ArrayList<StockVO>(chartsDtoProxy.getData().size());
		  for(StockVOProxy stockVOProxy: chartsDtoProxy.getData()) {
			  stockVOList.add(
					  new StockVO(
							  stockVOProxy.getId(), 
							  stockVOProxy.getName(), 
							  stockVOProxy.getOpen(), 
							  stockVOProxy.getClose(), 
							  stockVOProxy.getHigh(), 
							  stockVOProxy.getLow(), 
							  stockVOProxy.getAdjClose(), 
							  stockVOProxy.getStockDate(), 
							  stockVOProxy.getVolume())
					  );
		  }
		  
		  MacdVO macdVO = new MacdVO(
				  chartsDtoProxy.getMacd().getOutMACD(), 
				  chartsDtoProxy.getMacd().getOutMACDSignal(), 
				  chartsDtoProxy.getMacd().getOutMACDHist(), 
				  chartsDtoProxy.getMacd().getBegin(), 
				  chartsDtoProxy.getMacd().getLength()
				  );
		  
		  ChartsDTO chartsDTO = new ChartsDTO(stockVOList, macdVO);		  return chartsDTO;
	  }
	
}
