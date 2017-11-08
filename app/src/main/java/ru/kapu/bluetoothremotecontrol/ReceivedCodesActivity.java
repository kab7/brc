package ru.kapu.bluetoothremotecontrol;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;

public class ReceivedCodesActivity extends AppCompatActivity implements BluetoothSPP.OnDataReceivedListener {

    private BluetoothSPP bt;
    private ArrayAdapter<String> data_adapter;
    private ArrayList<String> codes_list;
    private Context context;
    private ProgressDialog progress;
    private ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_received_codes);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        MyApp app = (MyApp) getApplication();
        bt = app.getBt();
        bt.setOnDataReceivedListener(this);

        context = this;

        lv = (ListView) findViewById(R.id.lvCodes);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//        codes_list = getIntent().getStringArrayListExtra("codes_list");
//        data_adapter = new ReceivedCodesAdapter(this, codes_list);
//        lv.setAdapter(data_adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final AlertDialog.Builder alert = new AlertDialog.Builder(context);

                final EditText edittext = new EditText(context);

                edittext.setHint("New code");
                edittext.setSingleLine();

                FrameLayout container = new FrameLayout(context);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.leftMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, context.getResources().getDisplayMetrics());
                params.rightMargin = params.leftMargin;
                edittext.setLayoutParams(params);

                alert.setMessage("Enter code name");
                alert.setTitle("Add new code");

                container.addView(edittext);
                alert.setView(container);
                final String code = codes_list.get(position);

                alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Intent i = new Intent();
                        i.putExtra("code", code);
                        i.putExtra("code_name", edittext.getText().toString());
                        setResult(1, i);
                        finish();
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever you want to do with No option.
                    }
                });

                AlertDialog dlg = alert.create();
                dlg.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                dlg.show();
            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_refresh);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bt.send("codes", true);
            }
        });


        bt.send("codes", true);
    }

    @Override
    public void onDataReceived(byte[] data, String message)
    {
        if (message.equals("codes")) {
            //! get first response, prepare array
            codes_list = new ArrayList<String>(); //! prepare array of strings

            progress = new ProgressDialog(this);
            progress.setMessage("Fetching codes...");
            progress.show();
        }
        else if (message.startsWith("Code"))
        {
            //! Code received
            String received_code;
            String[] elems = message.split(",");

            Calendar calendar = Calendar.getInstance();
            long t = calendar.getTimeInMillis();

            if (elems.length==4)
            {
                received_code = elems[0].substring(6)
                        + " " + elems[1].substring(7, 7 + 16);

                t -= 1000 * Integer.parseInt(elems[3].substring(7).trim());
            }
            else if (elems.length==5)
            {
                received_code = elems[0].substring(6)
                        + "/" + elems[1].substring(7)
                        + " " + elems[2].substring(7, 7 + 16);
                t -= 1000 * Integer.parseInt(elems[4].substring(7).trim());
            }
            else
            {
                received_code = message;
            }

            calendar.setTimeInMillis(t);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            received_code += " " + sdf.format(calendar.getTime());

            codes_list.add(received_code);
        }
        else
        {
            //! Finished, show choose dlg
            progress.dismiss();
            data_adapter = new ReceivedCodesAdapter(this, codes_list);
            lv.setAdapter(data_adapter);
        }

    }
}
