package com.pvnsys.ttts.clients.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

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
       
		// Read connectParam connection_parameter_file to search for existing connection string
        SharedPreferences sp = this.getActivity().getSharedPreferences(getString(R.string.connection_param_file), Context.MODE_PRIVATE);
		String extistingConnectionString = sp.getString(BkActionFragment.CONNECTION_STRING_KEY, "");
		
		View dialogView = inflater.inflate(R.layout.bk_connection_dialog, null);

		if(extistingConnectionString != null && extistingConnectionString.trim().length() > 0) {
			EditText et = (EditText)dialogView.findViewById(R.id.connection_parameter);
			et.setText(extistingConnectionString);
		}
		
        builder.setView(dialogView)
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
