package com.senordesign.weegi;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

import com.senordesign.weegi.web.services.CytonService;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private Retrofit mRetrofit;
    private CytonService mCytonService;

    @BindView(R.id.ip_address_value)
    protected EditText mIpAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.start_recording_btn)
    public void onStartRecordingClick() {
        checkRetrofitClient();
    }

    @OnClick(R.id.stop_recording_btn)
    public void onStopRecordingClick() {
        checkRetrofitClient();
    }

    private void checkRetrofitClient() {
        if (mRetrofit == null)
            initializeRetrofitClient();

        if (!mRetrofit.baseUrl().toString().contains(mIpAddress.getText()))
            initializeRetrofitClient();
    }

    private void initializeRetrofitClient() {
        mRetrofit = new Retrofit.Builder()
                .baseUrl("http://" + mIpAddress.getText() + "/")
                .build();

        mCytonService = mRetrofit.create(CytonService.class);
    }
}
