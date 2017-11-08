package ru.kapu.bluetoothremotecontrol;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by kapus on 17.12.2015.
 */
public class ReceivedCodesAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final ArrayList<String> values;

    public ReceivedCodesAdapter(Context context,  ArrayList<String> values) {
        super(context, R.layout.item_code, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.received_code, parent, false);
        TextView tvName = (TextView) rowView.findViewById(R.id.tvReceivedName);
        TextView tvTime = (TextView) rowView.findViewById(R.id.tvReceivedTime);
        TextView tvCode = (TextView) rowView.findViewById(R.id.tvReceivedCode);

        String[] elems=values.get(position).split(" ");

        tvName.setText(elems[0]);
        tvCode.setText(elems[1]);
        tvTime.setText("Received: " + elems[2]);

        return rowView;
    }
}
