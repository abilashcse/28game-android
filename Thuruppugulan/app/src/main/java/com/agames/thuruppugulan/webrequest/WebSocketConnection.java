package com.agames.thuruppugulan.webrequest;

import com.agames.thuruppugulan.ui.main.game.Player;
import com.agames.thuruppugulan.webrequest.model.request.Authenticate;
import com.agames.thuruppugulan.webrequest.model.BaseWebModel;
import com.agames.thuruppugulan.webrequest.model.request.BroadcastJoined;
import com.agames.thuruppugulan.webrequest.model.request.JoinTable;
import com.agames.thuruppugulan.webrequest.model.response.AuthResponse;
import com.agames.thuruppugulan.webrequest.model.response.JoinTableResponse;
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
        processMessage(text);
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
        Logger.e("Error in websocket creation..."+response+""+t);
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
        if (text.contains("auth")) {
            AuthResponse response = gson.fromJson(text, AuthResponse.class);
            mListener.onAuthSuccess(response);
        } else if (text.contains("joinHub")) {
            JoinTableResponse response = gson.fromJson(text, JoinTableResponse.class);
            response.tableId = hubName;
            mListener.onJoinedTable(response);
        } else if (text.contains("\"msg\":\"joined\"")){
            PlayerJoinedResponse response = gson.fromJson(text, PlayerJoinedResponse.class);
            mListener.onPlayerJoined(response);
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

    private void sendMessage(BaseWebModel auth) {
        if(webSocket!=null) {
            webSocket.send(auth.toGSON());
        }
    }

}
