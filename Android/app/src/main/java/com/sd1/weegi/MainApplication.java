package com.sd1.weegi;

import android.app.Application;

import com.sd1.weegi.dagger.AppModule;
import com.sd1.weegi.dagger.DaggerRxBleClientComponent;

/**
 * Created by DMCar on 9/12/2017.
 */

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
    }

}
