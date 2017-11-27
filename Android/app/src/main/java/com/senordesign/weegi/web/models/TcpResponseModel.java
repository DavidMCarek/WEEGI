package com.senordesign.weegi.web.models;


import com.google.gson.annotations.SerializedName;

public class TcpResponseModel {

    @SerializedName("connected")
    public boolean isConnected;

    @SerializedName("delimiter")
    public boolean isDelimited;

    @SerializedName("ip_address")
    public String ipAddress;

    @SerializedName("output")
    public String output;

    @SerializedName("port")
    public int port;

    @SerializedName("latency")
    public int latency;
}
