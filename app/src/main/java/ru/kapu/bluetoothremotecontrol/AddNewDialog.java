package ru.kapu.bluetoothremotecontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Toast;

/**
 * Created by kapus on 07.12.2015.
 */
public class AddNewDialog extends DialogFragment {

    /* The activity that creates an instance of this dialog fragment must
        * implement this interface in order to receive event callbacks.
        * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeAddNewDialogListener {
        public void onDialogAddNewSelected(DialogFragment dialog, String item);
    }

    // Use this instance of the interface to deliver action events
    NoticeAddNewDialogListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final String[] mAddTypes = {
                getString(R.string.add_from_device),
                getString(R.string.add_manual)
        };

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.add_dialog_title);

        builder.setItems(mAddTypes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // The 'which' argument contains the index position
                // of the selected item
                mListener.onDialogAddNewSelected(AddNewDialog.this, mAddTypes[which]);
            }
        });

        return builder.create();
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeAddNewDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeAddNewDialogListener");
        }
    }
}
