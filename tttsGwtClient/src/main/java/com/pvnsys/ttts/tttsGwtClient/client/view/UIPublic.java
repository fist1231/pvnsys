package com.pvnsys.ttts.tttsGwtClient.client.view;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.pvnsys.ttts.tttsGwtClient.client.AppController;
import com.pvnsys.ttts.tttsGwtClient.client.event.authorization.AuthRequestTransport;
import com.pvnsys.ttts.tttsGwtClient.client.event.authorization.EventSourceRequestTransport;
import com.pvnsys.ttts.tttsGwtClient.client.event.authorization.LoginOnAuthRequired;
import com.pvnsys.ttts.tttsGwtClient.client.service.stocks.StockRequestFactory;

public class UIPublic extends Composite implements HasText {
	
    private static final Logger LOGGER = Logger.getLogger(UIPublic.class.getName());

	private static UIPublicUiBinder uiBinder = GWT
			.create(UIPublicUiBinder.class);

	interface UIPublicUiBinder extends UiBinder<Widget, UIPublic> {
	}

	@UiField
	Hyperlink loginLink;

	@UiHandler("loginLink")
	void onClick(ClickEvent e) {
		login();
	}

//	public UIPublic() {
//		initWidget(uiBinder.createAndBindUi(this));
//	}

	public UIPublic() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	public void setText(String text) {
	}

	public String getText() {
		return "";
	}
	
	private void login() {
//    	UILogin login = UILogin.getInstance(stockFactory, eventBus);
//    	if(!login.isShowing()) {
//    		login.center();
//    	}
		
	    EventBus eventBus = new SimpleEventBus();
	    
        GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
            public void onUncaughtException(Throwable e) {
            	if(e != null) {
	                Window.alert("Here is the Error in Gwt_a: " + e.getMessage());
	                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            	} else {
	                Window.alert("Caught nill exception in Gwt_a ");
            	}
            }
        });
	    
	    StockRequestFactory stockFactory = GWT.create(StockRequestFactory.class);
        new LoginOnAuthRequired().register(stockFactory, eventBus);
	    
	    
	    
        AuthRequestTransport authReqTransport = new AuthRequestTransport(eventBus);
        EventSourceRequestTransport transport = new EventSourceRequestTransport(eventBus, authReqTransport);
        stockFactory.initialize(eventBus, transport);
		
	    final AppController appViewer = new AppController(stockFactory, eventBus);
//	    DOM.setInnerHTML(RootPanel.get("summaryContainer").getElement(), "<b>Loading...</b>");
//	    appViewer.goSummary(RootPanel.get("summaryContainer"));
//	    Scheduler.get().scheduleDeferred(new Command() {
//	        public void execute () {
//	            GWT.runAsync(new RunAsyncCallback() {
//	                public void onFailure(Throwable caught) {
//	                }
//	            
//	                public void onSuccess() {
//	            	    appViewer.goSummary(RootPanel.get("summaryContainer"));
//	                }
//	              });
//	        }
//	      });

	    Scheduler.get().scheduleDeferred(new Command() {
	        public void execute () {
		        GWT.runAsync(new RunAsyncCallback() {
		            public void onFailure(Throwable caught) {
		                Window.alert("Failed to run appViewer.go: " + caught.getMessage());
		            }
		        
		            public void onSuccess() {
		        	    appViewer.go(RootPanel.get("nameFieldContainer"));
		            }
		          });
	        }
	      });


	    Scheduler.get().scheduleDeferred(new Command() {
	        public void execute () {
		        GWT.runAsync(new RunAsyncCallback() {
		            public void onFailure(Throwable caught) {
		                Window.alert("Failed to run goSummary: " + caught.getMessage());
		            }
		        
		            public void onSuccess() {
		            	appViewer.setSummaryContainer(RootPanel.get("summaryContainer"));
		            	appViewer.goSummary();
		            }
		          });
	        }
	      });

	    
	}

}
