package com.sd1.weegi.fragments;

import android.app.ListFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.exceptions.BleScanException;
import com.polidea.rxandroidble.scan.ScanResult;
import com.polidea.rxandroidble.scan.ScanSettings;
import com.sd1.weegi.MainApplication;
import com.sd1.weegi.R;
import com.sd1.weegi.adapters.DeviceListAdapter;
import com.sd1.weegi.wrappers.ScanResultWrapper;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by DMCar on 9/14/2017.
 */

public class ScannerFragment extends ListFragment {

    @Inject
    RxBleClient mRxBleClient;

    @BindView(android.R.id.progress)
    View mProgressSpinner;

    Unbinder mUnBinder;

    private static final long DEVICE_UPDATE_TIME_NANOS = 1000000000L;
    private static final long DEVICE_REMOVAL_TIME_NANOS = DEVICE_UPDATE_TIME_NANOS * 20L;

    private Subscription mScanSubscription;
    private DeviceListAdapter mAdapter;

    public static ScannerFragment newInstance() {
        Bundle args = new Bundle();
        ScannerFragment fragment = new ScannerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((MainApplication) this.getActivity().getApplication()).getRxBleClientComponent().inject(this);

        View v = inflater.inflate(R.layout.scanner_fragment, container, false);
        mUnBinder = ButterKnife.bind(this, v);

        mAdapter = new DeviceListAdapter(getContext(), R.layout.device_row);
        mAdapter.setNotifyOnChange(false);

        setListAdapter(mAdapter);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        mScanSubscription = mRxBleClient.scanBleDevices(
                new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build()
        )
                .doOnUnsubscribe(() -> mScanSubscription = null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::updateScanResults, this::onScanFailure);
    }

    private void onScanFailure(Throwable throwable) {
        if (throwable instanceof BleScanException) {
            handleBleScanException((BleScanException) throwable);
        }
    }

    private void handleBleScanException(BleScanException bleScanException) {

        switch (bleScanException.getReason()) {
            case BleScanException.BLUETOOTH_NOT_AVAILABLE:
                Toast.makeText(getContext(), "Bluetooth is not available", Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.BLUETOOTH_DISABLED:
                Toast.makeText(getContext(), "Enable bluetooth and try again", Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.LOCATION_PERMISSION_MISSING:
                Toast.makeText(getContext(), "On Android 6.0 location permission is required. Implement Runtime Permissions", Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.LOCATION_SERVICES_DISABLED:
                Toast.makeText(getContext(), "Location services needs to be enabled on Android 6.0", Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.BLUETOOTH_CANNOT_START:
            default:
                Toast.makeText(getContext(), "Unable to start scanning", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void updateScanResults(ScanResult scanResult) {
        ScanResultWrapper result = new ScanResultWrapper(scanResult);
        boolean dataChange = false;

        int position = mAdapter.getPosition(result);
        if (position == -1) {
            mAdapter.add(result);
            dataChange = true;
            mProgressSpinner.setVisibility(View.GONE);
        }
        else {
            ScanResultWrapper item = mAdapter.getItem(position);
            assert item != null;
            if (item.elapsedTimeNanos() > DEVICE_UPDATE_TIME_NANOS) {
                item = result;
                dataChange = true;
            }
        }

        mAdapter.sort((lhs, rhs) -> -Integer.compare(lhs.getRssi(), rhs.getRssi()));

        dataChange |= removeExpiredDevices();

        if (dataChange) {
            mAdapter.notifyDataSetChanged();
        }

    }

    private boolean removeExpiredDevices() {
        boolean dataChange = false;

        for (int i = 0; i < mAdapter.getCount(); i++) {
            ScanResultWrapper item = mAdapter.getItem(i);
            assert item != null;
            if (item.elapsedTimeNanos() > DEVICE_REMOVAL_TIME_NANOS) {
                mAdapter.remove(mAdapter.getItem(i));
                dataChange = true;
            }
        }

        if (mAdapter.getCount() == 0)
            mProgressSpinner.setVisibility(View.VISIBLE);

        return dataChange;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

    }

    @Override
    public void onStop() {
        super.onStop();
        mScanSubscription.unsubscribe();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnBinder.unbind();
    }
}
