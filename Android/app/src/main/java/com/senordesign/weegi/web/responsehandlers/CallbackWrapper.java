package com.senordesign.weegi.web.responsehandlers;

import android.util.Log;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by cocare on 12/5/2017.
 */

public abstract class CallbackWrapper<T> implements Callback<T> {

    private int mRetryCount = 3;

    public abstract void onResponseSuccess(T responseBody);
    public abstract void onResponseFailure();
    public abstract void onResponseError();

    public void setRetryLimit(int retryCount) {
        mRetryCount = retryCount;
    }

    private void logErrorResponse(ResponseBody responseBody) {
        try {
            Log.e("WebCallback", responseBody.string());
        } catch (IOException e) {}
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (response.isSuccessful()) {
            onResponseSuccess(response.body());
        } else if (mRetryCount > 0) {
            mRetryCount--;
            call.clone().enqueue(this);
        } else {
            onResponseFailure();
            logErrorResponse(response.errorBody());
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        if (mRetryCount > 0) {
            mRetryCount--;
            call.clone().enqueue(this);
        }

        onResponseError();
        Log.e("WebCallback", "Request failed", t);
    }
}
