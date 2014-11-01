package com.pvnsys.ttts.clients.android;

import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import android.app.Fragment;
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
import android.widget.Toast;


public class BkActionFragment extends Fragment {
	
	public static final String TIMER_KEY = "TIMER_KEY";
	public static final String CONNECTION_STRING_KEY = "CONNECTION_STRING_KEY";

	WebSocketClient mWebSocketClient = null;

	private int count = 0;
	Chronometer chr;
	ImageButton startFeedButton;
	ImageButton stopFeedButton;
    private String connectionParameter;
	
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
		setListeners(fragmentView);
		
		startFeedButton = (ImageButton)fragmentView.findViewById(R.id.start_feed_button);
		stopFeedButton = (ImageButton)fragmentView.findViewById(R.id.stop_feed_button);
		startFeedButton.setVisibility(View.VISIBLE);
		stopFeedButton.setVisibility(View.GONE);

		
		return fragmentView;
	}
	
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		if(outState != null) {
			Chronometer chr = (Chronometer)getActivity().findViewById(R.id.chronometer1);
			if(chr != null && count > 0) {
				outState.putLong(TIMER_KEY, chr.getBase());
			}
		}
	}
	
	
	/*
	 * ========================= Private helper methods =============================
	 */

	void startFeed(View view) {
		startFeedButton.setVisibility(View.GONE);
		stopFeedButton.setVisibility(View.VISIBLE);
		chr = (Chronometer)getActivity().findViewById(R.id.chronometer1);
		if(count == 0) {
			chr.setBase(SystemClock.elapsedRealtime());
			chr.start();
		}
		ScrollView sw = (ScrollView)getActivity().findViewById(R.id.scrollView1);
		LinearLayout quotes = (LinearLayout)getActivity().findViewById(R.id.quotes);
		mWebSocketClient = connectWebSocket(quotes, sw);
	}

	void stopFeed(View view) {
		startFeedButton.setVisibility(View.VISIBLE);
		stopFeedButton.setVisibility(View.GONE);
		
		chr = (Chronometer)getActivity().findViewById(R.id.chronometer1);
		chr.setBase(SystemClock.elapsedRealtime());
		chr.stop();
		System.out.println("$$$$$$$$$$$ Sending stop message to WebSocket");
		mWebSocketClient.close();
	}

	private WebSocketClient connectWebSocket(final LinearLayout guesses, final ScrollView sw) {
		  URI uri;
		  try {
//		    uri = new URI("ws://192.168.1.4:6969/feed/ws");
		    uri = new URI("ws://" + connectionParameter + "/feed/ws");
		  } catch (URISyntaxException e) {
		    e.printStackTrace();
			String string = "Unable to obtain connection to Server. Please check your connection parameters";
			Toast toastWin = Toast.makeText(getView().getContext(), string, Toast.LENGTH_SHORT);
			toastWin.show();
			return null;
		  } catch(Exception e) {
			    e.printStackTrace();
				String string = "Bad Connection Parameter. Please verify your IP and Port";
				Toast toastWin = Toast.makeText(getView().getContext(), string, Toast.LENGTH_SHORT);
				toastWin.show();
				return null;
		  }

		  mWebSocketClient = new WebSocketClient(uri) {
		    @Override
		    public void onOpen(ServerHandshake serverHandshake) {
			  
		      System.out.println("$$$$$$$$$$$ Sending message to WebSocket");
			  String msg = "{ \"id\":\"ID-1\", \"msgType\":\"FEED_REQ\", \"client\":\"TBD_ON_SERVER\", \"payload\":\"Omg, Android, wtf, WTF ???\" }";
			  try {  
			  	mWebSocketClient.send(msg);
			  } catch(Exception e) {
				    e.printStackTrace();
					String string = "Cannot send message to Server. Try again later.";
					Toast toastWin = Toast.makeText(getView().getContext(), string, Toast.LENGTH_SHORT);
					toastWin.show();
			  }
			  
		      Log.i("Websocket", "Opened");
//		      System.out.println("$$$$$$$$$$$ Sending message to WebSocket");
//				String msg = "{ \"id\":\"ID-1\", \"msgType\":\"FEED_REQ\", \"client\":\"TBD_ON_SERVER\", \"payload\":\"Omg, Android, wtf, WTF ???\" }";
//		      
//		      mWebSocketClient.send(msg);
		    }

		    @Override
		    public void onMessage(String s) {
		      final String message = s;
		      getActivity().runOnUiThread(new Runnable() {
		        @Override
		        public void run() {
		    		TextView tw = null;
		    		count++;
		    		String text = " "; 
		    		tw = new TextView(getActivity());
		    		tw.setGravity(Gravity.BOTTOM);
		    		tw.setText(message);
		    		tw.setTextColor(getResources().getColor(R.color.white));
		    		guesses.addView(tw);
		    		sw.post(new ScrollSW(sw,tw));
		        }
		      });
		    }

		    @Override
		    public void onClose(int i, String s, boolean b) {
		      Log.i("Websocket", "Closed " + s);
		    }

		    @Override
		    public void onError(Exception e) {
		      Log.i("Websocket", "Error " + e.getMessage());
		    }
		  };
		  try {
			  mWebSocketClient.connect();
		  } catch(Exception e) {
			    e.printStackTrace();
				String string = "Cannot connect to Server. Try again later.";
				Toast toastWin = Toast.makeText(getView().getContext(), string, Toast.LENGTH_SHORT);
				toastWin.show();
		  }
		  
		  return mWebSocketClient;
		}	
	
	@SuppressWarnings("unchecked")
	private void restoreInstanceState(View fragmentView, Bundle savedInstanceState) {
		
		if(savedInstanceState != null) {
			
			if(savedInstanceState.containsKey(TIMER_KEY)) {
				Chronometer chr = (Chronometer)fragmentView.findViewById(R.id.chronometer1);
				chr.setBase(savedInstanceState.getLong(TIMER_KEY));
				chr.start();
			}

		}
	}
	
	private void setListeners(View fragmentView) {
		fragmentView.findViewById(R.id.start_feed_button).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			        switch(v.getId()) {
			           case R.id.start_feed_button:
			              startFeed(v);   
			              break;
			           default:
			        	   break;
			        }	
			}
		});

		fragmentView.findViewById(R.id.stop_feed_button).setOnClickListener(new OnClickListener() {
			
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

}
