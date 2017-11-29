package com.senordesign.weegi.web.services;


import com.senordesign.weegi.web.models.CommandRequestModel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface CytonService {

    @GET("stream/start")
    Call<Void> startStreaming();

    @GET("stream/stop")
    Call<Void> stopStreaming();

    @POST("command")
    Call<Void> executeCommand(@Body CommandRequestModel commandRequestModel);
}
