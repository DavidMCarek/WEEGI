package com.sd1.weegi.ViewModels;

import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.scan.ScanResult;

/**
 * Created by DMCar on 9/21/2017.
 */

public class ScanResultViewModel {

    private Long mLastUpdateTime;
    private int mRssiPercent;
    private RxBleDevice mBleDevice;

    public ScanResultViewModel(ScanResult result) {
        mLastUpdateTime = System.currentTimeMillis();
        mRssiPercent = rssiToPercent(result.getRssi());
        mBleDevice = result.getBleDevice();
    }

    public void setLastUpdateTime() {
        this.mLastUpdateTime = System.currentTimeMillis();
    }

    public int getRssiPercent() {
        return mRssiPercent;
    }

    public void setRssiPercent(int rssi) {
        mRssiPercent = rssi;
    }

    public RxBleDevice getBleDevice() {
        return mBleDevice;
    }

    public long timeSinceUpdateMillis() {
        return System.currentTimeMillis() - mLastUpdateTime;
    }

    @Override
    public boolean equals(Object o) {
        if(o == null)
            return false;
        else if(o instanceof ScanResultViewModel) {
            return ((ScanResultViewModel) o).getBleDevice().getMacAddress().equalsIgnoreCase(this.getBleDevice().getMacAddress());
        }

        return false;
    }

    private static int rssiToPercent(int rssi) {
        if (rssi >= -30)
            return 100;
        if (rssi >= -60)
            return 75;
        if (rssi >= -80)
            return 50;
        if (rssi >= -90)
            return 25;
        else
            return 5;
    }
}
