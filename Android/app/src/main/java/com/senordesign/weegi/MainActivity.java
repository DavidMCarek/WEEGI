package com.senordesign.weegi;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import io.resourcepool.jarpic.client.SsdpClient;
import io.resourcepool.jarpic.model.DiscoveryListener;
import io.resourcepool.jarpic.model.DiscoveryRequest;
import io.resourcepool.jarpic.model.SsdpService;
import retrofit2.Retrofit;


import com.senordesign.weegi.web.services.CytonService;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final String OPENBCI_DEVICE_TYPE = "urn:schemas-upnp-org:device:Basic:1";
    private Retrofit mRetrofit;
    private CytonService mCytonService;

    private Spinner mDeviceSpinner;
    private List<String> deviceList;
    private ArrayAdapter<String> deviceListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        deviceList = new ArrayList<>();
        deviceListAdapter = new ArrayAdapter<>(
                getApplicationContext(), android.R.layout.simple_spinner_item, deviceList);
        deviceList.add(getString(R.string.searching));
        deviceListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDeviceSpinner = findViewById(R.id.device_spinner);
        mDeviceSpinner.setAdapter(deviceListAdapter);

        updateDeviceList();
    }

    @OnItemSelected(R.id.device_spinner)
    public void deviceSelected(Spinner spinner, int position) {
        checkRetrofitClient();
        // TODO
        // try connecting
        // if failure, then show toast and refresh device list
    }

    @OnClick(R.id.device_refresh_button)
    public void onDeviceRefreshClick() {
        updateDeviceList();
    }

    @OnClick(R.id.start_recording_btn)
    public void onStartRecordingClick() {
        checkRetrofitClient();
    }

    @OnClick(R.id.stop_recording_btn)
    public void onStopRecordingClick() {
        checkRetrofitClient();
    }

    private void updateDeviceList() {
        deviceList.clear();
        deviceListAdapter.clear();

        SsdpClient client = SsdpClient.create();
        DiscoveryRequest networkStorageDevice = DiscoveryRequest.builder()
                .serviceType(OPENBCI_DEVICE_TYPE)
                .build();
        client.discoverServices(networkStorageDevice, new DiscoveryListener() {
            @Override
            public void onServiceDiscovered(SsdpService service) {
                System.out.println("Found service: " + service);
            }
        });

        if (deviceList.size() == 0)
            deviceList.add(getString(R.string.no_devices));
        deviceListAdapter.addAll(deviceList);
        deviceListAdapter.notifyDataSetChanged();
    }

    private void checkRetrofitClient() {
        if (mRetrofit == null)
            initializeRetrofitClient();

        if (!mRetrofit.baseUrl().toString().contains(mDeviceSpinner.getSelectedItem().toString()))
            initializeRetrofitClient();
    }

    private void initializeRetrofitClient() {
        mRetrofit = new Retrofit.Builder()
                .baseUrl("http://" + (mDeviceSpinner.getSelectedItem() != null
                        ? mDeviceSpinner.getSelectedItem().toString()
                        : "0.0.0.0") + "/")
                .build();

        mCytonService = mRetrofit.create(CytonService.class);
    }
}
