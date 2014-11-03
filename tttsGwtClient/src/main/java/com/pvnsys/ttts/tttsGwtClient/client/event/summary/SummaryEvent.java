package com.pvnsys.ttts.tttsGwtClient.client.event.summary;

import com.google.gwt.event.shared.GwtEvent;

public class SummaryEvent extends GwtEvent<SummaryEventHandler>{
  public static Type<SummaryEventHandler> TYPE = new Type<SummaryEventHandler>();
  
  @Override
  public Type<SummaryEventHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(SummaryEventHandler handler) {
    handler.onLogin(this);
  }
}
