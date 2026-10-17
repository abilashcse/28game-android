package com.agames.thuruppugulan.webrequest.model.response;

import com.agames.thuruppugulan.webrequest.model.request.BidSelection;
import com.google.gson.annotations.SerializedName;

public class BidSelectionResponse extends BidSelection {
    @SerializedName("FROM")
    public String playerName;
}
