package com.agames.thuruppugulan.webrequest.model.request;

import com.agames.thuruppugulan.webrequest.model.BaseWebModel;
import com.google.gson.annotations.SerializedName;

public class BroadcastJoined extends BaseWebModel {
   // {"toH":"<TableID>","msg":"joined"}
    @SerializedName("toH")
    public String tableID;
    public String msg = "joined";
}
