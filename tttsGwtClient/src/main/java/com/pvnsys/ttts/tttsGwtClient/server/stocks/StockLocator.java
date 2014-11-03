package com.pvnsys.ttts.tttsGwtClient.server.stocks;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.google.web.bindery.requestfactory.server.RequestFactoryServlet;
import com.google.web.bindery.requestfactory.shared.ServiceLocator;

public class StockLocator implements ServiceLocator {

	HttpServletRequest request = RequestFactoryServlet.getThreadLocalRequest();
	ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(request.getSession().getServletContext());

	@Override
	public Object getInstance(Class<?> clazz) {
		return context.getBean(clazz);
	}

}
