package com.pvnsys.ttts.tttsGwtClient.server;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.google.web.bindery.requestfactory.server.ServiceLayerDecorator;
import com.google.web.bindery.requestfactory.shared.Locator;

public class CustomServiceLayerDecorator extends ServiceLayerDecorator {

	@Override
	public <T extends Locator<?, ?>> T createLocator(Class<T> clazz) {
		ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(CustomRequestFactoryServlet.getThreadLocalServletContext());
		return context.getBean(clazz);
	}
}
