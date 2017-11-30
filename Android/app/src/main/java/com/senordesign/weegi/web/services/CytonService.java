package com.senordesign.weegi.web.services;


import com.senordesign.weegi.web.models.CommandRequestModel;
import com.senordesign.weegi.web.models.MQTTRequestModel;
import com.senordesign.weegi.web.models.MQTTResponseModel;
import com.senordesign.weegi.web.models.StatusResponseModel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface CytonService {

    @POST("command")
    Call<Void> executeCommand(@Body CommandRequestModel commandRequestModel);

    @POST("mqtt")
    Call<MQTTResponseModel> setupCloudStreaming(@Body MQTTRequestModel mqttRequestModel);

    @GET("output/json")
    Call<Void> setOutputToJson();

    @POST("command")
    Call<StatusResponseModel> checkStatus(@Body CommandRequestModel commandRequestModel);
}
