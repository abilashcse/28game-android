package com.agames.thuruppugulan.webrequest.model.request;

import com.agames.thuruppugulan.ui.main.game.Player;
import com.agames.thuruppugulan.webrequest.model.BaseWebModel;
import com.google.gson.annotations.SerializedName;


public class PlayerDetails extends BaseWebModel {
   // {"toH":"<TableID>","msg":"player_details"}
   @SerializedName("toH")
   public String tableID;
   public String msg = "player_details";
   public Player[] players;
}
