package com.agames.thuruppugulan.ui.main.game;


import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.agames.thuruppugulan.R;
import com.agames.thuruppugulan.databinding.TableFragmentBinding;
import com.agames.thuruppugulan.ui.main.GameState;
import com.agames.thuruppugulan.ui.main.utils.ViewUtils;
import com.orhanobut.logger.Logger;

import java.util.Random;

public class ThuruppuKalli {
    private TableFragmentViewModel viewModel;

    private final TableFragmentBinding ui;
    private CardView[] userCardViews;

    public interface OnGameListener {
        void onCreatedTable(String tableId);

        void onJoinedTable(String tableId);
    }

    private OnGameListener mGameListener;
    public GameState state;

    private static String TEST_TABLE_ID = "6542";

    // Position goes like
    // 1 - 2 - 3 - 4
    // 2 - 3 - 4 - 1
    // 3 - 4 - 1 - 2
    // 4 - 1 - 2 - 3
    public int myPosition;


    public ThuruppuKalli(TableFragmentBinding ui, int myPosition, TableFragmentViewModel viewModel, OnGameListener listener) {
        this.ui = ui;
        this.myPosition = myPosition;
        this.viewModel = viewModel;
        this.mGameListener = listener;
        initCardObjects(ui);
        state = GameState.INIT;
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
        for (int i = 0; i < 8; i++) {
            userCardViews[i].setVisibility(View.GONE);
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
                } else if (state == GameState.DRAWING_CARD_LAST4) {
                    viewModel.drawSet();
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

    /**
     * When a table is created, the app should create a socket connection with wss://cloud.achex.ca/00001023
     * SEND:{"auth":"<player1_userName>@00001023","passwd":"secret-pwd"}
     * RECV:{"auth":"OK","SID":46913}
     * SEND:{"joinHub":"<TableID>","passwd":"secret-pwd"}
     * RECV:{"joinHub":"OK"}
     * After table is created share the table id to friends and wait till they join.
     */
    public void createTable() {
        if (viewModel.players[myPosition].isDealer) {
            state = GameState.CREATING_TABLE;
            viewModel.tableID = TEST_TABLE_ID;
            state = GameState.WAITING_FOR_FRIENDS;
            mGameListener.onCreatedTable(TEST_TABLE_ID);

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
        if (!viewModel.players[myPosition].isDealer) {

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
    }

    private void showCards(int from, int to) {
        for (int i = from; i < to; i++) {
            Player player = viewModel.players[myPosition];
            if (player == null) {
                Logger.e("Player is null");
                return;
            }
            String cardIconStr = player.cardsInHand.get(i).getCardIcon();
            int resID = ui.getRoot().getResources().getIdentifier(cardIconStr,
                    "drawable", ui.getRoot().getContext().getPackageName());
            Logger.d("resID for " + cardIconStr + " = " + resID);
            userCardViews[i].setVisibility(View.VISIBLE);
            userCardViews[i].setBackgroundResource(resID);
        }
    }
}