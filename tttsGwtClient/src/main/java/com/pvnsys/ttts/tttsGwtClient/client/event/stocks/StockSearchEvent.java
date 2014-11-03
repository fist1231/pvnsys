package com.pvnsys.ttts.tttsGwtClient.client.event.stocks;

import com.google.gwt.event.shared.GwtEvent;

public class StockSearchEvent extends GwtEvent<StockSearchEventHandler> {
  public static Type<StockSearchEventHandler> TYPE = new Type<StockSearchEventHandler>();

  private final String text;
  
  public StockSearchEvent(String text) {
    this.text = text;
  }
  
  public String getText() {
	  return text; 
  }
  
  @Override
  public Type<StockSearchEventHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(StockSearchEventHandler handler) {
    handler.onSearch(this);
  }
}
