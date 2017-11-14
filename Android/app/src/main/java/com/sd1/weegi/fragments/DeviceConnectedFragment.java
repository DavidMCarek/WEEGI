package com.sd1.weegi.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.exceptions.BleGattException;
import com.sd1.weegi.Constants;
import com.sd1.weegi.MainActivity;
import com.sd1.weegi.MainApplication;
import com.sd1.weegi.R;
import com.sd1.weegi.utils.BleUtil;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

import static com.polidea.rxandroidble.RxBleConnection.RxBleConnectionState.CONNECTED;
import static com.polidea.rxandroidble.RxBleConnection.RxBleConnectionState.DISCONNECTED;

/**
 * Created by DMCar on 10/2/2017.
 */

public class DeviceConnectedFragment extends Fragment {

    public static final String TAG = DeviceConnectedFragment.class.getName();

    @Inject
    RxBleClient mRxBleClient;

    private static final String BUNDLE_KEY_MAC_ADDRESS = DeviceConnectedFragment.class.getSimpleName() + ".macAddress";

    private Unbinder mUnBinder;
    private Observable<RxBleConnection> mConnectionObservable;
    private PublishSubject<Void> mDisconnectTrigger;
    private RxBleDevice mDevice;
    private Subscription mStateSubscription;
    private boolean mIsConnected;
    private boolean mKeepConnectionAlive;

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

        mIsConnected = false;
        mKeepConnectionAlive = true;

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        mDevice.observeConnectionStateChanges().subscribe();
        mStateSubscription = BleUtil.setupConnectionMonitor(mDevice, this::onConnectionStateChange);
        mConnectionObservable = BleUtil.setupConnection(mDevice, mDisconnectTrigger);
        mConnectionObservable.subscribe(ignore -> {}, this::handleConnectionError);
    }

    private void onConnectionStateChange(RxBleConnection.RxBleConnectionState newState) {
        if (newState == CONNECTED) {
            mConnectionObservable
                    .flatMap(rxBleConnection -> rxBleConnection
                    .writeCharacteristic(Constants.RFduinoService.Characteristics.UUID_WRITE, new byte[]{0x00}))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(ignore -> {
                        readData();
                    }, BleUtil::logError);
            mIsConnected = true;
        } else if (newState == DISCONNECTED && mIsConnected) {
            ((MainActivity) getActivity()).resetAppState();
        }
    }

    private void readData() {
        mConnectionObservable
                .flatMap(rxBleConnection -> rxBleConnection
                .readCharacteristic(Constants.RFduinoService.Characteristics.UUID_READ))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bytes -> {

                    Log.i(DeviceConnectedFragment.class.getName(), "read res");
                }, BleUtil::logError);
    }

    private void handleConnectionError(Throwable throwable) {
        if (throwable instanceof BleGattException) {
            Toast.makeText(getContext(), "Failed to connect to device", Toast.LENGTH_SHORT).show();
            ((MainActivity)getActivity()).resetAppState();
        } else {
            BleUtil.logError(throwable);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!mKeepConnectionAlive) {
            BleUtil.triggerDisconnect(mDisconnectTrigger);
        }
        mStateSubscription.unsubscribe();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnBinder.unbind();
    }
}
