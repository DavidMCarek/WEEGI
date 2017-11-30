package com.senordesign.weegi.web.models;


import com.google.gson.annotations.SerializedName;

public class MQTTResponseModel {

    @SerializedName("broker_address")
    private String mBrokerAddress;

    @SerializedName("latency")
    private int mLatency;

    @SerializedName("username")
    private String mUsername;

    @SerializedName("connected")
    private boolean mConnected;

    @SerializedName("output")
    private String mOutput;

    public MQTTResponseModel(String brokerAddress, int latency, String username, boolean connected, String output) {
        mBrokerAddress = brokerAddress;
        mLatency = latency;
        mUsername = username;
        mConnected = connected;
        mOutput = output;
    }

    public String getBrokerAddress() {
        return mBrokerAddress;
    }

    public int getLatency() {
        return mLatency;
    }

    public String getUsername() {
        return mUsername;
    }

    public boolean isConnected() {
        return mConnected;
    }

    public String getOutput() {
        return mOutput;
    }
}
