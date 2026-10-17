package com.agames.thuruppugulan.webrequest.model.response;

import com.agames.thuruppugulan.webrequest.model.BaseWebModel;
import com.google.gson.annotations.SerializedName;

public class JoinTableResponse extends BaseWebModel {
    //{"joinHub":"Ok"}
    @SerializedName("joinHub")
    public String status;
    public String tableId;
}
