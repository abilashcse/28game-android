package com.agames.thuruppugulan.webrequest.model.request;

import com.agames.thuruppugulan.webrequest.model.BaseWebModel;
import com.google.gson.annotations.SerializedName;

public class JoinTable extends BaseWebModel {
    //{"joinHub":"<hub name>"}
    @SerializedName("joinHub")
    public String hubName;
}
