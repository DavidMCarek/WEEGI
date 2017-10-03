package com.sd1.weegi.fragments;

import android.app.ListFragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.exceptions.BleScanException;
import com.polidea.rxandroidble.scan.ScanResult;
import com.sd1.weegi.MainActivity;
import com.sd1.weegi.MainApplication;
import com.sd1.weegi.R;
import com.sd1.weegi.utils.BleUtil;
import com.sd1.weegi.viewmodels.ScanResultViewModel;
import com.sd1.weegi.adapters.DeviceListAdapter;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Subscription;

/**
 * Created by DMCar on 9/14/2017.
 */

public class ScannerFragment extends ListFragment {

    @Inject
    RxBleClient mRxBleClient;

    @BindView(android.R.id.progress)
    View mProgressSpinner;

    Unbinder mUnBinder;

    private static final long DEVICE_UPDATE_TIME_MILLIS = 1000L;
    private static final long DEVICE_REMOVAL_TIME_MILLIS = 20000L;
    private static final long UPDATE_LIST_TIME_MILLIS = 3000L;

    private Subscription mScanSubscription;
    private DeviceListAdapter mAdapter;
    private Handler mListHandler;
    private Runnable mRemoveExpiredDevicesTimer;
    private boolean mUpdatingList;

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
        mScanSubscription = BleUtil.setupScanSubscription(mRxBleClient, this::updateScanResults, this::onScanFailure);
        setupDeviceRemovalTimer();
    }

    private void setupDeviceRemovalTimer() {
        mListHandler = new Handler();
        mRemoveExpiredDevicesTimer = new Runnable() {
            @Override
            public void run() {
                if (!mUpdatingList && removeExpiredDevices())
                    mAdapter.notifyDataSetChanged();

                mListHandler.postDelayed(this, UPDATE_LIST_TIME_MILLIS);
            }
        };

        mListHandler.postDelayed(mRemoveExpiredDevicesTimer,  UPDATE_LIST_TIME_MILLIS);
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
        mUpdatingList = true;

        ScanResultViewModel result = new ScanResultViewModel(scanResult);
        boolean dataChange = false;

        int position = mAdapter.getPosition(result);
        if (position == -1) {
            mAdapter.add(result);
            dataChange = true;
            mProgressSpinner.setVisibility(View.GONE);
        }
        else {
            ScanResultViewModel item = mAdapter.getItem(position);
            assert item != null;
            if (item.timeSinceUpdateMillis() > DEVICE_UPDATE_TIME_MILLIS) {
                item.setLastUpdateTime();
                item.setRssiPercent(result.getRssiPercent());
                dataChange = true;
            }
        }

        if (dataChange) {
            mAdapter.notifyDataSetChanged();
        }

        mUpdatingList = false;
    }

    private boolean removeExpiredDevices() {
        boolean dataChange = false;
        int adapterCount = mAdapter.getCount();
        for (int i = 0; i < adapterCount; i++) {
            ScanResultViewModel item = mAdapter.getItem(i);
            assert item != null;
            if (item.timeSinceUpdateMillis() > DEVICE_REMOVAL_TIME_MILLIS) {
                mAdapter.remove(mAdapter.getItem(i));
                i--;
                adapterCount--;
                dataChange = true;
            }
        }

        if (mAdapter.getCount() == 0)
            mProgressSpinner.setVisibility(View.VISIBLE);

        return dataChange;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        RxBleDevice d = ((DeviceListAdapter.ViewHolder)v.getTag()).getDevice();
        ((MainActivity) getActivity()).showDeviceConnectedFragment(d.getMacAddress());
    }

    @Override
    public void onStop() {
        super.onStop();
        mListHandler.removeCallbacks(mRemoveExpiredDevicesTimer);
        mScanSubscription.unsubscribe();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnBinder.unbind();
    }
}