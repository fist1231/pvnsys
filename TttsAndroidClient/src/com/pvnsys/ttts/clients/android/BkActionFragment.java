package com.pvnsys.ttts.clients.android;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.pvnsys.ttts.clients.android.util.Utils;
import com.pvnsys.ttts.clients.android.vo.FeedVO;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;


public class BkActionFragment extends Fragment {
	
	public static final String TIMER_KEY = "TIMER_KEY";
	public static final String CONNECTION_STRING_KEY = "CONNECTION_STRING_KEY";
	public static final String FEED_ACTIVE_KEY = "FEED_ACTIVE_KEY";

//	WebSocketClient mWebSocketClient = null;

	private int count = 0;
	Chronometer chr;
	ImageButton startFeedButton;
	ImageButton stopFeedButton;
    private String connectionParameter;
    private boolean isFeedActive;

    private final WebSocketConnection mConnection = new WebSocketConnection();
	private static final String TAG = "WebSocket";

//	private ScrollView sw;
//	private LinearLayout quotes;
	
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		getActivity().getMenuInflater().inflate(R.menu.bk_activity, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
        case R.id.restart_feed:
            	((TttsAndroidClient)getActivity()).startApp();
            	return true;
        	default:
        		return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		Bundle bundle=getArguments();
		connectionParameter = bundle.getString(CONNECTION_STRING_KEY);
		
        Log.v("===========> BK Action Fragment", "Fragment State: onActivityCreated()");
        super.onCreateView(inflater, container, savedInstanceState);
		count = 0;

		View fragmentView = inflater.inflate(R.layout.bk_action_fragment, container, false);

		restoreInstanceState(fragmentView, savedInstanceState);
		
		TextView connectionParamDisplay = (TextView)fragmentView.findViewById(R.id.connection_display_param);
		connectionParamDisplay.setText(connectionParameter);

		
		startFeedButton = (ImageButton)fragmentView.findViewById(R.id.start_feed_button);
		stopFeedButton = (ImageButton)fragmentView.findViewById(R.id.stop_feed_button);
		setFeedActive(isFeedActive);
		
		setListeners(fragmentView);
		
//		if(sw == null) {
//			sw = (ScrollView)getActivity().findViewById(R.id.scrollView1);
//	        Log.v("~~~~~~~~~~~~> BK Action Fragment", "Creating ScrollView sw= " + sw);
//		}
//		if(quotes  == null) {
//			quotes = (LinearLayout)getActivity().findViewById(R.id.quotes);
//	        Log.v("~~~~~~~~~~~~> BK Action Fragment", "Creating LinearLayout quotes = " + quotes);
//		}		
		
		return fragmentView;
	}
	
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		mConnection.disconnect();
//		stopFeed(this.getView());
		
		if(outState != null) {
			 outState.putString(CONNECTION_STRING_KEY, connectionParameter);
			 outState.putBoolean(FEED_ACTIVE_KEY, isFeedActive);

			Chronometer chr = (Chronometer)getActivity().findViewById(R.id.chronometer1);
			if(chr != null && count > 0) {
				outState.putLong(TIMER_KEY, chr.getBase());
			}
		}
	}
	
	
	/*
	 * ========================= Private helper methods =============================
	 */

//	void startFeed(View view) {
//		setFeedActive(true);
////		startFeedButton.setVisibility(View.GONE);
////		stopFeedButton.setVisibility(View.VISIBLE);
//		chr = (Chronometer)getActivity().findViewById(R.id.chronometer1);
//		if(count == 0) {
//			chr.setBase(SystemClock.elapsedRealtime());
//			chr.start();
//		}
//		ScrollView sw = (ScrollView)getActivity().findViewById(R.id.scrollView1);
//		LinearLayout quotes = (LinearLayout)getActivity().findViewById(R.id.quotes);
//		mWebSocketClient = connectWebSocket(quotes, sw);
//	}
//
//	void stopFeed(View view) {
//		setFeedActive(false);
////		startFeedButton.setVisibility(View.VISIBLE);
////		stopFeedButton.setVisibility(View.GONE);
//		
//		chr = (Chronometer)getActivity().findViewById(R.id.chronometer1);
//		chr.setBase(SystemClock.elapsedRealtime());
//		chr.stop();
//		System.out.println("$$$$$$$$$$$ Sending stop message to WebSocket");
//		if(mWebSocketClient != null) {
//			mWebSocketClient.close();
//		}
//	}

//	private WebSocketClient connectWebSocket(final LinearLayout quotes, final ScrollView sw) {
//		  URI uri;
//		  try {
////		    uri = new URI("ws://192.168.1.4:6969/feed/ws");
//		    uri = new URI("ws://" + connectionParameter + "/feed/ws");
//		  } catch (URISyntaxException e) {
//		    e.printStackTrace();
//			String string = "Unable to obtain connection to Server. Please check your connection parameters";
//			Toast toastWin = Toast.makeText(getView().getContext(), string, Toast.LENGTH_SHORT);
//			toastWin.show();
//			return null;
//		  } catch(Exception e) {
//			    e.printStackTrace();
//				String string = "Bad Connection Parameter. Please verify your IP and Port";
//				Toast toastWin = Toast.makeText(getView().getContext(), string, Toast.LENGTH_SHORT);
//				toastWin.show();
//				return null;
//		  }
//
//		  try {
//			  mWebSocketClient = new WebSocketClient(uri) {
//				    @Override
//				    public void onOpen(ServerHandshake serverHandshake) {
//					  
//				      System.out.println("$$$$$$$$$$$ Sending message to WebSocket");
//					  String msg = "{ \"id\":\"ID-1\", \"msgType\":\"FEED_REQ\", \"client\":\"TBD_ON_SERVER\", \"payload\":\"Omg, Android, wtf, WTF ???\" }";
//					  try {  
//					  	mWebSocketClient.send(msg);
//					  } catch(Exception e) {
//						    e.printStackTrace();
//							String string = "Cannot send message to Server. Try again later.";
//							Toast toastWin = Toast.makeText(getView().getContext(), string, Toast.LENGTH_SHORT);
//							toastWin.show();
//					  }
//					  
//				      Log.i("Websocket", "Opened");
////				      System.out.println("$$$$$$$$$$$ Sending message to WebSocket");
////						String msg = "{ \"id\":\"ID-1\", \"msgType\":\"FEED_REQ\", \"client\":\"TBD_ON_SERVER\", \"payload\":\"Omg, Android, wtf, WTF ???\" }";
////				      
////				      mWebSocketClient.send(msg);
//				    }
//
//				    @Override
//				    public void onMessage(String s) {
//				      final String message = s;
//				      if(getActivity() != null) {
//					      getActivity().runOnUiThread(new Runnable() {
//						        @Override
//						        public void run() {
//						    		count++;
//						    		TextView tw = new TextView(getActivity());
//						    		tw.setGravity(Gravity.BOTTOM);
//						    		tw.setText(message);
//						    		tw.setTextColor(getResources().getColor(R.color.white));
//						    		quotes.addView(tw);
//						    		sw.post(new ScrollSW(sw,tw));
//						        }
//						      });
//				      } else {
////							String string = "Cannot continue. Try again later.";
////							Toast toastWin = Toast.makeText(getView().getContext(), string, Toast.LENGTH_SHORT);
////							toastWin.show();
//					    	putTextInScroll(quotes, sw, "Unexpected Error, try again later" );
//				      }
//				    }
//
//				    @Override
//				    public void onClose(int i, String s, boolean b) {
//				      Log.i("Websocket", "Closed " + s);
//				    }
//
//				    @Override
//				    public void onError(Exception e) {
////						String string = "Quotes Server error: [" + e.getMessage() + "] Try again later.";
////						Toast toastWin = Toast.makeText(getView().getContext(), string, Toast.LENGTH_SHORT);
////						toastWin.show();
//				    	putTextInScroll(quotes, sw, "Websocket Error: " + e.getMessage());
//				      Log.i("Websocket", "Error " + e.getMessage());
//				      e.printStackTrace();
//				    }
//				  };
//		  } catch(Exception e) {
//			    e.printStackTrace();
//				String string = "Error creating WebSocket";
//				Toast toastWin = Toast.makeText(getView().getContext(), string, Toast.LENGTH_SHORT);
//				toastWin.show();
//		  }
//		  try {
//			  mWebSocketClient.connect();
//		  } catch(Exception e) {
//			    e.printStackTrace();
//		    	putTextInScroll(quotes, sw, "Cnnot connect to Server: " + e.getMessage());
////				String string = "Cannot connect to Server. Try again later.";
////				Toast toastWin = Toast.makeText(getView().getContext(), string, Toast.LENGTH_SHORT);
////				toastWin.show();
//		  }
//		  
//		  return mWebSocketClient;
//		}	
	
	
	@SuppressWarnings("unchecked")
	private void restoreInstanceState(View fragmentView, Bundle savedInstanceState) {
		
		if(savedInstanceState != null) {
			
			if(savedInstanceState.containsKey(CONNECTION_STRING_KEY)) {
				connectionParameter = savedInstanceState.getString(CONNECTION_STRING_KEY);
			}
			if(savedInstanceState.containsKey(FEED_ACTIVE_KEY)) {
				isFeedActive = savedInstanceState.getBoolean(FEED_ACTIVE_KEY);
				setFeedActive(isFeedActive);
			}
			if(savedInstanceState.containsKey(TIMER_KEY)) {
				Chronometer chr = (Chronometer)fragmentView.findViewById(R.id.chronometer1);
				chr.setBase(savedInstanceState.getLong(TIMER_KEY));
				chr.start();
			}
			
			ScrollView sw = (ScrollView)fragmentView.findViewById(R.id.scrollView1);
			LinearLayout quotes = (LinearLayout)fragmentView.findViewById(R.id.quotes);
		    System.out.println("+++++++ restoreInstanceState ScrollView sw = " + sw);
	        System.out.println("+++++++ restoreInstanceState LinearLayout quotes = " + quotes);
	        
			if(isFeedActive) {
				startFeed(fragmentView, sw, quotes);
			}

			
			
		}
	}
	
	private void setListeners(View fragmentView) {
		
		startFeedButton = (ImageButton)fragmentView.findViewById(R.id.start_feed_button);
		if(startFeedButton != null) {
			startFeedButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
				        switch(v.getId()) {
				           case R.id.start_feed_button:
				              startFeed(v, null, null);   
				              break;
				           default:
				        	   break;
				        }	
				}
			});
		}

		stopFeedButton = (ImageButton)fragmentView.findViewById(R.id.stop_feed_button);
		if(stopFeedButton != null) {
			stopFeedButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
				        switch(v.getId()) {
				           case R.id.stop_feed_button:
				              stopFeed(v);   
				              break;
				           default:
				        	   break;
				        }	
				}
			});
		}

		
	}
	
	class ScrollSW implements Runnable {

		ScrollView scroller;
		View child;
		ScrollSW(ScrollView scroller, View child) {
		    this.scroller=scroller; 
		    this.child=child;

		}
		public void run() {
		    scroller.scrollTo(0, child.getTop());
		}
	}


	public int getCount() {
		return count;
	}

	public boolean getFeedActive() {
		return isFeedActive;
	}

	public void setFeedActive(boolean isFeedActive) {
		this.isFeedActive = isFeedActive;
		if(!isFeedActive) {
			if(startFeedButton != null) {
				startFeedButton.setVisibility(View.VISIBLE);
			}
			if(stopFeedButton != null) {
				stopFeedButton.setVisibility(View.GONE);
			}
		} else {
			if(stopFeedButton != null) {
				stopFeedButton.setVisibility(View.VISIBLE);
			}
			if(startFeedButton != null) {
				startFeedButton.setVisibility(View.GONE);
			}
		}
	}

	
	
	   private void startFeed(View view, final ScrollView sw, final LinearLayout quotes) {

			setFeedActive(true);
			chr = (Chronometer)getActivity().findViewById(R.id.chronometer1);
			if(count == 0 && chr != null) {
				chr.setBase(SystemClock.elapsedRealtime());
				chr.start();
			}
//			if(sw == null) {
//				sw = (ScrollView)getActivity().findViewById(R.id.scrollView1);
//			}
//			if(quotes == null) {
//				quotes = (LinearLayout)getActivity().findViewById(R.id.quotes);
//			}
			System.out.println("$$$$$$$$$$$ ScrollView sw = " + sw);
	        System.out.println("$$$$$$$$$$$ LinearLayout quotes = " + quotes);

			
			final String wsuri = "ws://" + connectionParameter + "/feed/ws";

		      try {
		         mConnection.connect(wsuri, new WebSocketHandler() {

		            @Override
		            public void onOpen() {
		               Log.d(TAG, "Status: Connected to " + wsuri);
					      System.out.println("$$$$$$$$$$$ Sending message to WebSocket");
						  String msg = "{ \"msgType\":\"FEED_REQ\", \"payload\":null }";
//					  	  mWebSocketClient.send(msg);
					  	  mConnection.sendTextMessage(msg);
		            }

		            @Override
		            public void onTextMessage(String payload) {
					    System.out.println("$$$$$$$$$$$ Receiving message from WebSocket");
		                Log.d(TAG, "Got echo: " + payload);
		                if(getActivity() != null) {
				    		TextView tw = new TextView(getActivity());
				    		tw.setGravity(Gravity.BOTTOM);
				    		
							JSONObject json;
							try {
								json = new JSONObject(payload);
								FeedVO feedVO = new FeedVO(
										json.getString("id"), 
										json.getString("msgType"), 
										json.getString("client"), 
										json.getString("payload"), 
										json.getString("timestamp"), 
										json.getString("sequenceNum") 
								);
								
					    		tw.setText(Utils.rPad(feedVO.getSequenceNum(), 10) + Utils.rPad(feedVO.getPayload(), 10));
					    		
//					    		tw.setText(payload);
					    		tw.setTextColor(getResources().getColor(R.color.white));
					    		if(quotes == null) {
					    			LinearLayout quotes2 = (LinearLayout)getActivity().findViewById(R.id.quotes);
					    			quotes2.addView(tw);
					    		} else {
						    		quotes.addView(tw);
					    		}
					    		if(sw == null) {
					    			ScrollView sw2 = (ScrollView)getActivity().findViewById(R.id.scrollView1);
					    			sw2.post(new ScrollSW(sw2,tw));
					    		} else {
						    		sw.post(new ScrollSW(sw,tw));
					    		}
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
		                } else {
		                	putTextInScroll(quotes, sw, "??? Restoring connection ...");
		                }
		            }

		            @Override
		            public void onClose(int code, String reason) {
				       putTextInScroll(quotes, sw, "Connection to server closed.");
		               Log.d(TAG, "Connection lost.");
		            }
		         });
		      } catch (WebSocketException e) {

		    	putTextInScroll(quotes, sw, "Websocket Error: " + e.getMessage());
			    System.out.println("###### Error in WebSocket");
		         Log.d(TAG, e.toString());
		      }
		   }
	
		void stopFeed(View view) {
			setFeedActive(false);
			
			chr = (Chronometer)getActivity().findViewById(R.id.chronometer1);
			chr.setBase(SystemClock.elapsedRealtime());
			chr.stop();
			System.out.println("$$$$$$$$$$$ Sending stop message to WebSocket");
			if(mConnection != null) {
				mConnection.disconnect();
			}
		}
	   
		private void putTextInScroll(final LinearLayout quotes, final ScrollView sw, String text) {
			if(getActivity() != null) {
				TextView tw = new TextView(getActivity());
				tw.setGravity(Gravity.BOTTOM);
				tw.setText(text);
				tw.setTextColor(Color.YELLOW);
				tw.setTextColor(getResources().getColor(R.color.white));

	    		if(quotes == null) {
	    			LinearLayout quotes2 = (LinearLayout)getActivity().findViewById(R.id.quotes);
	    			quotes2.addView(tw);
	    		} else {
		    		quotes.addView(tw);
	    		}
	    		if(sw == null) {
	    			ScrollView sw2 = (ScrollView)getActivity().findViewById(R.id.scrollView1);
	    			sw2.post(new ScrollSW(sw2,tw));
	    		} else {
		    		sw.post(new ScrollSW(sw,tw));
	    		}
				
//				quotes.addView(tw);
//				sw.post(new ScrollSW(sw,tw));
			}
		}

}
