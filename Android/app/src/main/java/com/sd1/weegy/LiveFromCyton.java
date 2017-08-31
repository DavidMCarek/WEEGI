package com.sd1.weegy;

/**
 * Created by sun on 8/29/2017.
 */
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LiveFromCyton extends AppCompatActivity{

    //turn on bluetooth
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    // Phone does not support Bluetooth so let the user know and exit.
        if (BluetoothAdapter = null) {
            new AlertDialog.Builder(this)
                .setTitle("Not compatible")
                .setMessage("Your phone does not support Bluetooth")
                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                    }
                 })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }



    Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

    List<String> s = new ArrayList<String>();
    for(BluetoothDevice bt : pairedDevices)
            s.add(bt.getName());

    setListAdapter(new ArrayAdapter<String>(this, R.layout.list, s));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.live_from_cyton);

        //sets up drop down to chose a source
        bluetoothSpinner = (Spinner)findViewById(R.id.SourceSpinner);
        ArrayAdapter<String>sourceStrAdapter = new ArrayAdapter<String>(LiveFromCyton.this, android.R.layout.simple_spinner_item, sources);
        sourceStrAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bluetoothSpinner.setAdapter(sourceStrAdapter);
        //sourceSpinner.setOnItemSelectedListener(this);
}
