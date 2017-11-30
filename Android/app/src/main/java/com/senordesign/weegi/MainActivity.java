package com.senordesign.weegi;

import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.senordesign.weegi.web.models.CommandRequestModel;
import com.senordesign.weegi.web.models.MQTTRequestModel;
import com.senordesign.weegi.web.models.MQTTResponseModel;
import com.senordesign.weegi.web.models.StatusResponseModel;
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

    @BindView(R.id.cloud_server_name)
    protected EditText mServerName;

    @BindView(R.id.cloud_server_username)
    protected EditText mServerUsername;

    @BindView(R.id.cloud_server_password)
    protected EditText mServerPassword;

    @BindView(R.id.is_recording_value)
    protected TextView mIsRecording;

    @BindView(R.id.is_streaming_value)
    protected TextView mIsStreaming;

    private static final String OPENBCI_DEVICE_TYPE = "urn:schemas-upnp-org:device:Basic:1";
    private static final String TAG = "com.seniordesign.weegi";
    private static final long UPDATE_LIST_TIME_MILLIS = 5000L;
    private static final long EXPIRATION_TIME_MILLIS = 60000L;

    private Retrofit mRetrofit;
    private CytonService mCytonService;

    private AppCompatActivity mThis;
    private TextView mDeviceText;
    private Spinner mDeviceSpinner;

    private List<String> mDeviceList;
    private ArrayAdapter<String> mDeviceListAdapter;
    private WEEGiSsdpClientImpl mClient;
    private int mRetryCounter;

    private static final String RECORD_5_MIN_COMMAND = "A";
    private static final String START_STREAM_COMMAND = "b";
    private static final String OPEN_CHANNELS_COMMAND = "!@#$%^&*";

    private static final String TURN_CHANNELS_OFF_COMMAND = "12345678";
    private static final String STOP_STREAM_COMMAND = "s";
    private static final String STOP_RECORD_COMMAND = "j";

    private static final String STATUS_COMMAND = "n";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mThis = this;
        mDeviceText = findViewById(R.id.device_text);
        mDeviceSpinner = findViewById(R.id.device_spinner);

        mDeviceList = new ArrayList<>();
        mDeviceListAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, mDeviceList);
        mDeviceListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mClient = new WEEGiSsdpClientImpl();
        setupDeviceRefreshTimer();
        setupDeviceStatusTimer();
    }

    private void setupDeviceRefreshTimer() {
        updateDeviceList(OPENBCI_DEVICE_TYPE);
        Handler mListHandler = new Handler();
        Runnable mRemoveExpiredDevicesTimer = new Runnable() {
            @Override
            public void run() {
                List<String> newDeviceList = new ArrayList<>();
                boolean listChanged = false;

                for (SsdpService service : mClient.getUnexpiredServices(OPENBCI_DEVICE_TYPE, EXPIRATION_TIME_MILLIS))
                    newDeviceList.add(service.getRemoteIp().getHostAddress());

                if (newDeviceList.size() != mDeviceList.size())
                    listChanged = true;
                else
                    for (String list : mDeviceList)
                        if (!newDeviceList.contains(list))
                            listChanged = true;

                mDeviceList = newDeviceList;
                mDeviceText.setText(getText(R.string.device_label).toString() + " (" + mDeviceList.size() + ")");
                if (listChanged) {
                    mDeviceListAdapter = new ArrayAdapter<>(
                            mThis, android.R.layout.simple_spinner_item, mDeviceList);
                    mDeviceListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    mDeviceSpinner.setAdapter(mDeviceListAdapter);
                    mDeviceListAdapter.notifyDataSetChanged();
                    if (mDeviceSpinner.getSelectedItemPosition() < 0 && mDeviceList.size() > 0)
                        mDeviceSpinner.setSelection(0);
                }
                mListHandler.postDelayed(this, UPDATE_LIST_TIME_MILLIS);
            }
        };

        mListHandler.postDelayed(mRemoveExpiredDevicesTimer, UPDATE_LIST_TIME_MILLIS);
    }

    private void setupDeviceStatusTimer() {
        Handler handler = new Handler();
        int delay = 3000; //milliseconds

        handler.postDelayed(new Runnable() {
            public void run() {
                if (mDeviceSpinner.getSelectedItem() != null && checkRetrofitClient()) {
                    checkStatus();
                }

                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    @OnItemSelected(R.id.device_spinner)
    public void deviceSelected(Spinner spinner, int position) {
        Log.d(TAG, "device_spinner");

        // TODO send yt
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

        if (mCloudCheckbox.isChecked() && mServerName.getText().toString().equals("")) {
            Toast.makeText(this, "Cloud server url required for cloud recording.", Toast.LENGTH_LONG).show();
            return;
        }

        checkStatusAndStart();
    }

    private void checkStatusAndStart() {
        Call<StatusResponseModel> statusRequest = mCytonService.checkStatus(
                new CommandRequestModel(STATUS_COMMAND)
        );
        mRetryCounter = 3;
        statusRequest.enqueue(new Callback<StatusResponseModel>() {
            @Override
            public void onResponse(Call<StatusResponseModel> call, Response<StatusResponseModel> response) {
                if (!MainActivity.this.isDestroyed()) {
                    if (response.isSuccessful()) {
                        if (response.body().isRecording() || response.body().isStreaming()) {
                            Toast.makeText(MainActivity.this, "Device is already recording or streaming. Please stop first.", Toast.LENGTH_LONG).show();
                        } else {
                            if (mCloudCheckbox.isChecked()) {
                                setupMQTT();
                            } else {
                                startRecord();
                            }
                        }

                    } else {
                        if (mRetryCounter > 0) {
                            mRetryCounter--;
                            call.clone().enqueue(this);
                            return;
                        }

                        Toast.makeText(MainActivity.this, "Failed to get status", Toast.LENGTH_LONG).show();
                        try {
                            Log.e("CytonError", response.errorBody().string() + " ");
                        } catch (IOException e) {
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<StatusResponseModel> call, Throwable t) {
                if (mRetryCounter > 0) {
                    mRetryCounter--;
                    call.clone().enqueue(this);
                    return;
                }

                Log.e("CytonError", "Request failed", t);
                if (!MainActivity.this.isDestroyed()) {
                    Toast.makeText(MainActivity.this, "Error: Failed to get status", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void setupMQTT() {
        Call<MQTTResponseModel> mqttRequest = mCytonService.setupCloudStreaming(
                new MQTTRequestModel(
                        mServerName.getText().toString(),
                        mServerUsername.getText().toString(),
                        mServerPassword.getText().toString()
                )
        );

        mRetryCounter = 3;
        mqttRequest.enqueue(new Callback<MQTTResponseModel>() {
            @Override
            public void onResponse(Call<MQTTResponseModel> call, Response<MQTTResponseModel> response) {
                if (!MainActivity.this.isDestroyed()) {
                    if (response.isSuccessful())
                        setJsonOutput();
                    else {
                        if (mRetryCounter > 0) {
                            mRetryCounter--;
                            call.clone().enqueue(this);
                            return;
                        }
                        Toast.makeText(MainActivity.this, "Failed to start cloud streaming", Toast.LENGTH_LONG).show();
                        try {
                            Log.e("CytonError", response.errorBody().string() + " ");
                        } catch (IOException e) {
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<MQTTResponseModel> call, Throwable t) {
                if (mRetryCounter > 0) {
                    mRetryCounter--;
                    call.clone().enqueue(this);
                    return;
                }

                Log.e("CytonError", "Request failed", t);
                if (!MainActivity.this.isDestroyed())
                    Toast.makeText(MainActivity.this, "Error: Failed to start cloud streaming", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setJsonOutput() {
        Call<Void> setOutputToJsonRequest = mCytonService.setOutputToJson();

        mRetryCounter = 3;
        setOutputToJsonRequest.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!MainActivity.this.isDestroyed()) {
                    if (response.isSuccessful()) {
                        if (mSdCardCheckbox.isChecked()) {
                            startRecord();
                        } else {
                            startStreamingAndOpenChannels();
                        }
                    } else {
                        if (mRetryCounter > 0) {
                            mRetryCounter--;
                            call.clone().enqueue(this);
                            return;
                        }
                        Toast.makeText(MainActivity.this, "Failed to set output to JSON", Toast.LENGTH_LONG).show();
                        try {
                            Log.e("CytonError", response.errorBody().string() + " ");
                        } catch (IOException e) {
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (mRetryCounter > 0) {
                    mRetryCounter--;
                    call.clone().enqueue(this);
                    return;
                }

                Log.e("CytonError", "Request failed", t);
                if (!MainActivity.this.isDestroyed())
                    Toast.makeText(MainActivity.this, "Error: Failed to set output to JSON", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startRecord() {
        Call<Void> recordRequest = mCytonService.executeCommand(
                new CommandRequestModel(RECORD_5_MIN_COMMAND)
        );

        mRetryCounter = 3;
        recordRequest.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!MainActivity.this.isDestroyed()) {
                    if (response.isSuccessful())
                        startStreamingAndOpenChannels();
                    else {
                        if (mRetryCounter > 0) {
                            mRetryCounter--;
                            call.clone().enqueue(this);
                            return;
                        }
                        Toast.makeText(MainActivity.this, "Failed to start recording", Toast.LENGTH_LONG).show();
                        try {
                            Log.e("CytonError", response.errorBody().string() + " ");
                        } catch (IOException e) {
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (mRetryCounter > 0) {
                    mRetryCounter--;
                    call.clone().enqueue(this);
                    return;
                }

                Log.e("CytonError", "Request failed", t);
                if (!MainActivity.this.isDestroyed())
                    Toast.makeText(MainActivity.this, "Error: Failed to start recording", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startStreamingAndOpenChannels() {
        Call<Void> attachRequest = mCytonService.executeCommand(
                new CommandRequestModel(START_STREAM_COMMAND + OPEN_CHANNELS_COMMAND)
        );

        mRetryCounter = 3;
        attachRequest.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!MainActivity.this.isDestroyed()) {
                    if (response.isSuccessful())
                        return;

                    if (mRetryCounter > 0) {
                        mRetryCounter--;
                        call.clone().enqueue(this);
                        return;
                    }
                    Toast.makeText(MainActivity.this, "Failed to start stream and open channels", Toast.LENGTH_LONG).show();
                    try {
                        Log.e("CytonError", response.errorBody().string() + " ");
                    } catch (IOException e) {
                    }

                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (mRetryCounter > 0) {
                    mRetryCounter--;
                    call.clone().enqueue(this);
                    return;
                }

                Log.e("CytonError", "Request failed", t);
                if (!MainActivity.this.isDestroyed())
                    Toast.makeText(MainActivity.this, "Error: Failed to start stream and open channels", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void checkStatus() {
        Call<StatusResponseModel> statusRequest = mCytonService.checkStatus(
                new CommandRequestModel(STATUS_COMMAND)
        );

        statusRequest.enqueue(new Callback<StatusResponseModel>() {
            @Override
            public void onResponse(Call<StatusResponseModel> call, Response<StatusResponseModel> response) {
                if (!MainActivity.this.isDestroyed()) {
                    if (response.isSuccessful()) {
                        mIsStreaming.setText(response.body().isStreaming() + "");
                        mIsRecording.setText(response.body().isRecording() + "");
                    } else {
                        mIsStreaming.setText("");
                        mIsRecording.setText("");
                    }
                }
            }

            @Override
            public void onFailure(Call<StatusResponseModel> call, Throwable t) {
                mIsStreaming.setText("");
                mIsRecording.setText("");
            }
        });
    }

    @OnClick(R.id.stop_recording_btn)
    public void onStopRecordingClick() {
        if (mDeviceSpinner.getSelectedItem() == null) {
            Toast.makeText(this, "Please select a device to stop recording.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!checkRetrofitClient()) {
            return;
        }

        checkStatusAndStop();
    }

    private void checkStatusAndStop() {
        Call<StatusResponseModel> statusRequest = mCytonService.checkStatus(
                new CommandRequestModel(STATUS_COMMAND)
        );
        mRetryCounter = 3;
        statusRequest.enqueue(new Callback<StatusResponseModel>() {
            @Override
            public void onResponse(Call<StatusResponseModel> call, Response<StatusResponseModel> response) {
                if (!MainActivity.this.isDestroyed()) {
                    if (response.isSuccessful()) {
                        if (response.body().isRecording()) {
                            stopRecordingAndStreaming(true);
                        } else if (response.body().isStreaming()) {
                            stopRecordingAndStreaming(false);
                        } else {
                            Toast.makeText(MainActivity.this, "Board is not recording", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        if (mRetryCounter > 0) {
                            mRetryCounter--;
                            call.clone().enqueue(this);
                            return;
                        }

                        Toast.makeText(MainActivity.this, "Failed to get status", Toast.LENGTH_LONG).show();
                        try {
                            Log.e("CytonError", response.errorBody().string() + " ");
                        } catch (IOException e) {
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<StatusResponseModel> call, Throwable t) {
                if (mRetryCounter > 0) {
                    mRetryCounter--;
                    call.clone().enqueue(this);
                    return;
                }

                Log.e("CytonError", "Request failed", t);
                if (!MainActivity.this.isDestroyed()) {
                    Toast.makeText(MainActivity.this, "Error: Failed to get status", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void stopRecordingAndStreaming(boolean isRecording) {
        String command = TURN_CHANNELS_OFF_COMMAND +
                STOP_STREAM_COMMAND +
                (isRecording ? STOP_RECORD_COMMAND : "");

        Call<Void> stopRecordRequest = mCytonService.executeCommand(
                new CommandRequestModel(command));
        mRetryCounter = 3;
        stopRecordRequest.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!MainActivity.this.isDestroyed()) {
                    if (response.isSuccessful())
                        return;

                    if (mRetryCounter > 0) {
                        mRetryCounter--;
                        call.clone().enqueue(this);
                        return;
                    }

                    Toast.makeText(MainActivity.this, "Failed to stop", Toast.LENGTH_LONG).show();
                    try {
                        Log.e("CytonError", response.errorBody().string() + " ");
                    } catch (IOException e) {}
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (mRetryCounter > 0) {
                    mRetryCounter--;
                    call.clone().enqueue(this);
                    return;
                }

                Log.e("CytonError", "Request failed", t);
                if (!MainActivity.this.isDestroyed()) {
                    Toast.makeText(MainActivity.this, "Error: Failed to stop", Toast.LENGTH_LONG).show();
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
        mClient.discoverServices(networkDevice, new DiscoveryListener() {    // automatically called every INTERVAL_BETWEEN_REQUESTS ms
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
        mClient.stopDiscovery();
    }
}
