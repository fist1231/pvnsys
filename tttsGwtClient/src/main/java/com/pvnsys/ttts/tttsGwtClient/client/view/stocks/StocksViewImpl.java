package com.pvnsys.ttts.tttsGwtClient.client.view.stocks;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONException;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlexTable;
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
	  private Websocket engineSocket;
	
	  private TextBox funds;
	  private TextBox balance;
	  private TextBox numberOfTrades;
	  private TextBox inTrade;
	  private TextBox positionSize;
	  private FlexTable statusTable;
	
	  
	private static StocksViewUiBinder uiBinder = GWT.create(StocksViewUiBinder.class);
	private MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();


	@SuppressWarnings("rawtypes")
	@UiTemplate("StocksView.ui.xml")
	interface StocksViewUiBinder extends UiBinder<Widget, StocksViewImpl> {
	}

	@UiField
	DecoratorPanel simulatorPanel;
	
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
	Button startEngine;

	@UiField
	Button stopEngine;
	
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
	
	  private void initDetailsTable() {
		  statusTable.setWidget(0, 0, new Label("Funds"));
		  statusTable.setWidget(0, 1, funds);
		  statusTable.setWidget(1, 0, new Label("Balance"));
		  statusTable.setWidget(1, 1, balance);
		  statusTable.setWidget(2, 0, new Label("Trades"));
		  statusTable.setWidget(2, 1, numberOfTrades);
		  statusTable.setWidget(3, 0, new Label("Active"));
		  statusTable.setWidget(3, 1, inTrade);
		  statusTable.setWidget(4, 0, new Label("Size"));
		  statusTable.setWidget(4, 1, positionSize);
	  }

	public StocksViewImpl(String firstName) {
		initWidget(uiBinder.createAndBindUi(this));
		

		simulatorPanel.setWidth("18em");
	    VerticalPanel contentDetailsPanel = new VerticalPanel();
	    contentDetailsPanel.setWidth("100%");

	    statusTable = new FlexTable();
	    statusTable.setCellSpacing(0);
	    statusTable.setWidth("100%");
	    statusTable.addStyleName("contacts-ListContainer");
	    statusTable.getColumnFormatter().addStyleName(1, "add-contact-input");
	    funds = new TextBox();
	    funds.setEnabled(false);
	    balance = new TextBox();
	    balance.setEnabled(false);
	    numberOfTrades = new TextBox();
	    numberOfTrades.setEnabled(false);
	    inTrade = new TextBox();
	    inTrade.setEnabled(false);
	    positionSize = new TextBox();
	    positionSize.setEnabled(false);
	    initDetailsTable();
	    contentDetailsPanel.add(statusTable);
	    simulatorPanel.add(contentDetailsPanel);
		
		
		sPanelWS.setAlwaysShowScrollBars(true);
		connect.setVisible(true);
		disconnect.setVisible(false);
		startFeed.setVisible(false);
		stopFeed.setVisible(false);
		startStrategy.setVisible(false);
		stopStrategy.setVisible(false);
		startEngine.setVisible(false);
		stopEngine.setVisible(false);
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

				engineSocket = getEngineWebSocket(connectionString.getText());
			    if(engineSocket != null) {
			    	if(connectionString.getText() != null && connectionString.getText().trim().length() > 0) {
			    		engineSocket.open();
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
			    if(engineSocket != null) {
			    	engineSocket.close();
			    }
				startFeed.setVisible(false);
				stopFeed.setVisible(false);
				startStrategy.setVisible(false);
				stopStrategy.setVisible(false);
				startEngine.setVisible(false);
				stopEngine.setVisible(false);
				connect.setVisible(true);
				disconnect.setVisible(false);
				connectionLabel.setText("Disconnected");
			}
		});
		
		startFeed.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
			    String msg = "{\"msgType\":\"FEED_REQ\", \"payload\":null }";
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
			    String msg = "{\"msgType\":\"FEED_STOP_REQ\", \"payload\":null }";
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
			    String msg = "{\"msgType\":\"STRATEGY_REQ\", \"payload\":null }";
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
			    String msg = "{\"msgType\":\"STRATEGY_STOP_REQ\", \"payload\":null }";
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

		startEngine.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
			    String msg = "{\"msgType\":\"ENGINE_REQ\", \"payload\":null }";
			    if(engineSocket != null) {
			    	engineSocket.send(msg);
			    }
				startFeed.setVisible(false);
				stopFeed.setVisible(false);
				startStrategy.setVisible(false);
				stopStrategy.setVisible(false);
				startEngine.setVisible(false);
				stopEngine.setVisible(true);
				disconnect.setVisible(false);
				vPanelWS.clear();
			}
		});
		
		stopEngine.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
			    String msg = "{\"msgType\":\"ENGINE_STOP_REQ\", \"payload\":null }";
			    if(engineSocket != null) {
			    	engineSocket.send(msg);
			    }
				startFeed.setVisible(true);
				stopFeed.setVisible(false);
				startStrategy.setVisible(true);
				stopStrategy.setVisible(false);
				startEngine.setVisible(true);
				stopEngine.setVisible(false);
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
				startEngine.setVisible(true);
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
				startEngine.setVisible(true);
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

	private Websocket getEngineWebSocket(final String connectionString) {
		Websocket socket = new Websocket("ws://" + connectionString + "/engine/ws");
		socket.addListener(new WebsocketListener() {
			
			@Override
			public void onOpen() {
				connectionLabel.setText("Successfully connected to " + connectionString);
				startFeed.setVisible(true);
				startStrategy.setVisible(true);
				startEngine.setVisible(true);
				connect.setVisible(false);
				disconnect.setVisible(true);
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onMessage(String msg) {
				vPanelWS.add(new Label(msg));
				sPanelWS.scrollToBottom();
				
				try {
					JSONValue value = JSONParser.parseStrict(msg);
					
					JSONObject msgObj = value.isObject();

					JSONValue pld = msgObj.get("payload");
					
					String sts = pld.toString();

					int startIdx = sts.indexOf("funds");
					String remained = sts.substring(startIdx);
					int endIdx = remained.indexOf(",");
					String fnd = remained.substring(8, endIdx);
					if(fnd != null && fnd.trim().length() > 0) {
						funds.setText(fnd);
					}

					int startIdx2 = sts.indexOf("balance");
					String remained2 = sts.substring(startIdx2);
					int endIdx2 = remained2.indexOf(",");
					String bal = remained2.substring(10, endIdx2);
					if(bal != null && bal.trim().length() > 0) {
						balance.setText(bal);
					}

					int startIdx3 = sts.indexOf("transnum");
					String remained3 = sts.substring(startIdx3);
					int endIdx3 = remained3.indexOf(":");
					String tn = remained3.substring(11, endIdx3);
					if(tn != null && tn.trim().length() > 0) {
						numberOfTrades.setText(tn);
					}
					
					
//					vPanelWS.add(new Label("__________________ before payload; pld.isString().toString() = " + pld.isString().toString()));
//					sPanelWS.scrollToBottom();
//					
//					JSONObject pldstr = pld.isObject();
//					
//					vPanelWS.add(new Label("__________________ before another isObj 2; pldstr.toString() = " + pldstr.toString()));
//					sPanelWS.scrollToBottom();
//
//					vPanelWS.add(new Label("__________________ before another isObj 2; pldstr.isString().toString() = " + pldstr.isString().toString()));
//					sPanelWS.scrollToBottom();
//					
//					JSONObject subVal = pldstr.isObject();
//					
//					vPanelWS.add(new Label("__________________ before funds"));
//					sPanelWS.scrollToBottom();
//
//					JSONValue fnd = subVal.get("\"funds\"");
//					vPanelWS.add(new Label("__________________ after funds"));
//					sPanelWS.scrollToBottom();
//					
//					numberOfTrades.setText(fnd.toString());
//					vPanelWS.add(new Label("__________________ after nOt"));
//					sPanelWS.scrollToBottom();
//					
//					String fnds = fnd.toString();
//					vPanelWS.add(new Label("__________________ after fnds.toString"));
//					sPanelWS.scrollToBottom();
//					
//					funds.setText(fnds);
//					vPanelWS.add(new Label("__________________ before balance"));
//					sPanelWS.scrollToBottom();
//					
//					JSONValue b = subVal.get("\"balance\"");
//					String bal = b.toString();
//					balance.setText(bal);
//
//					System.out.println("~~~~~~~~~~~~~~~~~~~~~ bal = " + bal);
//					// Just for Engine
//					vPanelWS.add(new Label(bal));
//					sPanelWS.scrollToBottom();


//					
//					int startIdx = payload.indexOf("funds");
//					String remained = payload.substring(startIdx);
//					int endIdx = remained.indexOf(";");
//					String fnd = payload.substring(startIdx, endIdx);
//					funds.setText(fnd);
//
//					int startIdx2 = payload.indexOf("balance");
//					String remained2 = payload.substring(startIdx2);
//					int endIdx2 = remained2.indexOf(";");
//					String bal = payload.substring(startIdx2, endIdx2);
//					balance.setText(bal);
//
//					int startIdx3 = payload.indexOf("transnum");
//					String remained3 = payload.substring(startIdx3);
//					int endIdx3 = remained3.indexOf(";");
//					String tn = payload.substring(startIdx3, endIdx3);
//					numberOfTrades.setText(tn);
//
//					int startIdx4 = payload.indexOf("inTrade");
//					String remained4 = payload.substring(startIdx4);
//					int endIdx4 = remained4.indexOf(";");
//					String intr = payload.substring(startIdx4, endIdx4);
//					inTrade.setText(intr);
//
//					int startIdx5 = payload.indexOf("possize");
//					String ps = payload.substring(startIdx5 + 8);
//					positionSize.setText(ps);
					
				} catch (JSONException e) {
//					vPanelWS.add(new Label("JSONException --------> " + e.getMessage()));
//					sPanelWS.scrollToBottom();

					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
//					vPanelWS.add(new Label("ERROR ==========> " + e.getMessage()));
//					sPanelWS.scrollToBottom();

					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			@Override
			public void onClose() {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		return socket;
		
	}
	
	
}
