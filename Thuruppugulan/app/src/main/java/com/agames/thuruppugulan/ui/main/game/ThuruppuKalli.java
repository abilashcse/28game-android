package com.agames.thuruppugulan.ui.main.game;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;

import androidx.cardview.widget.CardView;

import com.agames.thuruppugulan.databinding.TableFragmentBinding;
import com.agames.thuruppugulan.model.GameUser;
import com.agames.thuruppugulan.ui.main.GameState;
import com.agames.thuruppugulan.ui.main.utils.ViewUtils;
import com.agames.thuruppugulan.webrequest.WebSocketConnection;
import com.agames.thuruppugulan.webrequest.model.response.AuthResponse;
import com.agames.thuruppugulan.webrequest.model.response.BidSelectionResponse;
import com.agames.thuruppugulan.webrequest.model.response.JoinTableResponse;
import com.agames.thuruppugulan.webrequest.model.response.AllPlayersDetailsResponse;
import com.agames.thuruppugulan.webrequest.model.response.PlayerDetailResponse;
import com.agames.thuruppugulan.webrequest.model.response.PlayerJoinedResponse;
import com.orhanobut.logger.Logger;

import java.util.Random;

public class ThuruppuKalli implements WebSocketConnection.OnWebSocketListener {

    private TableFragmentViewModel viewModel;

    private final TableFragmentBinding ui;
    private final Activity uiActivity;
    private CardView[] userCardViews;
    private CardView[] tableCards;

    public interface OnGameListener {
        void onCreatedTable(String tableId);
        void onJoinedTable(String tableId);
        void selectBid(Player player, boolean canPass);
        void chooseTrump(Player player);
        void onGameCreationFailure(Throwable throwable);
    }

    private OnGameListener mGameListener;
    public GameState state;
    private static final String STAGE_WS_INSTANCE = "00001023";
    private static final String TEST_TABLE_ID = "6542";
    public static boolean NO_SOCKET;
    private WebSocketConnection mWebSocket;

    private int noOfPlayers;

    // Position goes like
    // 1 - 2 - 3 - 4
    // 2 - 3 - 4 - 1
    // 3 - 4 - 1 - 2
    // 4 - 1 - 2 - 3
    public int myPosition;

    private boolean isUserShufflingEventSend;

    public ThuruppuKalli(Activity activity,
                         TableFragmentBinding ui,
                         int myPosition,
                         TableFragmentViewModel viewModel,
                         OnGameListener listener) {
        this.uiActivity = activity;
        this.ui = ui;
        this.myPosition = myPosition;
        this.viewModel = viewModel;
        this.mGameListener = listener;
        initCardObjects(ui);
        state = GameState.INIT;
        mWebSocket = WebSocketConnection.getInstance();
        mWebSocket.setGameListener(this);
    }

    private void initCardObjects(com.agames.thuruppugulan.databinding.TableFragmentBinding ui) {
        userCardViews = new CardView[]{
                ui.cardsLayout.userCard1,
                ui.cardsLayout.userCard2,
                ui.cardsLayout.userCard3,
                ui.cardsLayout.userCard4,
                ui.cardsLayout.userCard5,
                ui.cardsLayout.userCard6,
                ui.cardsLayout.userCard7,
                ui.cardsLayout.userCard8
        };

        tableCards = new CardView[] {
                ui.table.player1Card,
                ui.table.player2Card,
                ui.table.player3Card,
                ui.table.player4Card
        };
        for (CardView cardView: userCardViews) {
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }
        clearViews();
    }

    private void clearViews() {
        for (int i = 0; i < 8; i++) {
            userCardViews[i].setVisibility(View.GONE);
        }
        for (int i = 0; i < 4; i++) {
            tableCards[i].setVisibility(View.GONE);
        }
    }

    public void drawFirstSet() {

    }

    public void shuffleDeck() {
        Logger.d("state = " + state);
        if (state != GameState.FRIENDS_JOINED) {
            ViewUtils.showToast(ui.getRoot().getContext(), "Can't shuffle card.. Waiting for friends");
            return;
        }
        if (viewModel.players[myPosition].isDealer) {
            viewModel.shuffleDeck();
            if (!isUserShufflingEventSend) {
                isUserShufflingEventSend = true;
                mWebSocket.sendShufflingEvent(viewModel.me);
            }
        } else {
            ViewUtils.showToast(ui.getRoot().getContext(), "Only Dealer can shuffle cards");
        }
    }

    public void drawCards() {
        if (state == GameState.FRIENDS_JOINED || state == GameState.DRAWING_CARD_FIRST4) {

            if (viewModel.players[myPosition].isDealer) {
                if (state == GameState.FRIENDS_JOINED) {
                    state = GameState.DRAWING_CARD_FIRST4;
                } else if (state == GameState.DRAWING_CARD_FIRST4) {
                    state = GameState.DRAWING_CARD_LAST4;
                }
                if (state == GameState.DRAWING_CARD_FIRST4) {
                    viewModel.drawSet();
                    updateUIAfterFirstSet();
                    mWebSocket.sendFirstSetCards(viewModel.players);
                    state = GameState.FIRST_BID_SELECTION;
                    sendBid(false);
                } else if (state == GameState.DRAWING_CARD_LAST4) {
                    viewModel.drawSet();
                    mWebSocket.sendSecondSetCards(viewModel.players);
                    updateUIAfterSecondSet();
                }
            } else {
                ViewUtils.showToast(ui.getRoot().getContext(), "Only Dealer can draw cards");
            }
        } else {
            ViewUtils.showToast(ui.getRoot().getContext(), "Can't draw cards");
            return;
        }
    }

    private void sendBid(boolean canPass) {
        int nextPosition = viewModel.getMyPosition();
        if (!canPass) {
            nextPosition = getNextBidPosition(nextPosition);
        } else {
        }
        mWebSocket.sendSelectBid(viewModel.players, nextPosition, canPass);
    }

    private int getNextBidPosition(int nextPosition) {
        //First call
        if (nextPosition == 3) {
            nextPosition = 0;
        } else {
            nextPosition++;
        }
        return nextPosition;
    }


    public void nextBid(boolean passed) {
        int myPosition = viewModel.getMyPosition();
        int nextPosition = -1;
        if (passed) {
            if (myPosition == 0) {
                nextPosition = 2;
            } else if (myPosition == 1) {
                nextPosition = 3;
            }else if (myPosition == 2) {
                nextPosition = 0;
            }else if (myPosition == 3) {
                nextPosition = 1;
            }
        } else {
            nextPosition = getNextBidPosition(myPosition);
        }
        if (!viewModel.players[nextPosition].bidCalled) {
            mWebSocket.sendSelectBid(viewModel.players, nextPosition,true);
        } else {
           // mWebSocket.first
            if (state == GameState.FIRST_BID_SELECTION) {
                //All players called.... find the player who called the max point , select trump
                Player maxBidPlayer = getMaxBidPlayer();
                state = GameState.DRAWING_CARD_FIRST4;
                if(maxBidPlayer.user.getUserName().equals(viewModel.me.user.getUserName())) {
                    mGameListener.chooseTrump(maxBidPlayer);
                } else {

                }
                mWebSocket.sendChooseTrump(maxBidPlayer);
            }

        }

    }
    private Player getMaxBidPlayer() {
        Player maxBidPlayer = viewModel.players[0];
        int max = maxBidPlayer.pointCalled;
        for (int i =1 ; i<4; i++) {
            if (viewModel.players[i].pointCalled > max) {
                maxBidPlayer = viewModel.players[i];
                max = viewModel.players[i].pointCalled;
            }
        }

        return maxBidPlayer;
    }
    private void sortMyCards() {

    }
    /**
     * When a table is created, the app should create a socket connection with wss://cloud.achex.ca/00001023
     * SEND:{"auth":"<player1_userName>@00001023","passwd":"secret-pwd"}
     * RECV:{"auth":"OK","SID":46913}
     * SEND:{"joinHub":"<TableID>","passwd":"secret-pwd"}
     * RECV:{"joinHub":"OK"}
     * After table is created share the table id to friends and wait till they join.
     */
    public void createTable() {
        if (viewModel.me.isDealer) {
            state = GameState.CREATING_TABLE;
            viewModel.tableID = TEST_TABLE_ID;
            mWebSocket.connect(STAGE_WS_INSTANCE);
            state = GameState.WAITING_FOR_FRIENDS;
            if (NO_SOCKET) {
                mGameListener.onCreatedTable(TEST_TABLE_ID);
            }
        }
    }

    /**
     * SEND:{"auth":"<player2_userName>@00001023","passwd":"secret-pwd"}
     * RECV:{"auth":"OK","SID":46923}
     * SEND:{"joinHub":"<TableID>","passwd":"secret-pwd"}
     * RECV:{"joinHub":"OK"}
     * SEND:{"toH":"<TableID>","msg":"joined"}
     */
    public void joinTable() {
        if (!viewModel.me.isDealer) {
            mWebSocket.connect(STAGE_WS_INSTANCE);
            state = GameState.WAITING_FOR_FRIENDS;
        }
    }

    /**
     * RECV:{"toH":"<TableID>","msg":"joined","sID":46923,"FROM":"<player2_userName>@00001023"}
     */
    public void waitForOthers() {

    }

    @SuppressLint("DefaultLocale")
    public static String getRandomTableId() {
        // It will generate 4 digit random Number.
        // from 0 to 9999
        Random rnd = new Random();
        int number = rnd.nextInt(9999);
        // this will convert any number sequence into 4 character.
        return String.format("%04d", number);
    }

    public void updateUIAfterFirstSet() {
        showCards(0, 4);
    }

    public void updateUIAfterSecondSet() {
        showCards(4, 8);
        ui.drawCards.setVisibility(View.GONE);
        ui.shuffleDrawOptions.setVisibility(View.GONE);
    }

    private void showCards(int from, int to) {
        Player player = viewModel.players[viewModel.getMyPosition()];
        if (player == null) {
            Logger.e("Player is null");
            return;
        }
        if (player.cardsInHand.isEmpty()) {
            Logger.e("Cards are empty");
            ViewUtils.showToast(uiActivity, "Error: Cards are empty");
        }
        for (int i = from; i < to; i++) {

            String cardIconStr = player.cardsInHand.get(i).getCardIcon();
            int resID = ui.getRoot().getResources().getIdentifier(cardIconStr,
                    "drawable", ui.getRoot().getContext().getPackageName());
            Logger.d("resID for " + cardIconStr + " = " + resID);
            userCardViews[i].setVisibility(View.VISIBLE);
            userCardViews[i].setBackgroundResource(resID);
        }
    }


    @Override
    public void onConnected() {
        Logger.d("onConnected");
        mWebSocket.auth(viewModel.me.user.getUserName());
    }

    @Override
    public void onAuthSuccess( AuthResponse response ) {
        Logger.d("onAuthSuccess");
        mWebSocket.joinTable(TEST_TABLE_ID);
        noOfPlayers = 1;
        if (viewModel.me.isDealer) {
            //player[0] is always the person who created the table.
            viewModel.players[0] = viewModel.me;
        }

    }

    @Override
    public void onJoinedTable(JoinTableResponse response) {
        Logger.d("onJoinedTable "+response.tableId);
        mWebSocket.broadCastJoined(response.tableId);
    }

    @Override
    public void onPlayerJoined(PlayerJoinedResponse response) {
        Logger.d("onPlayerJoined "+response.playerName);
        if (viewModel.me != null && viewModel.me.isDealer && noOfPlayers < 4) {
            //Dealer sends the player position while creating the table first time
            Logger.d("Filling up the table noOfPlayers = "+noOfPlayers);
            GameUser user = new GameUser();
            user.setUserName(response.playerName);
            if (viewModel.players[noOfPlayers] == null) {
                viewModel.players[noOfPlayers] = new Player();
            }
            viewModel.players[noOfPlayers].user = user;
            viewModel.players[noOfPlayers].playerPosition = noOfPlayers;
            mWebSocket.broadCastPlayerDetails(viewModel.players);
            noOfPlayers++;
        }
        if (noOfPlayers == 4 && viewModel.me.isDealer) {
            Logger.d("Table filled");
            mWebSocket.broadCastAllPlayerJoined(viewModel.players);
            mGameListener.onCreatedTable(TEST_TABLE_ID);
        }
    }

    @Override
    public void onPlayerDetailsReceived(AllPlayersDetailsResponse response) {
        Logger.d("onPlayerDetailsReceived "+response.players);
        viewModel.players = response.players;
    }

    @Override
    public void onAllPlayersJoined(AllPlayersDetailsResponse response) {
        Logger.d("onAllPlayersJoined "+response.players);
        viewModel.players = response.players;
        if (!viewModel.me.isDealer) {
            if (viewModel.players.length == 4) {
                Logger.d("Table filled -- my position is "+viewModel.getMyPosition());
                mGameListener.onJoinedTable(TEST_TABLE_ID);
            }
        }
    }

    @Override
    public void onFirstSetCardsReceived(AllPlayersDetailsResponse response) {
        state = GameState.FIRST_BID_SELECTION;
        viewModel.players = response.players;
        if (!viewModel.me.isDealer) {
            uiActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateUIAfterFirstSet();
                    ui.loadingLayout.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public void onSecondSetCardsReceived(AllPlayersDetailsResponse response) {
        viewModel.players = response.players;
        if (!viewModel.me.isDealer) {
            uiActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateUIAfterSecondSet();
                }
            });
        }
    }

    @Override
    public void onUserShufflingCards(final PlayerDetailResponse response) {
        uiActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ui.loadingLayoutLoadingMessage.setText("\n"+response.player.user.getUserName() + " is Shuffling Cards...");
            }
        });
    }

    @Override
    public void onSelectBid(final BidSelectionResponse response) {
        viewModel.players = response.players;
        uiActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (viewModel.getMyPosition() == response.position) {
                    mGameListener.selectBid(viewModel.players[viewModel.getMyPosition()], response.canPass);
                }
            }
        });
    }

    @Override
    public void onChooseTrump(final BidSelectionResponse response) {
        viewModel.players = response.players;
        uiActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (viewModel.getMyPosition() == response.position) {
                    mGameListener.chooseTrump(response.players[viewModel.getMyPosition()]);
                }

            }
        });
    }

    @Override
    public void onFailure(WebSocketConnection.Reason failureReason, Throwable throwable) {
        Logger.e("onFailure");
        throwable.printStackTrace();
        mGameListener.onGameCreationFailure(throwable);
    }
}