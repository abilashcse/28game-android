package com.agames.thuruppugulan.webrequest.model.response;

import com.agames.thuruppugulan.webrequest.model.request.ChooseTrump;
import com.google.gson.annotations.SerializedName;

public class ChooseTrumpResponse extends ChooseTrump {
    @SerializedName("FROM")
    public String playerName;
}
