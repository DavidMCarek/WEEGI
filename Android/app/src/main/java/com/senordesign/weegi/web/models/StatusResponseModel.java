package com.senordesign.weegi.web.models;


import com.google.gson.annotations.SerializedName;

public class StatusResponseModel {

    @SerializedName("streaming")
    private boolean mIsStreaming;

    @SerializedName("recording")
    private boolean mIsRecording;

    public StatusResponseModel(boolean isStreaming, boolean isRecording) {
        mIsStreaming = isStreaming;
        mIsRecording = isRecording;
    }

    public boolean isStreaming() {
        return mIsStreaming;
    }

    public boolean isRecording() {
        return mIsRecording;
    }
}
