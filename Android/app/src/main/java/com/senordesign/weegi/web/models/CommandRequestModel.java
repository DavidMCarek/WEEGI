package com.senordesign.weegi.web.models;


import com.google.gson.annotations.SerializedName;

public class CommandRequestModel {

    @SerializedName("command")
    public String command;

    public CommandRequestModel(String command) {
        this.command = command;
    }
}
