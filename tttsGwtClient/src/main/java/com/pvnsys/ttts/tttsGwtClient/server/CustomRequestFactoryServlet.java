package com.pvnsys.ttts.tttsGwtClient.server;

import com.google.web.bindery.requestfactory.server.DefaultExceptionHandler;
import com.google.web.bindery.requestfactory.server.ExceptionHandler;
import com.google.web.bindery.requestfactory.server.RequestFactoryServlet;
import com.google.web.bindery.requestfactory.server.ServiceLayerDecorator;

public class CustomRequestFactoryServlet extends RequestFactoryServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2613434519469606316L;

	public CustomRequestFactoryServlet() {
		this(new DefaultExceptionHandler(), new CustomServiceLayerDecorator());
	}

	public CustomRequestFactoryServlet(ExceptionHandler exceptionHandler, ServiceLayerDecorator... serviceDecorators) {
		super(exceptionHandler, serviceDecorators);
	}
}
