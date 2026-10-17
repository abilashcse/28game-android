package com.agames.thuruppugulan.webrequest.model.request;

import com.agames.thuruppugulan.ui.main.game.Player;
import com.agames.thuruppugulan.webrequest.model.BaseWebModel;
import com.google.gson.annotations.SerializedName;

public class PlayerDetails extends BaseWebModel {
    @SerializedName("toH")
    public String tableID;
    public String msg = "single_player_details";
    public Player player;
}
