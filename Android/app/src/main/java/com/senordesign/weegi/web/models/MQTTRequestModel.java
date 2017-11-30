package com.senordesign.weegi.web.models;


import com.google.gson.annotations.SerializedName;

public class MQTTRequestModel {

    @SerializedName("broker_address")
    private String mBrokerAddress;

    @SerializedName("port")
    private int mPort;

    @SerializedName("latency")
    private int mLatency;

    @SerializedName("username")
    private String mUsername;

    @SerializedName("password")
    private String mPassword;

    @SerializedName("sample_numbers")
    private boolean mSampleNumbers;

    @SerializedName("timestamps")
    private boolean mTimestamps;

    public MQTTRequestModel(String fullUrl, String username, String password) {
        String[] splitUrl = fullUrl.split(":");
        mBrokerAddress = splitUrl[0];
        if (splitUrl.length > 1)
            mPort = Integer.parseInt(splitUrl[2]);

        mUsername = username;
        mPassword = password;
    }
}
