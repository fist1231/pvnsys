package com.pvnsys.ttts.tttsGwtClient.client.view.stocks;

import com.google.gwt.user.client.ui.Widget;

public interface StocksView<T> {

  public interface Controller<T> {
	    void onQuoteArrived(String text);
  }
  
  void setController(Controller<T> presenter);
  abstract Widget asWidget();
  
}
