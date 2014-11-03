package com.pvnsys.ttts.tttsGwtClient.client.event.stocks;

import com.google.gwt.event.shared.GwtEvent;

public class StocksEvent extends GwtEvent<StocksEventHandler> {
  public static Type<StocksEventHandler> TYPE = new Type<StocksEventHandler>();
  
  @Override
  public Type<StocksEventHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(StocksEventHandler handler) {
    handler.onSort(this);
  }
}
