package com.senordesign.weegi.web.services;


import com.senordesign.weegi.web.models.CommandRequestModel;
import com.senordesign.weegi.web.models.TcpRequestModel;
import com.senordesign.weegi.web.models.TcpResponseModel;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import rx.Observable;

public interface CytonService {

    @GET("tcp")
    Observable<TcpResponseModel> getTcpInfo();

    @POST("tcp")
    Observable<TcpResponseModel> setCommunicatingIp(@Body TcpRequestModel tcpRequestModel);

    @GET("stream/start")
    Observable<Void> startStreaming();

    @GET("stream/stop")
    Observable<Void> stopStreaming();

    @POST("command")
    Observable<Void> executeCommand(@Body CommandRequestModel commandRequestModel);
}
