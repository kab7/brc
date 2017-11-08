package ru.kapu.bluetoothremotecontrol;

import android.app.Application;
import android.content.Context;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;

/**
 * Created by kapus on 19.12.2015.
 */
public class MyApp extends Application {

    private static MyApp instance;

    private BluetoothSPP bt;

    public BluetoothSPP getBt() { return  bt;}
    public void setBt(BluetoothSPP obj) { this.bt = obj; }

    public MyApp()
    {
        super();
        instance = this;
        bt = new BluetoothSPP(this);
    }

    public static MyApp getInstance() {
        return instance;
    }
}
