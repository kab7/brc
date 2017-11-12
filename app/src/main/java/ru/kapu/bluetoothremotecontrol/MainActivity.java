package ru.kapu.bluetoothremotecontrol;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.HapticFeedbackConstants;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

import static android.os.VibrationEffect.DEFAULT_AMPLITUDE;
import static android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING;

public class MainActivity extends AppCompatActivity
    implements AddNewDialog.NoticeAddNewDialogListener,
        ManualInputDialog.NoticeManualInputDialogListener,
        EditDialog.NoticeEditDialogListener,
        BluetoothSPP.OnDataReceivedListener,
        BluetoothSPP.BluetoothConnectionListener
{

    private final int IDD_ADD_NEW_DIALOG = 0;
    private static final int REQUEST_CHOOSE_CODE = 1;
    private static final int REQUEST_TERMINAL_MODE = 2;
    Context context;
    CodesDatabase mydb;
    CodesCursorAdapter codes_adapter;

    private BluetoothSPP bt;
    String stored_address;
    String last_command;
    Integer last_code_id;
    ArrayList<String> codes_list;

    //! permissions
    private int grantResults[];
    private static final int REQUEST_WRITE_PERMISSIONS = 1;
    private static final int REQUEST_READ_PERMISSIONS = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setIcon(R.drawable.ic_bluetooth);

        context = MainActivity.this;

        //! Add new dialog
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment new_dlg = new AddNewDialog();
                new_dlg.show(getSupportFragmentManager(), "NoticeDialogFragment");
            }
        });

        //! Read bt address
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        stored_address = pref.getString("address", "");

        //! DB
        mydb = new CodesDatabase(this);

        // находим список
        ListView lvMain = findViewById(R.id.lvMain);

        // создаем адаптер
        codes_adapter = new CodesCursorAdapter(context, mydb.GetAllCodes());

        // присваиваем адаптер списку
        lvMain.setAdapter(codes_adapter);

        //! создаем контекстное меню
        registerForContextMenu(lvMain);

        lvMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Integer code_id = (Integer) view.getTag();
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                SendCode(code_id, 1);
            }
        });

        MyApp app = (MyApp) getApplication();
        bt = app.getBt();

        if(!bt.isBluetoothAvailable()) {
            Toast toast = Toast.makeText(getApplicationContext(), "Bluetooth not available!", Toast.LENGTH_LONG);
            toast.show();

            //! Close app, no BT in this device
            //!finish();
        }

        bt.setOnDataReceivedListener(this);
        bt.setBluetoothConnectionListener(this);
    }

    private void SendCode(Integer code_id, Integer counter)
    {
        if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
            String code_name = mydb.GetFieldById(code_id, "code_name");
            String code = mydb.GetFieldById(code_id, "code");
            last_command = "tx " + code_name + " " + code;
            if (counter > 1)
            {
                last_command += " " + counter.toString();
            }
            last_code_id = code_id;

            bt.send(last_command, true);
        } else {
            ShowNotConnectedMessage();
        }
    }

    /*
    @Override
    public void onStop()
    {
        super.onStop();
        bt.stopService();
    }*/

    @Override
    public void onStart()
    {
        super.onStart();
        Log.d("BRC", "Start");

        if (!bt.isBluetoothEnabled())
        {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        }
        else
        {
            if(!bt.isServiceAvailable())
            {
                Log.d("BRC", "Setup service");
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
            }

            if (bt.getServiceState() != BluetoothState.STATE_CONNECTED)
            {
                if (stored_address != null && !stored_address.isEmpty())
                {
                    bt.connect(stored_address);
                }
            }
            else
            {
                getSupportActionBar().setIcon(R.drawable.ic_bluetooth_connected);
            }
        }
    }

    @Override
    public void onDestroy()
    {
        Log.d("BRC", "Destroy");
        super.onDestroy();

        //! destroy bluetooth only if is finished
        if (isFinishing())
        {
            bt.stopService();
        }
    }

    public void onDeviceConnected(String name, String address)
    {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = pref.edit();
        edit.putString("address", address);
        edit.apply();

        getSupportActionBar().setIcon(R.drawable.ic_bluetooth_connected);

        Snackbar.make(findViewById(R.id.lvMain), "Connected to: " + name + "(" + address + ")", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
    }

    public void onDeviceDisconnected()
    {
        getSupportActionBar().setIcon(R.drawable.ic_bluetooth);
        Snackbar.make(findViewById(R.id.lvMain), "Disconnected", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
    }

    public void onDeviceConnectionFailed()
    {
        Snackbar.make(findViewById(R.id.lvMain), "Unable to connect", Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    public void ShowNotConnectedMessage()
    {
        Snackbar.make(findViewById(R.id.lvMain), "Not connected to device", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
    }

    @Override
    public void onDataReceived(byte[] data, String message)
    {
        if (!message.equals(last_command)) {
            //! Store new code
            if (message.startsWith("Code: ")) {
                Integer pos = message.indexOf("Data: ");
                if (pos > 0) {
                    if (last_code_id != -1) {
                        //! update code
                        mydb.UpdateCodeOnly(last_code_id, message.substring(pos + 6, pos + 6 + 16));
                        //! update listview
                        codes_adapter.changeCursor(mydb.GetAllCodes());
                        //! Show bar
                        Snackbar.make(findViewById(R.id.lvMain), "Success! New code: " + message.substring(pos + 6, pos + 6 + 16), Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                    }
                    last_code_id = -1;
                }
            } else {
                Snackbar.make(findViewById(R.id.lvMain), message, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                bt.connect(data);
            }
        }
        else if (requestCode == BluetoothState.REQUEST_ENABLE_BT)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
            }
            else
            {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        else if (requestCode == REQUEST_CHOOSE_CODE)
        {
            if (resultCode == 1) {
                String result=data.getStringExtra("code");

                if (result!=null && !result.isEmpty())
                {
                    String[] elems=result.split(" ");
                    String code_name;
                    String code_type;
                    String code = elems[1];
                    if (elems[0].contains("/"))
                    {
                        //! have type
                        String[] subelems = elems[0].split("/");
                        code_name = subelems[0];
                        code_type = subelems[1];
                    }
                    else
                    {
                        code_name = elems[0];
                        code_type = null;
                    }

                    String name=data.getStringExtra("code_name");

                    if (name == null || name.isEmpty())
                    {
                        name="New code";
                    }

                    //! Add new code to database
                    mydb.AddNewCode(name, code_name, code_type, code);

                    //! update listview
                    codes_adapter.changeCursor(mydb.GetAllCodes());
                }
            }
            bt.setOnDataReceivedListener(this);
        }
        else if (requestCode == REQUEST_TERMINAL_MODE)
        {
            bt.setOnDataReceivedListener(this);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId()==R.id.lvMain) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.context_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        info.targetView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        switch(item.getItemId()) {
            case R.id.context_send10:
                Integer code_id = (Integer) info.targetView.getTag();
                SendCode(code_id, 10);
                return true;
            case R.id.context_edit:
                //! edit code
                Integer id = (Integer) info.targetView.getTag();

                DialogFragment edit_dlg = new EditDialog();
                edit_dlg.show(getSupportFragmentManager(), "NoticeDialogFragment");
                getSupportFragmentManager().executePendingTransactions();

                ((EditDialog)edit_dlg).SetValues(
                        id,
                        mydb.GetFieldById(id, "description"),
                        mydb.GetFieldById(id, "code_name"),
                        mydb.GetFieldById(id, "type_name"),
                        mydb.GetFieldById(id, "code")
                );
                return true;
            case R.id.context_remove:
                //! remove code
                mydb.RemoveCodeById((Integer)info.targetView.getTag());
                //! update listview
                codes_adapter.changeCursor(mydb.GetAllCodes());
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //! Connect to device
        if (id == R.id.action_connect) {

            if(bt.getServiceState() == BluetoothState.STATE_CONNECTED)
            {
                bt.disconnect();
            }

            Intent intent = new Intent(getApplicationContext(), DeviceList.class);
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);

            return true;
        }

        //! Disconnect from device
        if (id == R.id.action_unbind) {

            //! delete record of device!
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor edit = pref.edit();
            edit.remove("address");
            edit.apply();

            return true;
        }

        //! Export to CSV
        if (id == R.id.action_export)
        {
            //! Request permission to write to external storage
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED )
            {
                //if you dont have required permissions ask for it (only required for API 23+)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSIONS);
            }
            else
            {
                //! we have permissions, do export
                ExportToCSV();
            }

            return true;
        }

        //! Import from CSV
        if (id == R.id.action_import)
        {
            //! Request permission to write to external storage
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED )
            {
                //if you dont have required permissions ask for it (only required for API 23+)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_PERMISSIONS);
            }
            else
            {
                //! we have permissions, do export
                ImportFromCSV();
            }
            return true;
        }

        //! Run BT terminal
        if (id == R.id.action_terminal) {

            if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                Intent intent = new Intent(this, TerminalActivity.class);
                startActivityForResult(intent, REQUEST_TERMINAL_MODE);
            } else {
                ShowNotConnectedMessage();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void ExportToCSV() {
        String msg;

        if (isExternalStorageWritable()) {
            msg = mydb.ExportToCSV();
        } else {
            msg = "External storage is not writable";
        }

        Snackbar.make(findViewById(R.id.lvMain), msg, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
    }

    public void ImportFromCSV() {
        String msg;

        if (isExternalStorageReadable()) {
            msg = mydb.ImportFromCSV();
        } else {
            msg = "External storage is not readable";
        }

        //! update listview
        codes_adapter.changeCursor(mydb.GetAllCodes());

        Snackbar.make(findViewById(R.id.lvMain), msg, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
    }

    @Override // android recommended class to handle permissions
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_WRITE_PERMISSIONS: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d("permission", "granted");
                    ExportToCSV();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.uujm
                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case REQUEST_READ_PERMISSIONS: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d("permission", "granted");
                    ImportFromCSV();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.uujm
                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' line to check for other
            // permissions this app might request
        }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }


    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    @Override
    public void onDialogAddNewSelected(DialogFragment dialog, String item)
    {

        if (item.equals(getString(R.string.add_manual)))
        {
            //! Manual
            DialogFragment manual_dlg = new ManualInputDialog();
            manual_dlg.show(getSupportFragmentManager(), "NoticeDialogFragment");
        }
        else if (item.equals(getString(R.string.add_from_device)))
        {
            //! Read device
            if(bt.getServiceState() == BluetoothState.STATE_CONNECTED)
            {
                Intent intent = new Intent(this, ReceivedCodesActivity.class);
                startActivityForResult(intent, REQUEST_CHOOSE_CODE);
            }
            else
            {
                ShowNotConnectedMessage();
            }
        }
    }

    @Override
    public void onDialogManualInputAdd(DialogFragment dialog, String desc, String name, String type, String code) {
        //! Save to database
        mydb.AddNewCode(desc, name, type, code);

        //! update listview
        codes_adapter.changeCursor(mydb.GetAllCodes());
    }

    @Override
    public void onDialogEdit(DialogFragment dialog, Integer id, String desc, String name, String type, String code) {

        if (id!=-1)
        {
            //! edit code
            mydb.EditCode(id, desc, name, type, code);
        }
        else
        {
            mydb.AddNewCode(desc, name, type, code);
        }

        //! update listview
            codes_adapter.changeCursor(mydb.GetAllCodes());
    }

}
