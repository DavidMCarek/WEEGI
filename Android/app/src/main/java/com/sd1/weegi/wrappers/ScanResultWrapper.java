package com.sd1.weegi.wrappers;

import com.polidea.rxandroidble.scan.ScanResult;

/**
 * Created by DMCar on 9/14/2017.
 */

public class ScanResultWrapper extends ScanResult {

    public ScanResultWrapper(ScanResult result) {
        super(result.getBleDevice(), result.getRssi(), result.getTimestampNanos(), result.getCallbackType(), result.getScanRecord());
    }

    @Override
    public boolean equals(Object o) {
        if(o == null)
            return false;
        else if(o instanceof ScanResultWrapper) {
            return ((ScanResultWrapper) o).getBleDevice().getMacAddress().equalsIgnoreCase(this.getBleDevice().getMacAddress());
        }

        return false;
    }

    public long elapsedTimeNanos() {
        return System.nanoTime() - this.getTimestampNanos();
    }
}