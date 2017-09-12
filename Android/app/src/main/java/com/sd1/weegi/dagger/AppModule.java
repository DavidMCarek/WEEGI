package com.sd1.weegi.dagger;

import android.app.Application;
import android.content.Context;

import com.polidea.rxandroidble.RxBleClient;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by DMCar on 9/12/2017.
 */

@Module
public class AppModule {

    Application mApplication;

    public AppModule(Application application) {
        mApplication = application;
    }

    @Provides
    @Singleton
    Application providesApplication() {
        return mApplication;
    }

    @Provides
    Context provideContext(Application application) {
        return application.getApplicationContext();
    }

    @Provides
    @Singleton
    public static RxBleClient provideRxBleClient(Context context) {
        return RxBleClient.create(context);
    }
}