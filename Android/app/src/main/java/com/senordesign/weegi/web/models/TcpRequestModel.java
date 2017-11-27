package com.senordesign.weegi.web.models;


import com.google.gson.annotations.SerializedName;

public class TcpRequestModel {

    @SerializedName("delimiter")
    public boolean isDelimited;

    @SerializedName("ip")
    public String ipAddress;

    @SerializedName("latency")
    public int latency;

    @SerializedName("output")
    public String output;

    @SerializedName("port")
    public int port;

    @SerializedName("sample_numbers")
    public boolean sampleNumbers;

    @SerializedName("timestamps")
    public boolean timestamps;
}
