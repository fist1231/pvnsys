/**
 * 
 */
package com.pvnsys.ttts.tttsGwtClient.client.view.complex;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import com.pvnsys.ttts.tttsGwtClient.client.view.complex.a1.UIAOneMax;
import com.smartgwt.client.widgets.layout.PortalLayout;
import com.smartgwt.client.widgets.layout.Portlet;

/**
 * @author tsa
 *
 */
public class UIComplex extends Composite implements HasText {

	private static UIComplexUiBinder uiBinder = GWT
			.create(UIComplexUiBinder.class);

	interface UIComplexUiBinder extends UiBinder<Widget, UIComplex> {
	}

	/**
	 * Because this class has a default constructor, it can
	 * be used as a binder template. In other words, it can be used in other
	 * *.ui.xml files as follows:
	 * <ui:UiBinder xmlns:ui="urn:ui:com.google.gwt.uibinder"
	 *   xmlns:g="urn:import:**user's package**">
	 *  <g:**UserClassName**>Hello!</g:**UserClassName>
	 * </ui:UiBinder>
	 * Note that depending on the widget that is used, it may be necessary to
	 * implement HasHTML instead of HasText.
	 */
	public UIComplex() {
		initWidget(uiBinder.createAndBindUi(this));

		PortalLayout portalLayout = new PortalLayout();
        portalLayout.setWidth100();  
        portalLayout.setHeight100();  
          
        Portlet portlet1 = new Portlet();  
        portlet1.setTitle("Portlet 1");  
        Portlet portlet2 = new Portlet();  
        portlet2.setTitle("Portlet 2");  
        Portlet portlet3 = new Portlet();  
        portlet3.setTitle("Portlet 3");  
        Portlet portlet4 = new Portlet();  
        portlet4.setTitle("Portlet 4");  
          
        portalLayout.addPortlet(portlet1, 0, 0);  
        portalLayout.addPortlet(portlet2, 0, 1, 0);  
        portalLayout.addPortlet(portlet3, 0, 1, 1);  
        portalLayout.addPortlet(portlet4, 1, 0);  

		
		hPan.add(portalLayout);
		
		max.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				setSize(getParent().getOffsetWidth() + "px", getParent().getOffsetHeight() + "px");
			}
		});

		min.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				setHeight("5px");
			}
		});
	}

	@UiField HTMLPanel hPan;
	
	@UiField Button min;
	@UiField Button max;
	@UiField Button close;
	@UiField Grid bGrid;

	@UiField Button button;

	public UIComplex(String firstName) {
		initWidget(uiBinder.createAndBindUi(this));

		// Can access @UiField after calling createAndBindUi
		button.setText(firstName);
	}

	@UiHandler("max")
	void onClick(ClickEvent e) {
		UIAOneMax a1 = GWT.create(UIAOneMax.class);
		a1.setGlassEnabled(true);
		a1.setModal(true);
		a1.setSize(getParent().getOffsetWidth() + "px", getParent().getOffsetHeight() + "px");
		a1.show();
	}

	public void setText(String text) {
		button.setText(text);
	}

	/**
	 * Gets invoked when the default constructor is called
	 * and a string is provided in the ui.xml file.
	 */
	public String getText() {
		return button.getText();
	}

}
