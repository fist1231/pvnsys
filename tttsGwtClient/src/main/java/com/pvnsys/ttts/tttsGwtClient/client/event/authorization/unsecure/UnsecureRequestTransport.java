package com.pvnsys.ttts.tttsGwtClient.client.event.authorization.unsecure;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.web.bindery.requestfactory.gwt.client.DefaultRequestTransport;
import com.google.web.bindery.requestfactory.shared.ServerFailure;
import com.pvnsys.ttts.tttsGwtClient.client.event.authorization.AuthRequiredEvent;
import com.pvnsys.ttts.tttsGwtClient.shared.ConstantsUtils;

public class UnsecureRequestTransport extends DefaultRequestTransport {
    private final EventBus eventBus;

    public UnsecureRequestTransport(EventBus eventBus) {
        this.eventBus = eventBus;
	    String requestURL = GWT.getHostPageBaseURL() + "unsecureRequest"; 
	    setRequestUrl(requestURL); 
    }

    @Override
    protected RequestCallback createRequestCallback(final TransportReceiver receiver) {
        final RequestCallback superCallback = super.createRequestCallback(receiver);

        return new AuthCallbackWrapper(superCallback, receiver);
    }
    
    private final class AuthCallbackWrapper implements RequestCallback {
        private final RequestCallback delegate;
        private final TransportReceiver receiver;

        public AuthCallbackWrapper(RequestCallback delegate, TransportReceiver receiver) {
            this.delegate = delegate;
            this.receiver = receiver;
        }

        @Override
        public void onResponseReceived(Request request, Response response) {
            /**
             * The GwtEntryPoint responds with Response.SC_UNAUTHORIZED and adds a "login" url header if the
             * user is not logged in. When we receive that combo, post an event so that the app can handle things as
             * it sees fit.
             */
            if (Response.SC_UNAUTHORIZED == response.getStatusCode()) {
                String loginUrl = response.getHeader("login");
                if (loginUrl != null) {
                    boolean fatal = false;
                    ServerFailure failure = new ServerFailure("Unauthenticated user", ConstantsUtils.UNAUTHENTICATED_EXCEPTION_TYPE, null, fatal);
                    receiver.onTransportFailure(failure);
                    eventBus.fireEvent(new AuthRequiredEvent(loginUrl));
                    return;
                }
            }
            delegate.onResponseReceived(request, response);
        }

        @Override
        public void onError(Request request, Throwable exception) {
            delegate.onError(request, exception);
        }
    }
}
