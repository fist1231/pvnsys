package com.pvnsys.ttts.tttsGwtClient.client.view.charts;

import com.google.gwt.user.client.ui.Widget;
import com.pvnsys.ttts.tttsGwtClient.shared.dto.ChartsDTO;

public interface ChartsView<T> {

  public interface Controller<T> {
    void onSearchButtonClicked(String text);
    void onSuggestionEntered(String text);
  }
  
  void setController(Controller<T> presenter);
  void setData(ChartsDTO data);
  abstract Widget asWidget();
  
}
