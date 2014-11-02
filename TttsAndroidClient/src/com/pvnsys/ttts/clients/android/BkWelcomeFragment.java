package com.pvnsys.ttts.clients.android;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;


public class BkWelcomeFragment extends Fragment {
	
	private void showNameDialog() {
	    FragmentManager fragmentManager = getFragmentManager();
	    FragmentTransaction transaction = fragmentManager.beginTransaction();
	    ConnectionParametersDialogFragment nameDialod = new ConnectionParametersDialogFragment();
	    nameDialod.setTargetFragment(this, 0);
	    nameDialod.show(transaction, "work");
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        Log.v("===========> BK Welcome Fragment", "Fragment State: onActivityCreated()");
        super.onCreateView(inflater, container, savedInstanceState);

		// Inflate fragment
        View fragmentView = inflater.inflate(R.layout.bk_welcome_fragment, container, false);

		OnClickListener welcomeListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			        switch(v.getId()) {
			           case R.id.begin_button_button:
			        	   showNameDialog();
			              break;
			           case R.id.help_button_button:
			        	   startHelpFragment();   
			              break;
			           case R.id.exit_button_button:
			        	   exitApp();   
			              break;
			           default:
			        	   break;
			        }	
			}
		};
		
		fragmentView.findViewById(R.id.begin_button_button).setOnClickListener(welcomeListener);
		fragmentView.findViewById(R.id.help_button_button).setOnClickListener(welcomeListener);
		fragmentView.findViewById(R.id.exit_button_button).setOnClickListener(welcomeListener);

        return fragmentView;
	}

	private void startHelpFragment() {
		Toast toastWin = Toast.makeText(getActivity(), "Help is under construction", Toast.LENGTH_SHORT);
		toastWin.show();
	}

	private void exitApp() {
		getActivity().finish();
	}
	
}
