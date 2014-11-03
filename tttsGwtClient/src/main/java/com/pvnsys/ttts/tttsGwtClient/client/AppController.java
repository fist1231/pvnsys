package com.pvnsys.ttts.tttsGwtClient.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.RootPanel;
import com.pvnsys.ttts.tttsGwtClient.client.controller.charts.ChartsController;
import com.pvnsys.ttts.tttsGwtClient.client.controller.common.Controller;
import com.pvnsys.ttts.tttsGwtClient.client.controller.stocks.StocksController;
import com.pvnsys.ttts.tttsGwtClient.client.controller.summary.SummaryController;
import com.pvnsys.ttts.tttsGwtClient.client.event.stocks.StockSearchEvent;
import com.pvnsys.ttts.tttsGwtClient.client.event.stocks.StockSearchEventHandler;
import com.pvnsys.ttts.tttsGwtClient.client.service.stocks.StockRequestFactory;
import com.pvnsys.ttts.tttsGwtClient.client.view.charts.ChartsViewImpl;
import com.pvnsys.ttts.tttsGwtClient.client.view.stocks.StocksViewImpl;
import com.pvnsys.ttts.tttsGwtClient.client.view.summary.SummaryView;
import com.pvnsys.ttts.tttsGwtClient.shared.StockVO;

public class AppController implements Controller, ValueChangeHandler<String> {
	// private final HandlerManager eventBus;
	private EventBus eventBus;
	// private final StockServiceAsync stockService;
	private HasWidgets stocksContainer;
	private StocksViewImpl<StockVO> stocksView = null;
	private StockRequestFactory stockFactory;

	private HasWidgets summaryContainer;
	private SummaryView summaryView = null;
	private boolean isLoggedIn;
	private String search = "ABX";

	private HasWidgets chartsContainer;

	public AppController() {
		// bind();
	}

	public AppController(StockRequestFactory stockFactory, EventBus eventBus) {
		this.eventBus = eventBus;
		this.stockFactory = stockFactory;
		bind();
	}

	public void bind() {
		History.addValueChangeHandler(this);

		// eventBus.addHandler(StocksEvent.TYPE,
		// new StocksEventHandler() {
		//
		// public void onSort(StocksEvent event) {
		// // TODO Auto-generated method stub
		//
		// }
		// });
		//
		eventBus.addHandler(StockSearchEvent.TYPE,
				new StockSearchEventHandler() {

					public void onSearch(StockSearchEvent event) {
						doSearch(event.getText());

					}
				});

	}

	private void doSearch(String text) {
		this.search = text;
		this.isLoggedIn = true;
		History.newItem("stocks", false);
		History.fireCurrentHistoryState();
		// Controller controller = new SummaryController(stockFactory, eventBus,
		// new SummaryView(isLoggedIn), text);
		// controller.go(summaryContainer);
		// if(stocksView == null) {
		// if(stocksView == null) {
		// stocksView = new StocksViewImpl<StockVO>("Find");
		// }
		// }
		// new StocksController(stockFactory, eventBus, stocksView,
		// text).go(stocksContainer);

	}

	// private void doStocks() {
	// History.newItem("stocks");
	// }

	public void go() {

		if ("".equals(History.getToken())) {
			History.newItem("stocks");
		} else {
			History.fireCurrentHistoryState();
		}
	}

	public void goSummary(boolean isLoggedIn) {
		this.isLoggedIn = isLoggedIn;
		if (!isLoggedIn) {
			if ("".equals(History.getToken())) {
				History.newItem("summaryPublic");
			} else {
				History.fireCurrentHistoryState();
			}
		} else {

		}
	}

	public void goSummary() {

		// GWT.runAsync(new RunAsyncCallback() {
		// public void onFailure(Throwable caught) {
		// }
		//
		// public void onSuccess() {
		// // lazily initialize our views, and keep them around to be reused
		// //
		// if(summaryView == null) {
		// summaryView = new SummaryView();
		// }
		// new SummaryController(stockFactory, eventBus, summaryView,
		// "ABX").go(summaryContainer);
		//
		// }
		// });

		// if ("".equals(History.getToken())) {
		// History.newItem("stocks");
		// }
		// else {
		// History.fireCurrentHistoryState();
		// }
	}

	public void onValueChange(ValueChangeEvent<String> event) {
		String token = event.getValue();

		if (token != null) {
			if (token.equals("stocks")) {

				GWT.runAsync(new RunAsyncCallback() {
					public void onFailure(Throwable caught) {
					}

					public void onSuccess() {
						// lazily initialize our views, and keep them around to
						// be reused
						//
						if (stocksView == null) {
							stocksView = new StocksViewImpl<StockVO>("Find");
						}
						new StocksController(stockFactory, eventBus,
								stocksView, search).go(stocksContainer);

//						if (summaryView == null) {
							summaryView = new SummaryView(isLoggedIn);
//						}
						new SummaryController(stockFactory, eventBus,
								summaryView, search).go(summaryContainer);

					}
				});
				
				// Init charts
			    Scheduler.get().scheduleDeferred(new Command() {
			        public void execute () {
				        GWT.runAsync(new RunAsyncCallback() {
				            public void onFailure(Throwable e) {
				                Window.alert("Here is the Error in StocksController initCharts() call: " + e.getMessage());
				            }
				        
				            public void onSuccess() {
				        		ChartsViewImpl<StockVO> chartsView = new ChartsViewImpl<StockVO>();
				        		new ChartsController(stockFactory, eventBus, chartsView, search).go(chartsContainer);
				            }
				          });
			        }
			      });
				
				
				
			} else if (token.equals("summaryPublic")) {

		        RootPanel.get("welcomeContainer").setVisible(true);
				GWT.runAsync(new RunAsyncCallback() {
					public void onFailure(Throwable caught) {
					}

					public void onSuccess() {
						// lazily initialize our views, and keep them around to
						// be reused
						//
						if (summaryView == null) {
							boolean isLoggedIn = false;
							summaryView = new SummaryView(isLoggedIn);
						}
						new SummaryController(stockFactory, eventBus,
								summaryView, search).go(summaryContainer);

					}
				});
			}
			// else if (token.equals("add") || token.equals("edit")) {
			// GWT.runAsync(new RunAsyncCallback() {
			// public void onFailure(Throwable caught) {
			// }
			//
			// public void onSuccess() {
			// // lazily initialize our views, and keep them around to be reused
			// //
			// if (editContactView == null) {
			// editContactView = new EditContactView();
			//
			// }
			// new EditContactPresenter(rpcService, eventBus, editContactView).
			// go(container);
			// }
			// });
			// }
		}
	}

	public void setEventBus(EventBus eventBus) {
		this.eventBus = eventBus;
	}

	public void setStockFactory(StockRequestFactory stockFactory) {
		this.stockFactory = stockFactory;
	}

	public void setStocksContainer(HasWidgets stocksContainer) {
		this.stocksContainer = stocksContainer;
	}

	public void setSummaryContainer(HasWidgets summaryContainer) {
		this.summaryContainer = summaryContainer;
	}

	@Override
	public void go(HasWidgets container) {
		// TODO Auto-generated method stub

	}

	public void setChartsContainer(HasWidgets chartsContainer) {
		this.chartsContainer = chartsContainer;
	}
}
