package com.sd1.weegi.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.utils.ConnectionSharingAdapter;
import com.sd1.weegi.MainApplication;
import com.sd1.weegi.R;
import com.sd1.weegi.adapters.DeviceListAdapter;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by DMCar on 10/2/2017.
 */

public class DeviceConnectedFragment extends Fragment {

    @Inject
    RxBleClient mRxBleClient;

    private static final String BUNDLE_KEY_MAC_ADDRESS =
            DeviceConnectedFragment.class.getSimpleName() + ".macAddress";
    private static final long RETRY_DELAY = 100;

    private Unbinder mUnBinder;
    private Observable<RxBleConnection> mConnectionObservable;
    private PublishSubject<Void> mDisconnectTrigger;
    private RxBleDevice mDevice;

    public static DeviceConnectedFragment newInstance(String macAddress) {

        Bundle args = new Bundle();

        args.putString(BUNDLE_KEY_MAC_ADDRESS, macAddress);

        DeviceConnectedFragment fragment = new DeviceConnectedFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        ((MainApplication) this.getActivity().getApplication()).getRxBleClientComponent().inject(this);

        View v = inflater.inflate(R.layout.device_connected, container, false);
        mUnBinder = ButterKnife.bind(this, v);

        mDisconnectTrigger = PublishSubject.create();

        String macAddress = getArguments().getString(BUNDLE_KEY_MAC_ADDRESS);
        assert macAddress != null;
        mDevice = mRxBleClient.getBleDevice(macAddress);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        mConnectionObservable = mDevice.establishConnection(false)
                .takeUntil(mDisconnectTrigger)
                .retry(3)
                .retryWhen(o -> o.delay(RETRY_DELAY, TimeUnit.MILLISECONDS))
                .compose(new ConnectionSharingAdapter());
    }

    @Override
    public void onStop() {
        super.onStop();
        //unsubscribe
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnBinder.unbind();
    }
}
