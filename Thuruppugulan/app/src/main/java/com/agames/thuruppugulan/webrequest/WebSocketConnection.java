package com.agames.thuruppugulan.webrequest;

import com.agames.thuruppugulan.ui.main.game.Player;
import com.agames.thuruppugulan.webrequest.model.request.Authenticate;
import com.agames.thuruppugulan.webrequest.model.BaseWebModel;
import com.agames.thuruppugulan.webrequest.model.request.BroadcastJoined;
import com.agames.thuruppugulan.webrequest.model.request.JoinTable;
import com.agames.thuruppugulan.webrequest.model.request.AllPlayersDetails;
import com.agames.thuruppugulan.webrequest.model.request.PlayerDetails;
import com.agames.thuruppugulan.webrequest.model.response.AuthResponse;
import com.agames.thuruppugulan.webrequest.model.response.BidSelectionResponse;
import com.agames.thuruppugulan.webrequest.model.response.JoinTableResponse;
import com.agames.thuruppugulan.webrequest.model.response.AllPlayersDetailsResponse;
import com.agames.thuruppugulan.webrequest.model.response.PlayerDetailResponse;
import com.agames.thuruppugulan.webrequest.model.response.PlayerJoinedResponse;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketConnection extends WebSocketListener {

    private Request webSocketRequest;
    private WebSocket webSocket;
    private String hubName;

    private static WebSocketConnection connection;

    public enum Reason {
        AUTH_FAIL
    }
    public interface OnWebSocketListener{
        void onConnected();
        void onAuthSuccess(AuthResponse response );
        void onJoinedTable(JoinTableResponse response);
        void onPlayerJoined(PlayerJoinedResponse response);
        void onPlayerDetailsReceived(AllPlayersDetailsResponse response);
        void onAllPlayersJoined(AllPlayersDetailsResponse response);
        void onUserShufflingCards(PlayerDetailResponse response);
        void onFirstSetCardsReceived(AllPlayersDetailsResponse response);
        void onSelectBid(BidSelectionResponse response);
        void onSecondSetCardsReceived(AllPlayersDetailsResponse response);
        void onFailure(Reason failureReason, Throwable throwable);
    }

    private OnWebSocketListener mListener;

    public static synchronized WebSocketConnection getInstance() {

        if (connection == null) {
            connection = new WebSocketConnection();
        }
        return connection;
    }

    public void connect(String instanceID) {
        Logger.d("Connecting to websocket");
        webSocketRequest = new Request.Builder().url("wss://cloud.achex.ca/"+instanceID)
                .addHeader("Connection","close")
                .addHeader("content-type", "application/json")
                .addHeader("Origin", "*")
                .build();

        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
        client.newWebSocket(webSocketRequest, this);
        client.dispatcher().executorService().shutdown();
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        this.webSocket = webSocket;
        Logger.d("Websocket opened " + response.message());
        if (mListener != null) {
            mListener.onConnected();
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        Logger.d("RECV->"+text);
        if (text!=null) {
            processMessage(text);
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        Logger.d("RECV->"+bytes);
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        super.onClosing(webSocket, code, reason);
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        super.onClosed(webSocket, code, reason);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
        super.onFailure(webSocket, t, response);
        Logger.e("Error in websocket creation..."+response);
        t.printStackTrace();
        if (mListener != null) {
            mListener.onFailure(Reason.AUTH_FAIL, t);
        }
    }


    public void setGameListener(OnWebSocketListener mListener) {
        this.mListener = mListener;
    }


    public void close() {
        if (this.webSocket != null) {
            this.webSocket.close(1000,"Exit");
        }
    }



    private void processMessage(String text) {
        Gson gson = new Gson();
        Logger.d("processMessage");
        if (text.contains("auth")) {
            AuthResponse response = gson.fromJson(text, AuthResponse.class);
            mListener.onAuthSuccess(response);
        } else if (text.contains("joinHub")) {
            JoinTableResponse response = gson.fromJson(text, JoinTableResponse.class);
            response.tableId = hubName;
            mListener.onJoinedTable(response);
        } else if (text.contains("joined")){
            PlayerJoinedResponse response = gson.fromJson(text, PlayerJoinedResponse.class);
            mListener.onPlayerJoined(response);
        } else if(text.contains("player_details")) {
            AllPlayersDetailsResponse response = gson.fromJson(text, AllPlayersDetailsResponse.class);
            mListener.onPlayerDetailsReceived(response);
        }else if(text.contains("all_players_connected")) {
            AllPlayersDetailsResponse response = gson.fromJson(text, AllPlayersDetailsResponse.class);
            mListener.onAllPlayersJoined(response);
        }else if(text.contains("send_first_set_cards")) {
            AllPlayersDetailsResponse response = gson.fromJson(text, AllPlayersDetailsResponse.class);
            mListener.onFirstSetCardsReceived(response);
        }else if(text.contains("send_second_set_cards")) {
            AllPlayersDetailsResponse response = gson.fromJson(text, AllPlayersDetailsResponse.class);
            mListener.onSecondSetCardsReceived(response);
        }else if(text.contains("send_shuffling_event")) {
            PlayerDetailResponse response = gson.fromJson(text, PlayerDetailResponse.class);
            mListener.onUserShufflingCards(response);
        }else if(text.contains("send_select_bid")) {
            BidSelectionResponse response = gson.fromJson(text, BidSelectionResponse.class);
            mListener.onSelectBid(response);
        }

    }


    public void auth(String username) {
        Authenticate auth = new Authenticate();
        auth.auth = username;
        auth.passwd="wtwtipoti09ij";
        sendMessage(auth);
    }

    //table is a hub
    public void joinTable(String tableName) {
        JoinTable hub = new JoinTable();
        hub.hubName = tableName;
        this.hubName = tableName;
        sendMessage(hub);
    }

    public void broadCastJoined(String tableName) {
        BroadcastJoined broadcast = new BroadcastJoined();
        broadcast.tableID = tableName;
        sendMessage(broadcast);
    }

    public void broadCastPlayerDetails(Player[] players) {
        Logger.d("broadCastPlayerDetails");
        AllPlayersDetails allPlayersDetails = new AllPlayersDetails();
        allPlayersDetails.tableID = this.hubName;
        allPlayersDetails.players = players;
        sendMessage(allPlayersDetails);
    }

    public void broadCastAllPlayerJoined(Player[] players) {
        Logger.d("broadCastAllPlayerJoined");
        AllPlayersDetails allPlayersDetails = new AllPlayersDetails();
        allPlayersDetails.msg="all_players_connected";
        allPlayersDetails.tableID = this.hubName;
        allPlayersDetails.players = players;
        sendMessage(allPlayersDetails);
    }

    public void sendFirstSetCards(Player[] players) {
        Logger.d("sendFirstSetCards");
        AllPlayersDetails allPlayersDetails = new AllPlayersDetails();
        allPlayersDetails.msg="send_first_set_cards";
        allPlayersDetails.tableID = this.hubName;
        allPlayersDetails.players = players;
        sendMessage(allPlayersDetails);
    }

    public void sendSecondSetCards(Player[] players) {
        Logger.d("sendSecondSetCards");
        AllPlayersDetails allPlayersDetails = new AllPlayersDetails();
        allPlayersDetails.msg="send_second_set_cards";
        allPlayersDetails.tableID = this.hubName;
        allPlayersDetails.players = players;
        sendMessage(allPlayersDetails);
    }
    public void sendShufflingEvent(Player me) {
        Logger.d("sendSecondSetCards");
        PlayerDetails playerDetails = new PlayerDetails();
        playerDetails.msg="send_shuffling_event";
        playerDetails.tableID = this.hubName;
        playerDetails.player= me;
        sendMessage(playerDetails);
    }

    public void sendSelectBid(Player player, boolean canPass) {
        Logger.d("sendSelectBid "+player + "canPass = "+canPass);
        PlayerDetails playerDetails = new PlayerDetails();
        playerDetails.msg="send_select_bid";
        playerDetails.tableID = this.hubName;
        playerDetails.player= player;
        sendMessage(playerDetails);
    }

    private void sendMessage(BaseWebModel auth) {
        if(webSocket!=null) {
            String message = auth.toGSON();
            Logger.json(message);
            webSocket.send(message);
        }
    }

}
