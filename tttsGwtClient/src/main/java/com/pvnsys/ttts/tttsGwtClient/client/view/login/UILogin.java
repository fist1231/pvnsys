package com.pvnsys.ttts.tttsGwtClient.client.view.login;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.pvnsys.ttts.tttsGwtClient.client.AuthenticationService;
import com.pvnsys.ttts.tttsGwtClient.client.AuthenticationServiceAsync;
import com.pvnsys.ttts.tttsGwtClient.client.event.stocks.StockSearchEvent;
import com.pvnsys.ttts.tttsGwtClient.client.service.stocks.StockRequestFactory;

public class UILogin extends DialogBox {

	private static UILoginUiBinder uiBinder = GWT.create(UILoginUiBinder.class);
//	private StockRequestFactory stockFactory;
	private EventBus eventBus;
	private static UILogin uiLogin;

	interface UILoginUiBinder extends UiBinder<Widget, UILogin> {
	}

	
	public static UILogin getInstance(StockRequestFactory stockFactory, EventBus eventBus) {
		if(uiLogin == null) {
			uiLogin = new UILogin("tsa", stockFactory, eventBus);
		}
		return uiLogin;
	}
	
	private UILogin() {
		setWidget(uiBinder.createAndBindUi(this));
	}

	private void hideMe() {
		this.hide();
		
	}
	
	private UILogin(String firstName, StockRequestFactory stockFactory, EventBus eventBus) {
		
//		this.stockFactory = stockFactory;
		this.eventBus = eventBus;
		setWidget(uiBinder.createAndBindUi(this));
		
		usernameField.setText("pvn");
//		usernameField.setFocus(true);
		passwordField.getElement().setAttribute("type", "text");
		passwordField.setText("wtfwtfwtf");
		
		usernameField.setStyleName("login-label-text");
		passwordField.setStyleName("login-label-text");
		
		setGlassEnabled(true);
		cancelButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				hideMe();
			}
		});
		
		usernameField.addKeyDownHandler(new KeyDownHandler() {
			
			@Override
			public void onKeyDown(KeyDownEvent event) {
			    if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
			    	authenticate();
			    } else {
					if(usernameField.getText().equals("pvn")) {
						usernameField.setText("");
						usernameField.setStyleName("");

					}
			    }
			}
		});

		passwordField.addKeyDownHandler(new KeyDownHandler() {
			
			@Override
			public void onKeyDown(KeyDownEvent event) {
			    if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
			    	authenticate();
			    } else {
					if(passwordField.getText().equals("wtfwtfwtf")) {
						passwordField.getElement().setAttribute("type", "password");
						passwordField.setText("");
						passwordField.setStyleName("");
					}
			    }
			}
		});
		
//		usernameField.addFocusHandler(new FocusHandler() {
//			
//			@Override
//			public void onFocus(FocusEvent event) {
//				if(usernameField.getText().equals("username")) {
//					usernameField.setText("");
//				}
//				
//			}
//		});

//		passwordField.addFocusHandler(new FocusHandler() {
//			
//			@Override
//			public void onFocus(FocusEvent event) {
//				if(passwordField.getText().equals("password")) {
//					passwordField.getElement().setAttribute("type", "password");
//					passwordField.setText("");
//				}
//				
//			}
//		});
		
		usernameField.addBlurHandler(new BlurHandler() {
			
			@Override
			public void onBlur(BlurEvent event) {
				if(usernameField.getText().trim().length() == 0) {
					usernameField.setText("pvn");
				}
			}
		});
		passwordField.addBlurHandler(new BlurHandler() {
			
			@Override
			public void onBlur(BlurEvent event) {
				if(passwordField.getText().trim().length() == 0) {
					passwordField.getElement().setAttribute("type", "text");
					passwordField.setText("wtfwtfwtf");
					passwordField.setStyleName("login-label-text");
				}
			}
		});
		
		
		setFocus();
	}
	
	private void setFocus() {
	    Scheduler.get().scheduleDeferred(new Command() {
	        public void execute () {
	            GWT.runAsync(new RunAsyncCallback() {
	                public void onFailure(Throwable e) {
	                	// Do nothing
	                }
	            
	                public void onSuccess() {
	                	usernameField.setFocus(true);
	                }
	              });
	        }
	      });
		
	}
	
	@UiField
	Button loginButton;

	@UiField
	Button cancelButton;

	@UiField
	TextBox usernameField;
	
	@UiField
	PasswordTextBox passwordField;
	
	@UiField
	Label errorText;
	
	@UiHandler("passwordField")
	void onKeyDown(KeyDownEvent event) {
	    if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
	    	authenticate();
	    }		
	}

	@UiHandler("loginButton")
	void onClick(ClickEvent e) {
		authenticate();
	}

	private void authenticate() {
		enableAll(false);
		AuthenticationServiceAsync authenticationService = GWT.create(AuthenticationService.class);

		authenticationService.authenticate(usernameField.getText(), passwordField.getText(), new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
//				Window.alert("Failed to login properly: " + caught.getMessage());
				errorText.setText("Wrong Username or Password.");
				enableAll(true);
			}
			public void onSuccess(String result) {
//		        RootPanel.get("loadingStocks").setVisible(true);
//		        RootPanel.get("loadingCharts").setVisible(true);
				if(eventBus != null) {
				      eventBus.fireEvent(new StockSearchEvent("ABX"));
				}
				enableAll(true);
				hide();
				
			}
		});
	}
	
	private void enableAll(boolean isEnabled) {
		usernameField.setEnabled(isEnabled);
		passwordField.setEnabled(isEnabled);
		loginButton.setEnabled(isEnabled);
		cancelButton.setEnabled(isEnabled);
	}
	
//	private void goSuccess() {
//		this.hide();
//	}

}
