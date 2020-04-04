package com.agames.thuruppugulan.webrequest.model.response;

import com.agames.thuruppugulan.webrequest.model.request.PlayerDetails;
import com.google.gson.annotations.SerializedName;

public class PlayerDetailResponse extends PlayerDetails {
    @SerializedName("FROM")
    public String playerName;
}
