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
	  private Websocket feedSocket;
	  private Websocket strategySocket;
	
	
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
	Button startStrategy;

	@UiField
	Button stopStrategy;
	
	@UiField
	Button connect;

	@UiField
	Button disconnect;
	
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
		disconnect.setVisible(false);
		startFeed.setVisible(false);
		stopFeed.setVisible(false);
		startStrategy.setVisible(false);
		stopStrategy.setVisible(false);
		connectionLabel.setText("Disconnected");
		connectionString.setText("127.0.0.1:6969");
		
		connect.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				feedSocket = getFeedWebSocket(connectionString.getText());
			    if(feedSocket != null) {
			    	if(connectionString.getText() != null && connectionString.getText().trim().length() > 0) {
			    		feedSocket.open();
				    	connectionValidationLabel.setText("");
			    	} else {
			    		connectionValidationLabel.setText("Please enter connection string. Format: [127.0.0.1:6969]");
			    	}
			    }
			    
				strategySocket = getStrategyWebSocket(connectionString.getText());
			    if(strategySocket != null) {
			    	if(connectionString.getText() != null && connectionString.getText().trim().length() > 0) {
			    		strategySocket.open();
			    	} else {
			    	}
			    }
			    
			}
		});

		disconnect.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
			    if(feedSocket != null) {
			    	feedSocket.close();
			    }
			    if(strategySocket != null) {
			    	strategySocket.close();
			    }
				startFeed.setVisible(false);
				stopFeed.setVisible(false);
				startStrategy.setVisible(false);
				stopStrategy.setVisible(false);
				connect.setVisible(true);
				disconnect.setVisible(false);
				connectionLabel.setText("Disconnected");
			}
		});
		
		startFeed.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
			    String msg = "{\"msgType\":\"FEED_REQ\", \"payload\":\"Sample Payload\" }";
			    if(feedSocket != null) {
			    	feedSocket.send(msg);
			    }
				startFeed.setVisible(false);
				stopFeed.setVisible(true);
				startStrategy.setVisible(false);
				stopStrategy.setVisible(false);
				disconnect.setVisible(false);
				vPanelWS.clear();
			}
		});
		
		stopFeed.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
			    String msg = "{\"msgType\":\"FEED_STOP_REQ\", \"payload\":\"Sample Payload\" }";
			    if(feedSocket != null) {
			    	feedSocket.send(msg);
			    }
				startFeed.setVisible(true);
				stopFeed.setVisible(false);
				startStrategy.setVisible(true);
				stopStrategy.setVisible(false);
				disconnect.setVisible(true);
			}
		});

		startStrategy.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
			    String msg = "{\"msgType\":\"STRATEGY_REQ\", \"payload\":\"Sample Payload\" }";
			    if(strategySocket != null) {
			    	strategySocket.send(msg);
			    }
				startFeed.setVisible(false);
				stopFeed.setVisible(false);
				startStrategy.setVisible(false);
				stopStrategy.setVisible(true);
				disconnect.setVisible(false);
				vPanelWS.clear();
			}
		});
		
		stopStrategy.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
			    String msg = "{\"msgType\":\"STRATEGY_STOP_REQ\", \"payload\":\"Sample Payload\" }";
			    if(strategySocket != null) {
			    	strategySocket.send(msg);
			    }
				startFeed.setVisible(true);
				stopFeed.setVisible(false);
				startStrategy.setVisible(true);
				stopStrategy.setVisible(false);
				disconnect.setVisible(true);
			}
		});
		
	}
	
	public Widget asWidget() {
		return this;
	}

	private Websocket getFeedWebSocket(final String connectionString) {
		Websocket socket = new Websocket("ws://" + connectionString + "/feed/ws");
		socket.addListener(new WebsocketListener() {
			
			@Override
			public void onOpen() {
				connectionLabel.setText("Successfully connected to " + connectionString);
				startFeed.setVisible(true);
				startStrategy.setVisible(true);
				connect.setVisible(false);
				disconnect.setVisible(true);
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

	private Websocket getStrategyWebSocket(final String connectionString) {
		Websocket socket = new Websocket("ws://" + connectionString + "/strategy/ws");
		socket.addListener(new WebsocketListener() {
			
			@Override
			public void onOpen() {
				connectionLabel.setText("Successfully connected to " + connectionString);
				startFeed.setVisible(true);
				startStrategy.setVisible(true);
				connect.setVisible(false);
				disconnect.setVisible(true);
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
