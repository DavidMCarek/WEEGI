package com.senordesign.weegi;

import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.senordesign.weegi.web.models.CommandRequestModel;
import com.senordesign.weegi.web.services.CytonService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import io.resourcepool.jarpic.model.DiscoveryListener;
import io.resourcepool.jarpic.model.DiscoveryRequest;
import io.resourcepool.jarpic.model.SsdpService;
import io.resourcepool.jarpic.model.SsdpServiceAnnouncement;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.sd_checkbox)
    protected CheckBox mSdCardCheckbox;

    @BindView(R.id.cloud_checkbox)
    protected CheckBox mCloudCheckbox;

    @BindView(R.id.cloud_settings_layout)
    protected ConstraintLayout mCloudSettingsLayout;

    private static final String OPENBCI_DEVICE_TYPE = "urn:schemas-upnp-org:device:Basic:1";
    private static final String TAG = "com.seniordesign.weegi";
    private static final long UPDATE_LIST_TIME_MILLIS = 5000L;
    private static final long EXPIRATION_TIME_MILLIS = 60000L;

    private Retrofit mRetrofit;
    private CytonService mCytonService;

    private AppCompatActivity mThis;
    private TextView mDeviceText;
    private Spinner mDeviceSpinner;

    private List<String> deviceList;
    private ArrayAdapter<String> deviceListAdapter;
    private WEEGiSsdpClientImpl client;

    private static final String ATTACH_COMMAND = "{";
    private static final String TURN_CHANNELS_ON_COMMAND = "!@#$%^&*";
    private static final String RECORD_5_MIN_COMMAND = "A";
    private static final String STOP_RECORD_COMMAND = "j";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mThis = this;
        mDeviceText = findViewById(R.id.device_text);
        mDeviceSpinner = findViewById(R.id.device_spinner);

        deviceList = new ArrayList<>();
        deviceListAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, deviceList);
        deviceListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        client = new WEEGiSsdpClientImpl();
        setupDeviceRefreshTimer();
    }

    private void setupDeviceRefreshTimer() {
        updateDeviceList(OPENBCI_DEVICE_TYPE);
        Handler mListHandler = new Handler();
        Runnable mRemoveExpiredDevicesTimer = new Runnable() {
            @Override
            public void run() {
                List<String> newDeviceList = new ArrayList<>();
                boolean listChanged = false;

                for (SsdpService service: client.getUnexpiredServices(OPENBCI_DEVICE_TYPE, EXPIRATION_TIME_MILLIS))
                    newDeviceList.add(service.getRemoteIp().getHostAddress());

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
                            mThis, android.R.layout.simple_spinner_item, deviceList);
                    deviceListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    mDeviceSpinner.setAdapter(deviceListAdapter);
                    deviceListAdapter.notifyDataSetChanged();
                    if (mDeviceSpinner.getSelectedItemPosition() < 0 && deviceList.size() > 0)
                        mDeviceSpinner.setSelection(0);
                }
                mListHandler.postDelayed(this, UPDATE_LIST_TIME_MILLIS);
            }
        };

        mListHandler.postDelayed(mRemoveExpiredDevicesTimer, UPDATE_LIST_TIME_MILLIS);
    }

    @OnItemSelected(R.id.device_spinner)
    public void deviceSelected(Spinner spinner, int position) {
        Log.d(TAG, "device_spinner");

        // TODO
        // try connecting
        // if failure, then show toast and refresh device list
    }

    @OnClick(R.id.start_recording_btn)
    public void onStartRecordingClick() {
        Log.d(TAG, "start_recording_btn");
        if (!checkRetrofitClient())
            return;

        if (!mCloudCheckbox.isChecked() && !mSdCardCheckbox.isChecked()) {
            Toast.makeText(this, "Please select a recording destination.", Toast.LENGTH_LONG).show();
            return;
        }

        Call<Void> setupRequest = mCytonService.executeCommand(new CommandRequestModel(ATTACH_COMMAND));

        setupRequest.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!MainActivity.this.isDestroyed()) {
                    if (response.isSuccessful())
                        MainActivity.this.startStreaming();
                    else {
                        Toast.makeText(MainActivity.this, "Failed to setup wifi shield", Toast.LENGTH_LONG).show();
                        try {
                            Log.e("CytonError", response.errorBody().string() + " ");
                        } catch (IOException e) {}
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("CytonError", "Request failed", t);
                if (!MainActivity.this.isDestroyed())
                    Toast.makeText(MainActivity.this, "Error: Failed to setup wifi shield", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startStreaming() {
        Call<Void> startStreamRequest = mCytonService.startStreaming();
        startStreamRequest.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!MainActivity.this.isDestroyed()) {
                    if (response.isSuccessful()) {
                        if (mSdCardCheckbox.isChecked()) {
                            turnOnChannels();
                        }
                        if (mCloudCheckbox.isChecked()) {
                            // ToDo start mqtt shazz
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Failed to start streaming.", Toast.LENGTH_LONG).show();
                        try {
                            Log.e("CytonError", response.errorBody().string() + " ");
                        } catch (IOException e) {}
                    }
                }

            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("CytonError", "Request failed", t);
                if (!MainActivity.this.isDestroyed())
                    Toast.makeText(MainActivity.this, "Error: Failed to start streaming.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void turnOnChannels() {
        Call<Void> turnOnChannelsRequest = mCytonService.executeCommand(new CommandRequestModel(TURN_CHANNELS_ON_COMMAND));
        turnOnChannelsRequest.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!MainActivity.this.isDestroyed()) {
                    if (response.isSuccessful())
                        MainActivity.this.startRecording();
                    else {
                        Toast.makeText(MainActivity.this, "Failed to turn on channels", Toast.LENGTH_LONG).show();
                        try {
                            Log.e("CytonError", response.errorBody().string() + " ");
                        } catch (IOException e) {}
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("CytonError", "Request failed", t);
                if (!MainActivity.this.isDestroyed())
                    Toast.makeText(MainActivity.this, "Error: Failed to turn on channels", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startRecording() {
        Call<Void> recordRequest = mCytonService.executeCommand(new CommandRequestModel(RECORD_5_MIN_COMMAND));
        recordRequest.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!MainActivity.this.isDestroyed()) {
                    if (response.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Started recording", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Failed to start recording", Toast.LENGTH_LONG).show();
                        try {
                            Log.e("CytonError", response.errorBody().string() + " ");
                        } catch (IOException e) {}
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("CytonError", "Request failed", t);
                if (!MainActivity.this.isDestroyed()) {
                    Toast.makeText(MainActivity.this, "Error: Failed to start recording", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @OnClick(R.id.stop_recording_btn)
    public void onStopRecordingClick() {
        Log.d(TAG, "stop_recording_btn");
        if (!checkRetrofitClient())
            return;

        Call<Void> stopRecordRequest = mCytonService.executeCommand(new CommandRequestModel(STOP_RECORD_COMMAND));
        stopRecordRequest.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!MainActivity.this.isDestroyed()) {
                    if (response.isSuccessful()) {
                        stopStreaming();
                    } else {
                        Toast.makeText(MainActivity.this, "Failed to stop recording.", Toast.LENGTH_LONG).show();
                        try {
                            Log.e("CytonError", response.errorBody().string() + " ");
                        } catch (IOException e) {}
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("CytonError", "Request failed", t);
                if (!MainActivity.this.isDestroyed()) {
                    Toast.makeText(MainActivity.this, "Error: Failed to stop recording.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void stopStreaming() {
        Call<Void> stopStreamRequest = mCytonService.stopStreaming();
        stopStreamRequest.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!MainActivity.this.isDestroyed()) {
                    if (response.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Successfully stopped recording.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Failed to stop streaming.", Toast.LENGTH_LONG).show();
                        try {
                            Log.e("CytonError", response.errorBody().string() + " ");
                        } catch (IOException e) {}
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("CytonError", "Request failed", t);
                if (!MainActivity.this.isDestroyed()) {
                    Toast.makeText(MainActivity.this, "Error: Failed to stop streaming.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @OnCheckedChanged(R.id.cloud_checkbox)
    public void onCloudCheckboxChange() {
        if (mCloudCheckbox.isChecked())
            mCloudSettingsLayout.setVisibility(View.VISIBLE);
        else
            mCloudSettingsLayout.setVisibility(View.GONE);
    }

    private void updateDeviceList(String serviceType) {
        DiscoveryRequest networkDevice = DiscoveryRequest.builder()
                .serviceType(serviceType)
                .build();
        client.discoverServices(networkDevice, new DiscoveryListener() {    // automatically called every INTERVAL_BETWEEN_REQUESTS ms
            @Override
            public void onServiceDiscovered(SsdpService service) {
                Log.i(TAG, "Service discovered: " + service);
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
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http:/" + mDeviceSpinner.getSelectedItem().toString() + "/")
                .build();

        mCytonService = mRetrofit.create(CytonService.class);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        client.stopDiscovery();
    }
}
