package com.agames.thuruppugulan.webrequest.model;

import com.google.gson.Gson;

public class BaseWebModel {
    public String toGSON() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
