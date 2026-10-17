package com.agames.thuruppugulan.webrequest.model.response;

import com.agames.thuruppugulan.webrequest.model.request.BroadcastJoined;
import com.google.gson.annotations.SerializedName;

public class PlayerJoinedResponse extends BroadcastJoined {
    //RECV:{"toH":"<TableID>","msg":"joined","sID":62,"FROM":"<Player>"}
    @SerializedName("FROM")
    public String playerName;
}
