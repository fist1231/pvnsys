package com.pvnsys.ttts.tttsGwtClient.client.event.stocks;

import com.google.gwt.event.shared.EventHandler;

public interface StocksEventHandler extends EventHandler {
  void onSort(StocksEvent event);
}
