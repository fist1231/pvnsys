package com.pvnsys.ttts.tttsGwtClient.client.event.stocks;

import com.google.gwt.event.shared.EventHandler;

public interface StockSearchEventHandler extends EventHandler {
  void onSearch(StockSearchEvent event);
}
