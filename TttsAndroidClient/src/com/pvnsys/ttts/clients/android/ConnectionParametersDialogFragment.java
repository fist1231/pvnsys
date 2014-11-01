package com.pvnsys.ttts.clients.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

public class ConnectionParametersDialogFragment extends DialogFragment {
	
	
    public interface ConnectionParametersDialogListener  {
        public void onDialogPositiveClick(DialogFragment dialog);
    }
	
    ConnectionParametersDialogListener mListener;
    
   
    @Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (ConnectionParametersDialogListener)activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement ConnectionParametersDialogListener");
        }
    }
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        
        builder.setView(inflater.inflate(R.layout.bk_connection_dialog, null))
        	.setMessage(R.string.connection_string_message)
        	.setTitle(R.string.connection_dialog_title)
        	.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       mListener.onDialogPositiveClick(ConnectionParametersDialogFragment.this);
                   }
               });
        
        // Create the AlertDialog object and return it
        return builder.create();
	}
	

}
