package ru.kapu.bluetoothremotecontrol;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by kab on 11.12.2015.
 */

public class CodesCursorAdapter extends CursorAdapter {
    public CodesCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_code, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // Find fields to populate in inflated template
        TextView tvName = (TextView) view.findViewById(R.id.tvName);
        TextView tvCodeName = (TextView) view.findViewById(R.id.tvCodeName);
        TextView tvCode = (TextView) view.findViewById(R.id.tvCode);

        // Extract properties from cursor
        Integer id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
        String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
        String code_name = cursor.getString(cursor.getColumnIndexOrThrow("code_name"));
        String type_name = cursor.getString(cursor.getColumnIndexOrThrow("type_name"));
        String code = cursor.getString(cursor.getColumnIndexOrThrow("code"));

        if (type_name!=null && !type_name.isEmpty())
        {
            code_name += "/" + type_name;
        }

        // Populate fields with extracted properties
        tvName.setText(description);
        tvCodeName.setText(code_name);
        tvCode.setText(code);

        view.setTag(id);
    }
}
