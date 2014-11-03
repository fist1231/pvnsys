package com.pvnsys.ttts.tttsGwtClient.client.view.complex.a1;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Widget;
import com.pvnsys.ttts.tttsGwtClient.client.service.stocks.StockRequestFactory;

public class UIAOneMax extends DialogBox {

	private static UIAOneMaxUiBinder uiBinder = GWT.create(UIAOneMaxUiBinder.class);
//	private StockRequestFactory stockFactory;
	private EventBus eventBus;
//	private static UIAOneMax uiAOneMax;

	interface UIAOneMaxUiBinder extends UiBinder<Widget, UIAOneMax> {
	}

	
//	public static UIAOneMax getInstance(StockRequestFactory stockFactory, EventBus eventBus) {
//		if(uiAOneMax == null) {
//			uiAOneMax = new UIAOneMax("tsa", stockFactory, eventBus);
//		}
//		return uiAOneMax;
//	}
	
	public UIAOneMax() {
		setWidget(uiBinder.createAndBindUi(this));
		setGlassEnabled(true);
		setModal(true);
		close.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				hideMe();
			}
		});

		max.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				hide();
				show();
				setSize(getParent().getOffsetWidth() + "px", getParent().getOffsetHeight() + "px");
//				setSize(getParent().getOffsetWidth() + "px", getParent().getOffsetHeight() + "px");
			}
		});

		min.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				hide();
				show();
				setHeight("5px");
//				setHeight("5px");
			}
		});
	}

	private void hideMe() {
		this.hide();
		
	}
	
	private UIAOneMax(String firstName, StockRequestFactory stockFactory, EventBus eventBus) {
		
//		this.stockFactory = stockFactory;
		this.eventBus = eventBus;
		setWidget(uiBinder.createAndBindUi(this));
		

	}
	
	@UiField
	Button min;

	@UiField
	Button max;

	@UiField
	Button close;


}
