package com.agames.thuruppugulan.ui.main.game;


import com.agames.thuruppugulan.model.GameUser;
import com.agames.thuruppugulan.ui.main.game.props.Card;

import java.util.ArrayList;

public class Player {
    public GameUser user;
    public boolean isDealer;
    public int playerPosition;
    public ArrayList <Card> cardsInHand = new ArrayList<>();
    public ArrayList <Card> cardsWon = new ArrayList<>();
    public boolean isTrump;
    public Card trumpCard;
    public int maxPoint;
    public int getPoints() {
        int pointsEarn = 0;
        for (Card card: cardsWon) {
            pointsEarn += card.getValue();
        }
        return pointsEarn;
    }
}
