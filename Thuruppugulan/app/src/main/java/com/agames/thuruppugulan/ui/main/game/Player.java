package com.agames.thuruppugulan.ui.main.game;


import androidx.annotation.NonNull;

import com.agames.thuruppugulan.model.GameUser;
import com.agames.thuruppugulan.ui.main.game.props.Card;

import java.util.ArrayList;

public class Player {
    public GameUser user;
    public boolean isDealer;
    public int playerPosition;
    public ArrayList <Card> cardsInHand = new ArrayList<>();
    public ArrayList <Card> cardsWon = new ArrayList<>();
    public boolean bidCalled;
    public boolean isTrump;
    public Card trumpCard;
    public int pointCalled;
    public int getPoints() {
        int pointsEarn = 0;
        for (Card card: cardsWon) {
            pointsEarn += card.getValue();
        }
        return pointsEarn;
    }

    @NonNull
    @Override
    public String toString() {
        return "PlayerName: = "+user.getUserName()+"\n"+
                "player position = "+playerPosition+"\n"+
                "isDealer = "+isDealer;
    }
}
