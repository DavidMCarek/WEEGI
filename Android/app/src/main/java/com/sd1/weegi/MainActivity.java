package com.sd1.weegi;

import android.Manifest;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.polidea.rxandroidble.RxBleClient;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity {

    @Inject
    RxBleClient mRxBleClient;

    private static final String[] PERMISSIONS = new String[]{
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((MainApplication) getApplication()).getRxBleClientComponent().inject(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

}
