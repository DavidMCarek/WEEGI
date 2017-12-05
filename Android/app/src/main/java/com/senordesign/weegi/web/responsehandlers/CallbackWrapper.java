package com.senordesign.weegi.web.responsehandlers;

import android.app.Activity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by cocare on 12/5/2017.
 */

public abstract class CallbackWrapper<T> implements Callback<T> {

    private int mRetryCount = 3;
    private Activity mContext;

    public abstract void onResponseSuccess(T responseBody);
    public abstract void onResponseFailure(Call<T> call);
    public abstract void onResponseError();

    public void setRetryLimit(int retryCount) {
        mRetryCount = retryCount;
    }

    public void setContext(Activity context) {
        mContext = context;
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (response.isSuccessful()) {
            onResponseSuccess(response.body());
        } else if (mRetryCount > 0) {
            mRetryCount--;
            call.clone().enqueue(this);
        } else {
            onResponseFailure(call);
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        onResponseError();
    }
}
