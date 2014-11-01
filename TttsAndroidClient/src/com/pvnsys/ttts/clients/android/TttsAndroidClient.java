package com.pvnsys.ttts.clients.android;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Chronometer;
import android.widget.EditText;

import com.pvnsys.ttts.clients.android.ConnectionParametersDialogFragment.ConnectionParametersDialogListener;


public class TttsAndroidClient extends FragmentActivity implements ConnectionParametersDialogListener {

	public String test = "aaaaa";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bk_activity);
		
		Fragment actionFragment = getFragmentManager().findFragmentById(R.id.fragment_container);
		if(actionFragment != null) {
			if(actionFragment instanceof BkActionFragment) {
				FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
				fragmentTransaction.show(actionFragment);
//				fragmentTransaction.add(R.id.fragment_container, actionFragment);
//				fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//				fragmentTransaction.addToBackStack(null);
				fragmentTransaction.commit();
			}
			if(actionFragment instanceof BkWelcomeFragment) {
				FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
				fragmentTransaction.show(actionFragment);
				fragmentTransaction.commit();
			}
		} else {
			BkWelcomeFragment fragment = (BkWelcomeFragment)getFragmentManager().findFragmentById(R.id.fragment_container);
			if(fragment == null) {
				fragment = new BkWelcomeFragment();
			}

			FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
			fragmentTransaction.add(R.id.fragment_container, fragment);
			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		}

		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}
	
	void startApp() { 
		Intent intent = getIntent();
		finish();
		startActivity(intent);
	}


	@Override
	public void onDialogPositiveClick(DialogFragment dialog) {
		EditText et = (EditText)dialog.getDialog().findViewById(R.id.connection_parameter);
		String connectionParameter = et.getText().toString().trim();
		
		BkWelcomeFragment actionFragment = (BkWelcomeFragment)getFragmentManager().findFragmentById(R.id.fragment_container);
		if(actionFragment != null) {
//			numberOfGuesses = actionFragment.getCount();
		}
		
		Chronometer chr = (Chronometer)findViewById(R.id.chronometer1);
		String elapsedTime = "00:01";
		if(chr != null) {
			elapsedTime = chr.getText().toString().replace("Elapsed Time: ", "");
		}
		
//		addToTopScores(elapsedTime, numberOfGuesses, name, true);
		
//		displayTopScores();
		startMainActionFragment(connectionParameter);

	}
	
	void startMainActionFragment(String connectionParameter) {
		BkActionFragment fragment = (BkActionFragment)getFragmentManager().findFragmentById(R.layout.bk_action_fragment);
		if(fragment == null) {
			fragment = new BkActionFragment();
			Bundle bundle = new Bundle();
			bundle.putString(BkActionFragment.CONNECTION_STRING_KEY, connectionParameter);
			fragment.setArguments(bundle);
		}
		FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.fragment_container, fragment);
		fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}
	

}
