package com.pvnsys.ttts.tttsGwtClient.client.view.summary;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.pvnsys.ttts.tttsGwtClient.client.view.common.SimpleView;
import com.pvnsys.ttts.tttsGwtClient.client.controller.summary.SummaryController;

public class SummaryView extends SimpleView implements SummaryController.Display {
  private TextBox name;
  private TextBox stockDate;
  private TextBox open;
  private TextBox close;
  private TextBox high;
  private TextBox low;
  private TextBox adjClose;
  private TextBox volume;
  private FlexTable statusTable;
  private Button loginButton;
  private boolean isLoggedIn;
  
  public SummaryView() {
	  buildUI();
  }
  
  public SummaryView(boolean isLoggedIn) {
	  this.isLoggedIn = isLoggedIn;
	  buildUI();
  }
  
  private void buildUI() {
	    DecoratorPanel contentDetailsDecorator = new DecoratorPanel();
	    contentDetailsDecorator.setWidth("18em");
	    initWidget(contentDetailsDecorator);

	    VerticalPanel contentDetailsPanel = new VerticalPanel();
	    contentDetailsPanel.setWidth("100%");

	    // Create the contacts list
	    //
	    statusTable = new FlexTable();
	    statusTable.setCellSpacing(0);
	    statusTable.setWidth("100%");
	    statusTable.addStyleName("contacts-ListContainer");
	    statusTable.getColumnFormatter().addStyleName(1, "add-contact-input");
	    name = new TextBox();
	    name.setEnabled(false);
	    stockDate = new TextBox();
	    stockDate.setEnabled(false);
	    open = new TextBox();
	    open.setEnabled(false);
	    close = new TextBox();
	    close.setEnabled(false);
	    high = new TextBox();
	    high.setEnabled(false);
	    low = new TextBox();
	    low.setEnabled(false);
	    adjClose = new TextBox();
	    adjClose.setEnabled(false);
	    volume = new TextBox();
	    volume.setEnabled(false);
	    initDetailsTable();
	    contentDetailsPanel.add(statusTable);
	    
	    HorizontalPanel menuPanel = new HorizontalPanel();
	    loginButton = new Button("Login");
	    menuPanel.add(loginButton);
	    contentDetailsPanel.add(menuPanel);

	    HorizontalPanel logoutPanel = new HorizontalPanel();
	    Anchor logoutLink = new Anchor("Log Out", false, GWT.getHostPageBaseURL() + "j_spring_security_logout");
	    logoutPanel.add(logoutLink);
	    contentDetailsPanel.add(logoutPanel);
	    
	    if(!isLoggedIn) {
		    loginButton.setVisible(true);
		    logoutLink.setVisible(false);
	    } else {
		    loginButton.setVisible(false);
		    logoutLink.setVisible(true);
	    }
	    contentDetailsDecorator.add(contentDetailsPanel);
	  
  }
  
  private void initDetailsTable() {
	  statusTable.setWidget(0, 0, new Label("Ticker"));
	  statusTable.setWidget(0, 1, name);
	  statusTable.setWidget(1, 0, new Label("Date"));
	  statusTable.setWidget(1, 1, stockDate);
	  statusTable.setWidget(2, 0, new Label("Open"));
	  statusTable.setWidget(2, 1, open);
	  statusTable.setWidget(3, 0, new Label("Close"));
	  statusTable.setWidget(3, 1, close);
	  statusTable.setWidget(4, 0, new Label("High"));
	  statusTable.setWidget(4, 1, high);
	  statusTable.setWidget(5, 0, new Label("Low"));
	  statusTable.setWidget(5, 1, low);
	  statusTable.setWidget(6, 0, new Label("Adj. Close"));
	  statusTable.setWidget(6, 1, adjClose);
	  statusTable.setWidget(7, 0, new Label("Volume"));
	  statusTable.setWidget(7, 1, volume);
//    firstName.setFocus(true);
  }
  
  public HasValue<String> getName() {
    return name;
  }

  public HasValue<String> getStockDate() {
    return stockDate;
  }

  public HasValue<String> getOpen() {
    return open;
  }

  public HasValue<String> getClose() {
	    return close;
  }

  public HasValue<String> getHigh() {
	    return high;
  }
  
  public HasValue<String> getLow() {
	    return low;
  }

  public HasValue<String> getAdjClose() {
	    return adjClose;
  }

  public HasValue<String> getVolume() {
	    return volume;
  }

  public HasClickHandlers getLoginButton() {
    return loginButton;
  }
  
  
  public Widget asWidget() {
    return this;
  }
  
}
