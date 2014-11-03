package com.pvnsys.ttts.tttsGwtClient.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.pvnsys.ttts.tttsGwtClient.client.event.authorization.EventSourceRequestTransport;
import com.pvnsys.ttts.tttsGwtClient.client.event.authorization.LoginOnAuthRequired;
import com.pvnsys.ttts.tttsGwtClient.client.event.authorization.secure.SecureRequestTransport;
import com.pvnsys.ttts.tttsGwtClient.client.event.authorization.unsecure.UnsecureRequestTransport;
import com.pvnsys.ttts.tttsGwtClient.client.service.stocks.StockRequestFactory;
import com.pvnsys.ttts.tttsGwtClient.client.view.UIPublic;
import com.pvnsys.ttts.tttsGwtClient.client.view.complex.UIComplex;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class TttsGwtClient implements EntryPoint {

    private static final Logger LOGGER = Logger.getLogger(TttsGwtClient.class.getName());
    
    private EventBus eventBus;
    private StockRequestFactory stockFactory;
    private AppController appViewer;

	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";

	/**
	 * Create a remote service proxy to talk to the server-side Greeting service.
	 */

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {

		RootPanel.get("loadingSummary").setVisible(false);
        RootPanel.get("welcomeContainer").setVisible(true);

        GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
            public void onUncaughtException(Throwable e) {
            	if(e != null) {
//	                Window.alert("Here is the Error in Gwt_a: " + e.getMessage());
	                System.out.println("===========> e:" + e.toString());
	                System.out.println("===========> e:" + e.getMessage());
	                e.printStackTrace();
	                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            	} else {
	                Window.alert("Caught nill exception in Gwt_a ");
            	}
            }
        });
	    
        
	    eventBus = new SimpleEventBus();
	    stockFactory = GWT.create(StockRequestFactory.class);
	    appViewer = new AppController();
	    appViewer.setEventBus(eventBus);
	    appViewer.bind();
	    appViewer.setStocksContainer(RootPanel.get("stocksContainer"));
	    appViewer.setSummaryContainer(RootPanel.get("summaryContainer"));
//	    appViewer.setChartsContainer(RootPanel.get("chartsContainer"));

		
		AuthenticationServiceAsync authService = GWT.create(AuthenticationService.class);
		authService.isLoggedIn(new AsyncCallback<Boolean>() {
			public void onFailure(Throwable e) {
                Window.alert("Here is the Error in Gwt_a isLoggedIn() check: " + e.getMessage());
			}

			public void onSuccess(Boolean result) {
				if(result) {
			        RootPanel.get("loadingSummary").setVisible(true);
					goSecure();
				} else {
			        RootPanel.get("loadingSummary").setVisible(true);
					goUnsecure();
//					goComplex();
				}
			}
		});
		
//		goUnsecure();
//	    goSecure();
//	    goPublic();
	    
	}
	
	private void goUnsecure() {
		
	    Scheduler.get().scheduleDeferred(new Command() {
	        public void execute () {
	            GWT.runAsync(new RunAsyncCallback() {
	                public void onFailure(Throwable e) {
		                Window.alert("Here is the Error in Gwt_a goUnsecure() call: " + e.getMessage());
	                }
	            
	                public void onSuccess() {
//	                    new LoginOnAuthRequired().register(stockFactory, eventBus);
	                    UnsecureRequestTransport unsecureReqTransport = new UnsecureRequestTransport(eventBus);
	                    EventSourceRequestTransport transport = new EventSourceRequestTransport(eventBus, unsecureReqTransport);
	                    stockFactory.initialize(eventBus, transport);
	            	    appViewer.setStockFactory(stockFactory);
	            	    appViewer.goSummary(false);
//		        	    appViewer.go(RootPanel.get("nameFieldContainer"));
	                }
	              });
	        }
	      });
	}
	
	
	private void goSecure() {

		
//	    DOM.setInnerHTML(RootPanel.get("loadingStocks").getElement(), "<b>Loading...</b>");
//	    DOM.setInnerHTML(RootPanel.get("loadingCharts").getElement(), "<b>Loading...</b>");
//        RootPanel.get("loadingStocks").setVisible(false);
//        RootPanel.get("loadingCharts").setVisible(false);
		
 	    Scheduler.get().scheduleDeferred(new Command() {
	        public void execute () {
	            GWT.runAsync(new RunAsyncCallback() {
	                public void onFailure(Throwable caught) {
	                }
	            
	                public void onSuccess() {
	                    new LoginOnAuthRequired().register(stockFactory, eventBus);
	                    SecureRequestTransport authReqTransport = new SecureRequestTransport(eventBus);
	                    EventSourceRequestTransport transport = new EventSourceRequestTransport(eventBus, authReqTransport);
	                    stockFactory.initialize(eventBus, transport);
	                    appViewer.setStockFactory(stockFactory);
	            	    appViewer.goSummary(true);
		        	    appViewer.go();
	                }
	              });
	        }
	      });
	}
	
	private void goPublic() {
	    UIPublic uiPublic = GWT.create(UIPublic.class);
	    RootPanel.get("nameFieldContainer").add(uiPublic);
		
	}
	
	private void goComplex() {
	    UIComplex uiComplex = GWT.create(UIComplex.class);
	    RootPanel.get("complexContainer").add(uiComplex);
		
	}
	
	
}
