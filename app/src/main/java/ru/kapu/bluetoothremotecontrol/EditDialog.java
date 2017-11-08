package ru.kapu.bluetoothremotecontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.widget.EditText;

/**
 * Created by kab on 13.12.2015.
 */

public class EditDialog  extends DialogFragment {

    /* The activity that creates an instance of this dialog fragment must
    * implement this interface in order to receive event callbacks.
    * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeEditDialogListener {
        public void onDialogEdit(DialogFragment dialog, Integer id, String desc, String name, String type, String code);
    }

    // Use this instance of the interface to deliver action events
    NoticeEditDialogListener mListener;
    Integer code_id;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setTitle(R.string.edit_code);

        builder.setView(inflater.inflate(R.layout.manual_input, null))
                // Add action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Adding new code
                        Dialog dlg = (Dialog) dialog;
                        EditText edit;
                        edit = (EditText) dlg.findViewById(R.id.code_desc);
                        String code_desc = edit.getText().toString();
                        edit = (EditText) dlg.findViewById(R.id.code_name);
                        String code_name = edit.getText().toString();
                        edit = (EditText) dlg.findViewById(R.id.code_type);
                        String code_type = edit.getText().toString();
                        edit = (EditText) dlg.findViewById(R.id.code);
                        String code = edit.getText().toString();

                        mListener.onDialogEdit(EditDialog.this, code_id, code_desc, code_name, code_type, code);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        EditDialog.this.getDialog().cancel();
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
            mListener = (NoticeEditDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeEditDialogListener");
        }
    }

    public void SetValues(Integer id, String code_desc, String code_name, String code_type, String code)
    {
        Dialog dlg = this.getDialog();
        EditText edit;
        edit = (EditText) dlg.findViewById(R.id.code_desc);
        edit.setText(code_desc);
        edit = (EditText) dlg.findViewById(R.id.code_name);
        edit.setText(code_name);
        if (code_type!=null && !code_type.isEmpty()) {
            edit = (EditText) dlg.findViewById(R.id.code_type);
            edit.setText(code_type);
        }
        edit = (EditText) dlg.findViewById(R.id.code);
        edit.setText(code);

        code_id = id;
    }

    public void SetTitle(String title)
    {
        Dialog dlg = this.getDialog();
        dlg.setTitle(title);
    }

}
