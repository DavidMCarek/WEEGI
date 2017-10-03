package com.sd1.weegi.dagger;

import com.sd1.weegi.MainActivity;
import com.sd1.weegi.fragments.DeviceConnectedFragment;
import com.sd1.weegi.fragments.ScannerFragment;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by DMCar on 9/12/2017.
 */

@Singleton
@Component(modules = {AppModule.class})
public interface RxBleClientComponent {
    void inject(MainActivity activity);
    void inject(ScannerFragment fragment);
    void inject(DeviceConnectedFragment fragment);
}