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
import com.pvnsys.ttts.clients.android.vo.StrategyVO;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;


public class StrategyFragment extends Fragment {
	
	public static final String TIMER_KEY = "TIMER_KEY";
	public static final String CONNECTION_STRING_KEY = "CONNECTION_STRING_KEY";
	public static final String FEED_ACTIVE_KEY = "FEED_ACTIVE_KEY";

//	WebSocketClient mWebSocketClient = null;

	private int count = 0;
	Chronometer chr;
	ImageButton startStrategyButton;
	ImageButton stopStrategyButton;
    private String connectionParameter;
    private boolean isFeedActive;

    private final WebSocketConnection mConnection = new WebSocketConnection();
	private static final String TAG = "WebSocket";

//	private ScrollView sw;
//	private LinearLayout quotes;
	
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		getActivity().getMenuInflater().inflate(R.menu.strategy_activity, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
        case R.id.strategy_restart_feed:
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
		
        Log.v("===========> Strategy Fragment", "Fragment State: onActivityCreated()");
        super.onCreateView(inflater, container, savedInstanceState);
		count = 0;

		View fragmentView = inflater.inflate(R.layout.strategy_fragment, container, false);

		restoreInstanceState(fragmentView, savedInstanceState);
		
		TextView connectionParamDisplay = (TextView)fragmentView.findViewById(R.id.strategy_connection_display_param);
		connectionParamDisplay.setText(connectionParameter);

		
		startStrategyButton = (ImageButton)fragmentView.findViewById(R.id.start_strategy_button);
		stopStrategyButton = (ImageButton)fragmentView.findViewById(R.id.stop_strategy_button);
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

			Chronometer chr = (Chronometer)getActivity().findViewById(R.id.strategy_chronometer1);
			if(chr != null && count > 0) {
				outState.putLong(TIMER_KEY, chr.getBase());
			}
		}
	}
	
	
	/*
	 * ========================= Private helper methods =============================
	 */

	
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
				Chronometer chr = (Chronometer)fragmentView.findViewById(R.id.strategy_chronometer1);
				chr.setBase(savedInstanceState.getLong(TIMER_KEY));
				chr.start();
			}
			
			ScrollView sw = (ScrollView)fragmentView.findViewById(R.id.strategy_scrollView1);
			LinearLayout quotes = (LinearLayout)fragmentView.findViewById(R.id.strategy_quotes);
		    System.out.println("+++++++ restoreInstanceState ScrollView sw = " + sw);
	        System.out.println("+++++++ restoreInstanceState LinearLayout quotes = " + quotes);
	        
			if(isFeedActive) {
				startFeed(fragmentView, sw, quotes);
			}

			
			
		}
	}
	
	private void setListeners(View fragmentView) {
		
		startStrategyButton = (ImageButton)fragmentView.findViewById(R.id.start_strategy_button);
		if(startStrategyButton != null) {
			startStrategyButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
				        switch(v.getId()) {
				           case R.id.start_strategy_button:
				              startFeed(v, null, null);   
				              break;
				           default:
				        	   break;
				        }	
				}
			});
		}

		stopStrategyButton = (ImageButton)fragmentView.findViewById(R.id.stop_strategy_button);
		if(stopStrategyButton != null) {
			stopStrategyButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
				        switch(v.getId()) {
				           case R.id.stop_strategy_button:
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
			if(startStrategyButton != null) {
				startStrategyButton.setVisibility(View.VISIBLE);
			}
			if(stopStrategyButton != null) {
				stopStrategyButton.setVisibility(View.GONE);
			}
		} else {
			if(stopStrategyButton != null) {
				stopStrategyButton.setVisibility(View.VISIBLE);
			}
			if(startStrategyButton != null) {
				startStrategyButton.setVisibility(View.GONE);
			}
		}
	}

	
	
	   private void startFeed(View view, final ScrollView sw, final LinearLayout quotes) {

			setFeedActive(true);
			chr = (Chronometer)getActivity().findViewById(R.id.strategy_chronometer1);
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
			System.out.println("$$$$$$$$$$$ Strategy ScrollView sw = " + sw);
	        System.out.println("$$$$$$$$$$$ LinearLayout quotes = " + quotes);

			
			final String wsuri = "ws://" + connectionParameter + "/strategy/ws";

		      try {
		         mConnection.connect(wsuri, new WebSocketHandler() {

		            @Override
		            public void onOpen() {
		               Log.d(TAG, "Status: Connected to " + wsuri);
					      System.out.println("$$$$$$$$$$$ Sending message to Strategy WebSocket");
						  String msg = "{ \"msgType\":\"STRATEGY_REQ\", \"payload\":null }";
//					  	  mWebSocketClient.send(msg);
					  	  mConnection.sendTextMessage(msg);
		            }

		            @Override
		            public void onTextMessage(String payload) {
					    System.out.println("$$$$$$$$$$$ Receiving message from Strategy WebSocket");
		                Log.d(TAG, "Got echo: " + payload);
		                if(getActivity() != null) {
				    		TextView tw = new TextView(getActivity());
				    		tw.setGravity(Gravity.BOTTOM);
				    		try {
								JSONObject json = new JSONObject(payload);
								StrategyVO strategyVO = new StrategyVO(
										json.getString("id"), 
										json.getString("msgType"), 
										json.getString("client"), 
										json.getString("payload"), 
										json.getString("timestamp"), 
										json.getString("sequenceNum"), 
										json.getString("signal")
								);
								
					    		tw.setText(Utils.lPad(strategyVO.getSequenceNum(), 10) + Utils.lPad(strategyVO.getPayload(), 10) + Utils.lPad(strategyVO.getSignal(), 10));
					    		tw.setTextColor(getResources().getColor(R.color.white));
					    		if(quotes == null) {
					    			LinearLayout quotes2 = (LinearLayout)getActivity().findViewById(R.id.strategy_quotes);
					    			quotes2.addView(tw);
					    		} else {
						    		quotes.addView(tw);
					    		}
					    		if(sw == null) {
					    			ScrollView sw2 = (ScrollView)getActivity().findViewById(R.id.strategy_scrollView1);
					    			sw2.post(new ScrollSW(sw2,tw));
					    		} else {
						    		sw.post(new ScrollSW(sw,tw));
					    		}
								
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								
				                Log.d(TAG, "String is not  valid json: " + payload);
								e.printStackTrace();
							}
		                } else {
		                	putTextInScroll(quotes, sw, "??? Restoring Strategy connection ...");
		                }
		            }

		            @Override
		            public void onClose(int code, String reason) {
				       putTextInScroll(quotes, sw, "Connection to Strategy server closed.");
		               Log.d(TAG, "Connection lost.");
		            }
		         });
		      } catch (WebSocketException e) {

		    	putTextInScroll(quotes, sw, "Strategy Websocket Error: " + e.getMessage());
			    System.out.println("###### Error in Strategy WebSocket");
		         Log.d(TAG, e.toString());
		      }
		   }
	
		void stopFeed(View view) {
			setFeedActive(false);
			
			chr = (Chronometer)getActivity().findViewById(R.id.strategy_chronometer1);
			chr.setBase(SystemClock.elapsedRealtime());
			chr.stop();
			System.out.println("$$$$$$$$$$$ Sending stop strategy message to WebSocket");
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
	    			LinearLayout quotes2 = (LinearLayout)getActivity().findViewById(R.id.strategy_quotes);
	    			quotes2.addView(tw);
	    		} else {
		    		quotes.addView(tw);
	    		}
	    		if(sw == null) {
	    			ScrollView sw2 = (ScrollView)getActivity().findViewById(R.id.strategy_scrollView1);
	    			sw2.post(new ScrollSW(sw2,tw));
	    		} else {
		    		sw.post(new ScrollSW(sw,tw));
	    		}
				
//				quotes.addView(tw);
//				sw.post(new ScrollSW(sw,tw));
			}
		}
		
}
