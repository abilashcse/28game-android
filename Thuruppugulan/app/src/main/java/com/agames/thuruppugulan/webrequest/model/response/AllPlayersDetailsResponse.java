package com.agames.thuruppugulan.webrequest.model.response;

import com.agames.thuruppugulan.webrequest.model.request.AllPlayersDetails;
import com.google.gson.annotations.SerializedName;

public class AllPlayersDetailsResponse extends AllPlayersDetails {

    @SerializedName("FROM")
    public String playerName;
}
