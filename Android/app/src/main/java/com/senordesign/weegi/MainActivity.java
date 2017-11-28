package com.senordesign.weegi;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import io.resourcepool.jarpic.model.DiscoveryListener;
import io.resourcepool.jarpic.model.DiscoveryRequest;
import io.resourcepool.jarpic.model.SsdpService;
import io.resourcepool.jarpic.model.SsdpServiceAnnouncement;
import retrofit2.Retrofit;


import com.senordesign.weegi.web.services.CytonService;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String OPENBCI_DEVICE_TYPE = "urn:schemas-upnp-org:device:Basic:1";
    private static final String TAG = "com.seniordesign.weegi";
    private static final long UPDATE_LIST_TIME_MILLIS = 5000L;
    private static final long EXPIRATION_TIME_MILLIS = 10000L;

    private Retrofit mRetrofit;
    private CytonService mCytonService;
    private WEEGiSsdpClientImpl client;

    private TextView mDeviceText;
    private Spinner mDeviceSpinner;
    private List<String> deviceList;
    private ArrayAdapter<String> deviceListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mDeviceText = findViewById(R.id.device_text);

        deviceList = new ArrayList<>();
        deviceListAdapter = new ArrayAdapter<>(
                getApplicationContext(), android.R.layout.simple_spinner_item, deviceList);
        deviceListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDeviceSpinner = findViewById(R.id.device_spinner);
        mDeviceSpinner.setAdapter(deviceListAdapter);

        client = new WEEGiSsdpClientImpl();
        setupDeviceRefreshTimer();
    }

    private void setupDeviceRefreshTimer() {
        Handler mListHandler = new Handler();
        Runnable mRemoveExpiredDevicesTimer = new Runnable() {
            @Override
            public void run() {
                List<String> newDeviceList = new ArrayList<>();
                boolean listChanged = false;

                for (SsdpService service: client.getUnexpiredServices(OPENBCI_DEVICE_TYPE, EXPIRATION_TIME_MILLIS))
                    newDeviceList.add(service.getRemoteIp().toString());

                if (newDeviceList.size() != deviceList.size())
                    listChanged = true;
                else
                    for (String list : deviceList)
                        if (!newDeviceList.contains(list))
                            listChanged = true;

                deviceList = newDeviceList;
                mDeviceText.setText(getText(R.string.device_label).toString() + " (" + deviceList.size() + ")");
                if (listChanged) {
                    deviceListAdapter = new ArrayAdapter<>(
                            getApplicationContext(), android.R.layout.simple_spinner_item, deviceList);
                    deviceListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    deviceListAdapter.notifyDataSetChanged();
                }
                updateDeviceList(OPENBCI_DEVICE_TYPE);
                mListHandler.postDelayed(this, UPDATE_LIST_TIME_MILLIS);
            }
        };

        updateDeviceList(OPENBCI_DEVICE_TYPE);
        mListHandler.postDelayed(mRemoveExpiredDevicesTimer, UPDATE_LIST_TIME_MILLIS);
    }

    @OnItemSelected(R.id.device_spinner)
    public void deviceSelected(Spinner spinner, int position) {
        Log.d(TAG, "device_spinner");
        if (!checkRetrofitClient())
            return;
        // TODO
        // try connecting
        // if failure, then show toast and refresh device list
    }

    @OnClick(R.id.device_refresh_button)
    public void onDeviceRefreshClick() {
        Log.d(TAG, "device_refresh_button");
        updateDeviceList(OPENBCI_DEVICE_TYPE);
    }

    @OnClick(R.id.start_recording_btn)
    public void onStartRecordingClick() {
        Log.d(TAG, "start_recording_btn");
        checkRetrofitClient();
    }

    @OnClick(R.id.stop_recording_btn)
    public void onStopRecordingClick() {
        Log.d(TAG, "stop_recording_btn");
        checkRetrofitClient();
    }

    private void updateDeviceList(String serviceType) {
        DiscoveryRequest networkDevice = DiscoveryRequest.builder()
                .serviceType(serviceType)
                .build();
        client.discoverServices(networkDevice, new DiscoveryListener() {
            @Override
            public void onServiceDiscovered(SsdpService service) {
                Log.i(TAG, "Service discovered" + service);
            }

            @Override
            public void onServiceAnnouncement(SsdpServiceAnnouncement announcement) {
                Log.i(TAG, "Service announced something: " + announcement);
            }

            @Override
            public void onFailed(Exception ex) {
                Log.i(TAG, "Service discovery failed: " + ex.getMessage());
            }
        });

    }

    private boolean checkRetrofitClient() {
        if (mDeviceSpinner.getSelectedItem() == null) {
            Toast.makeText(this, "Device not selected", Toast.LENGTH_LONG).show();
            return false;
        }

        if (mRetrofit == null)
            initializeRetrofitClient();

        if (!mRetrofit.baseUrl().toString().contains(mDeviceSpinner.getSelectedItem().toString()))
            initializeRetrofitClient();

        return true;
    }

    private void initializeRetrofitClient() {
        mRetrofit = new Retrofit.Builder()
                .baseUrl("http://" + mDeviceSpinner.getSelectedItem().toString() + "/")
                .build();

        mCytonService = mRetrofit.create(CytonService.class);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        client.stopDiscovery();
    }
}
