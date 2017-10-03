package com.sd1.weegi.utils;

import android.util.Log;

import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.scan.ScanResult;
import com.polidea.rxandroidble.scan.ScanSettings;
import com.polidea.rxandroidble.utils.ConnectionSharingAdapter;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subjects.PublishSubject;

/**
 * Created by DMCar on 9/21/2017.
 */

public class BleUtil {

    public static Subscription setupScanSubscription(RxBleClient client, Action1<ScanResult> onScanResultReceived, Action1<Throwable> onScanFailure) {
        return client
                .scanBleDevices(
                        new ScanSettings.Builder()
                                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                                .build())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onScanResultReceived, onScanFailure);
    }

    public static Subscription setupConnectionMonitor(RxBleDevice device, Action1<RxBleConnection.RxBleConnectionState> onConnectionStateChange) {
        return device.observeConnectionStateChanges()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onConnectionStateChange, BleUtil::logError);
    }

    public static void triggerDisconnect(PublishSubject<Void> subject) {
        subject.onNext(null);
    }

    public static Observable<RxBleConnection> setupConnection(RxBleDevice device, PublishSubject<Void> disconnectTrigger) {
        return device
                .establishConnection(false)
                .takeUntil(disconnectTrigger)
                .compose(new ConnectionSharingAdapter());
    }

    public static void logError(Throwable throwable) {
        Log.e(BleUtil.class.getName(), "Error", throwable);
    }
}
