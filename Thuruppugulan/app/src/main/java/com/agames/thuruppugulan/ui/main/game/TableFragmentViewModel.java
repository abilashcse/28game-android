package com.agames.thuruppugulan.ui.main.game;

import androidx.lifecycle.ViewModel;

import com.agames.thuruppugulan.ui.main.game.props.Deck;

public class TableFragmentViewModel extends ViewModel {
    public Player[] players = new Player[4];
    public Deck deck = new Deck();

    public void shuffleDeck() {
        deck.shuffleDeck();
    }

    public void drawCards() {
        deck.drawCards();
    }
}
