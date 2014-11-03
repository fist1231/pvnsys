package com.pvnsys.ttts.tttsGwtClient.client.controller.summary;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;
import com.pvnsys.ttts.tttsGwtClient.client.controller.common.Controller;
import com.pvnsys.ttts.tttsGwtClient.client.model.stocks.StockVOProxy;
import com.pvnsys.ttts.tttsGwtClient.client.service.stocks.StockRequestFactory;
import com.pvnsys.ttts.tttsGwtClient.client.view.login.UILogin;
import com.pvnsys.ttts.tttsGwtClient.shared.ConstantsUtils;

public class SummaryController implements Controller {  
  public interface Display {
    HasClickHandlers getLoginButton();
    HasValue<String> getName();
    HasValue<String> getStockDate();
    HasValue<String> getOpen();
    HasValue<String> getClose();
    HasValue<String> getHigh();
    HasValue<String> getLow();
    HasValue<String> getAdjClose();
    HasValue<String> getVolume();
    Widget asWidget();
  }
  
//  private final StockServiceAsync stockService;
  private StockRequestFactory stockFactory;
  
//  private final HandlerManager eventBus;
  private final EventBus eventBus;
  private final Display display;
  private String ticker;
  
  public SummaryController(StockRequestFactory stockFactory, EventBus eventBus, Display display) {
    this.stockFactory = stockFactory;
    this.eventBus = eventBus;
    this.display = display;
    bind();
  }
  
  public SummaryController(StockRequestFactory stockFactory, EventBus eventBus, Display display, String ticker) {
    this.stockFactory = stockFactory;
    this.eventBus = eventBus;
    this.display = display;
    this.ticker = ticker;
    bind();
    
    
  }
  
  public void bind() {
    this.display.getLoginButton().addClickHandler(new ClickHandler() {   
      public void onClick(ClickEvent event) {
        doLogin();
      }
    });

  }

  public void go(final HasWidgets container) {
    container.clear();
    RootPanel.get("loadingSummary").setVisible(false);
//    DOM.setInnerHTML(((RootPanel)container).getElement(), "");
//    DOM.setInnerHTML(container., "<b>Loading...</b>");

    
//    for(Iterator<Widget> it=container.iterator(); it.hasNext();) {
//    	it.remove();
//    }
    
    container.add(display.asWidget());
    getSummary(ticker);
  }

  private void doLogin() {
	  
  	UILogin login = UILogin.getInstance(stockFactory, eventBus);
  	if(!login.isShowing()) {
  		login.center();
  	}

//      Window.alert("TODO: Login will be here ...");
      
//	  UILogin login = new UILogin();   
//	  login.center();
//      eventBus.fireEvent(new SummaryEvent());

  }
  
  private void getSummary(String ticker) {

	  stockFactory.stockRequest().getLastStockQuote(ticker).fire(
			  new Receiver<StockVOProxy>() {

				@Override
				public void onSuccess(StockVOProxy stock) {
			          SummaryController.this.display.getName().setValue(stock.getName());
			          SummaryController.this.display.getStockDate().setValue(stock.getStockDate().toLocaleString());
			          SummaryController.this.display.getOpen().setValue(Double.toString(stock.getOpen()));
			          SummaryController.this.display.getClose().setValue(Double.toString(stock.getClose()));
			          SummaryController.this.display.getHigh().setValue(Double.toString(stock.getHigh()));
			          SummaryController.this.display.getLow().setValue(Double.toString(stock.getLow()));
			          SummaryController.this.display.getAdjClose().setValue(Double.toString(stock.getAdjClose()));
			          SummaryController.this.display.getVolume().setValue(Double.toString(stock.getVolume()));
				}

				@Override
				public void onFailure(ServerFailure error) {
					if(!ConstantsUtils.UNAUTHENTICATED_EXCEPTION_TYPE.equals(error.getExceptionType())) {
						Window.alert("All is lost :( " + error.getMessage());
					}
				}
			}
			  );
	  
//	    stockService.getLastStockQuote(ticker, new AsyncCallback<StockVO>() {
//	        public void onSuccess(StockVO result) {
//	          stockVO = result;
//	          SummaryController.this.display.getName().setValue(stockVO.getName());
//	          SummaryController.this.display.getStockDate().setValue(stockVO.getStockDate().toLocaleString());
//	          SummaryController.this.display.getOpen().setValue(Double.toString(stockVO.getOpen()));
//	          SummaryController.this.display.getClose().setValue(Double.toString(stockVO.getClose()));
//	          SummaryController.this.display.getHigh().setValue(Double.toString(stockVO.getHigh()));
//	          SummaryController.this.display.getLow().setValue(Double.toString(stockVO.getLow()));
//	          SummaryController.this.display.getAdjClose().setValue(Double.toString(stockVO.getAdjClose()));
//	          SummaryController.this.display.getVolume().setValue(Double.toString(stockVO.getVolume()));
//	        }
//	        
//	        public void onFailure(Throwable caught) {
//	          Window.alert("Error retrieving stock info");
//	        }
//	      });
	  
  }
  
}
