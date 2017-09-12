package com.sd1.weegi;

import android.app.Application;

import com.sd1.weegi.dagger.AppModule;
import com.sd1.weegi.dagger.DaggerRxBleClientComponent;
import com.sd1.weegi.dagger.RxBleClientComponent;

/**
 * Created by DMCar on 9/12/2017.
 */

public class MainApplication extends Application {

    private RxBleClientComponent mRxBleClientComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        mRxBleClientComponent = DaggerRxBleClientComponent.builder()
                .appModule(new AppModule(this))
                .build();
    }

    public RxBleClientComponent getRxBleClientComponent() {
        return mRxBleClientComponent;
    }

}
