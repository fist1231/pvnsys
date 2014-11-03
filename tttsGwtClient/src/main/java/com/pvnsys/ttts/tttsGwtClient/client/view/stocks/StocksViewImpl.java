package com.pvnsys.ttts.tttsGwtClient.client.view.stocks;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.pvnsys.ttts.tttsGwtClient.client.view.common.SimpleView;
import com.sksamuel.gwt.websockets.Websocket;
import com.sksamuel.gwt.websockets.WebsocketListener;

public class StocksViewImpl<T> extends SimpleView implements StocksView<T> {

	  private Controller<T> controller;
	  private Websocket socket;
	
	
	private static StocksViewUiBinder uiBinder = GWT.create(StocksViewUiBinder.class);
	private MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();


	@SuppressWarnings("rawtypes")
	@UiTemplate("StocksView.ui.xml")
	interface StocksViewUiBinder extends UiBinder<Widget, StocksViewImpl> {
	}

	@UiField
	VerticalPanel vPanelWS;
	
	@UiField
	ScrollPanel sPanelWS;

	@UiField
	HorizontalPanel hPanelWSConnection;

	@UiField
	HorizontalPanel hPanelWSButtons;

	@UiField
	Button startFeed;

	@UiField
	Button stopFeed;

	@UiField
	Button connect;

	@UiField
	Label connectionLabel;
	
	@UiField
	Label connectionValidationLabel;

	@UiField
	TextBox connectionString;
	
	
	public StocksViewImpl() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	  public void setController(Controller<T> controller) {
		    this.controller = controller;
	  }
	

	public StocksViewImpl(String firstName) {
		initWidget(uiBinder.createAndBindUi(this));
		sPanelWS.setAlwaysShowScrollBars(true);
		connect.setVisible(true);
		startFeed.setVisible(false);
		stopFeed.setVisible(false);
		connectionLabel.setText("Disconnected");
		
		connect.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
		    	socket = getWebSocket(connectionString.getText());
			    if(socket != null) {
			    	if(connectionString.getText() != null && connectionString.getText().trim().length() > 0) {
				    	socket.open();
				    	connectionValidationLabel.setText("");
			    	} else {
			    		connectionValidationLabel.setText("Please enter connection string. Format: [127.0.0.1:6969]");
			    	}
			    }
			}
		});

		startFeed.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
			    String msg = "{ \"id\":\"ID-3\", \"msgType\":\"FEED_REQ\", \"client\":\"TBD_ON_SERVER\", \"payload\":\"Omg, GWT, lolz, WTF ???\" }";
			    if(socket != null) {
			    	socket.send(msg);
			    }
				startFeed.setVisible(false);
				stopFeed.setVisible(true);
			}
		});
		
		stopFeed.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
			    if(socket != null) {
			    	socket.close();
			    }
				startFeed.setVisible(false);
				stopFeed.setVisible(false);
				connect.setVisible(true);
				connectionLabel.setText("Disconnected");
			}
		});
		
	}
	
	public Widget asWidget() {
		return this;
	}

	private Websocket getWebSocket(final String connectionString) {
		Websocket socket = new Websocket("ws://" + connectionString + "/feed/ws");
		socket.addListener(new WebsocketListener() {
			
			@Override
			public void onOpen() {
				connectionLabel.setText("Successfully connected to " + connectionString);
				startFeed.setVisible(true);
				connect.setVisible(false);
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onMessage(String msg) {
				vPanelWS.add(new Label(msg));
				sPanelWS.scrollToBottom();
			}
			
			@Override
			public void onClose() {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		return socket;
		
	}
	
}
