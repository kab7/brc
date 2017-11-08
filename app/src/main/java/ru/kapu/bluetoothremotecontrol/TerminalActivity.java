package ru.kapu.bluetoothremotecontrol;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;

public class TerminalActivity extends AppCompatActivity {

    private BluetoothSPP bt;
    TextView text_terminal;
    EditText edit_command;
    Button button_send;
    ScrollView scroll_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal);

        text_terminal = (TextView)findViewById(R.id.tvTerminal);
        edit_command = (EditText)findViewById(R.id.etCommand);
        button_send = (Button)findViewById(R.id.btnSend);
        scroll_view = (ScrollView)findViewById(R.id.scrollview1_terminal);

        MyApp app = (MyApp) getApplication();
        bt = app.getBt();

        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                text_terminal.append(message + "\n");
                scroll_view.smoothScrollTo(0, text_terminal.getBottom());
            }
        });

        button_send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (edit_command.getText().length() != 0) {
                    bt.send(edit_command.getText().toString(), true);
                    edit_command.setText("");
                }
            }
        });

    }
}
