package com.pvnsys.ttts.tttsGwtClient.client;

import java.util.Comparator;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.pvnsys.ttts.tttsGwtClient.shared.StockVO;

public class UITttsGwtClient extends Composite implements HasText {

	private static UITttsGwtClientUiBinder uiBinder = GWT.create(UITttsGwtClientUiBinder.class);

//	private final StockServiceAsync stockService = GWT.create(StockService.class);

	interface UITttsGwtClientUiBinder extends UiBinder<Widget, UITttsGwtClient> {
	}

	public UITttsGwtClient() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiField
	Button button;
	@UiField
	CellTable<StockVO> cellTable;

	@UiField
	SimplePager pager = new SimplePager();

	@UiField
	TextBox search;
	
	public UITttsGwtClient(String firstName) {
		initWidget(uiBinder.createAndBindUi(this));
		button.setText(firstName);
		search.setText("ABX");
		search.setFocus(true);
	}
	

	@UiHandler("button")
	void onClick(ClickEvent e) {
		search.setEnabled(false);
		button.setEnabled(false);
		Window.alert("Searching for: " + search.getText());
		getStocks(search.getText());
	}
	
//	@UiHandler("search")
//	void onChange() {
//		
//	}

	public void setText(String text) {
		button.setText(text);
	}

	public String getText() {
		return button.getText();
	}
	
	private void getStocks(String searchText) {
		final DialogBox dialogBox = new DialogBox();
		dialogBox.setText("Remote Procedure Call");
		dialogBox.setAnimationEnabled(true);
		final Button closeButton = new Button("Close");
		// We can set the id of a widget by accessing its Element
		closeButton.getElement().setId("closeButton");
		final Label textToServerLabel = new Label();
		final HTML serverResponseLabel = new HTML();
		VerticalPanel dialogVPanel = new VerticalPanel();
		dialogVPanel.addStyleName("dialogVPanel");
		dialogVPanel.add(new HTML("<b>Sending name to the server:</b>"));
		dialogVPanel.add(textToServerLabel);
		dialogVPanel.add(new HTML("<br><b>Server replies:</b>"));
		dialogVPanel.add(serverResponseLabel);
		dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		dialogVPanel.add(closeButton);
		dialogBox.setWidget(dialogVPanel);

		// Add a handler to close the DialogBox
		closeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				dialogBox.hide();
			}
		});
/*		
		stockService.searchStocks(searchText.trim().toUpperCase(),
				new AsyncCallback<List<StockVO>>() {
					public void onFailure(Throwable caught) {
						// Show the RPC error message to the user
						dialogBox
								.setText("Remote Procedure Call - Failure");
						serverResponseLabel
								.addStyleName("serverResponseLabelError");
						serverResponseLabel.setHTML(caught.getMessage());
						dialogBox.center();
						closeButton.setFocus(true);
						search.setEnabled(true);
						button.setEnabled(true);

						
					}

					public void onSuccess(List<StockVO> result) {
						populateCellTable(result);
					}
				});
*/		
	}
	
	private void populateCellTable(List<StockVO> stocks) {
		
//	    CellTable<StockVO> table = new CellTable<StockVO>();
		
//		if(cellTable != null) {
//			cellTable.removeFromParent();
//		} else {
//			cellTable = new CellTable<StockVO>();
//		}

	    ListDataProvider<StockVO> dataProvider = new ListDataProvider<StockVO>();

	    // Connect the table to the data provider.
	    dataProvider.addDataDisplay(cellTable);

	    // Add the data to the data provider, which automatically pushes it to the
	    // widget.
	    List<StockVO> list = dataProvider.getList();
	    for (StockVO stock : stocks) {
	      list.add(stock);
	    }
		
		if(cellTable.getColumnCount() == 0) {
		    // Create id column.
		    TextColumn<StockVO> idColumn = new TextColumn<StockVO>() {
		      @Override
		      public String getValue(StockVO stock) {
		        return Long.toString(stock.getId());
		      }
		    };
	
		    // Make the name column sortable.
		    idColumn.setSortable(true);
	
		    // Create name column.
		    TextColumn<StockVO> nameColumn = new TextColumn<StockVO>() {
		      @Override
		      public String getValue(StockVO stock) {
		        return stock.getName();
		      }
		    };
	
		    // Make the name column sortable.
		    nameColumn.setSortable(true);
	
		    // Create open column.
		    TextColumn<StockVO> openColumn = new TextColumn<StockVO>() {
		      @Override
		      public String getValue(StockVO stock) {
		        return Double.toString(stock.getOpen());
		      }
		    };
	
		    // Make the open column sortable.
		    openColumn.setSortable(true);
	
		    // Create close column.
		    TextColumn<StockVO> closeColumn = new TextColumn<StockVO>() {
		      @Override
		      public String getValue(StockVO stock) {
		        return Double.toString(stock.getClose());
		      }
		    };
	
		    // Make the close column sortable.
		    closeColumn.setSortable(true);
	
		    // Create high column.
		    TextColumn<StockVO> highColumn = new TextColumn<StockVO>() {
		      @Override
		      public String getValue(StockVO stock) {
		        return Double.toString(stock.getHigh());
		      }
		    };
	
		    // Make the high column sortable.
		    highColumn.setSortable(true);
	
		    // Create low column.
		    TextColumn<StockVO> lowColumn = new TextColumn<StockVO>() {
		      @Override
		      public String getValue(StockVO stock) {
		        return Double.toString(stock.getLow());
		      }
		    };
	
		    // Make the open column sortable.
		    lowColumn.setSortable(true);
	
		    // Create volume column.
		    TextColumn<StockVO> volumeColumn = new TextColumn<StockVO>() {
		      @Override
		      public String getValue(StockVO stock) {
		        return Long.toString(stock.getVolume());
		      }
		    };
	
		    // Make the volume column sortable.
		    volumeColumn.setSortable(true);
		    
		    cellTable.addColumn(idColumn, "Id");
		    cellTable.addColumn(nameColumn, "Name");
		    cellTable.addColumn(openColumn, "Open");
		    cellTable.addColumn(closeColumn, "Close");
		    cellTable.addColumn(highColumn, "High");
		    cellTable.addColumn(lowColumn, "Low");
		    cellTable.addColumn(volumeColumn, "Volume");

		    // Add a ColumnSortEvent.ListHandler to connect sorting to the
		    // java.util.List.
		    ListHandler<StockVO> columnSortHandler = new ListHandler<StockVO>(list);
		    columnSortHandler.setComparator(nameColumn,
		            new Comparator<StockVO>() {
		              public int compare(StockVO o1, StockVO o2) {
		                if (o1 == o2) {
		                  return 0;
		                }

		                // Compare the name columns.
		                if (o1 != null) {
		                  return (o2 != null) ? o1.getName().compareToIgnoreCase(o2.getName()) : 1;
		                }
		                return -1;
		              }
		            });
		        cellTable.addColumnSortHandler(columnSortHandler);
		        // We know that the data is sorted alphabetically by default.
		        cellTable.getColumnSortList().push(nameColumn);
		}
		
	    // Create a data provider.

	        
        pager.setDisplay(cellTable);
		search.setEnabled(true);
		button.setEnabled(true);


	    
	}

}
