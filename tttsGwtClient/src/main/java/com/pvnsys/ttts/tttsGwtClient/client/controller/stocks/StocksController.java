package com.pvnsys.ttts.tttsGwtClient.client.controller.stocks;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.HasWidgets;
import com.pvnsys.ttts.tttsGwtClient.client.controller.common.Controller;
import com.pvnsys.ttts.tttsGwtClient.client.service.stocks.StockRequestFactory;
import com.pvnsys.ttts.tttsGwtClient.client.view.stocks.StocksView;
import com.pvnsys.ttts.tttsGwtClient.shared.StockVO;

public class StocksController implements Controller, StocksView.Controller<StockVO> {  

  private StockRequestFactory stockFactory;
  private final EventBus eventBus;
  private final StocksView<StockVO> view;
  private String ticker = "ABX";
  
  public StocksController(StockRequestFactory stockFactory, EventBus eventBus, StocksView<StockVO> view) {
    this.eventBus = eventBus;
    this.stockFactory = stockFactory;
    this.view = view;
    this.view.setController(this);
  }

  public StocksController(StockRequestFactory stockFactory, EventBus eventBus, StocksView<StockVO> view, String text) {
	    this.eventBus = eventBus;
	    this.stockFactory = stockFactory;
	    this.view = view;
	    this.view.setController(this);
	    this.ticker = text;
	  }

  
  public void go(final HasWidgets container) {
    container.clear();

    container.add(view.asWidget());
  }


}
