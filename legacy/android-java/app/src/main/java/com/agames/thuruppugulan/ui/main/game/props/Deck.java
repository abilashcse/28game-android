package com.agames.thuruppugulan.ui.main.game.props;

import android.util.Log;


import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Random;


public class Deck {

    private Suit suit;
    private Rank rank;
    private ArrayList<Card> deck;
    private Random randomGenerator = new Random();

    public Deck() {
        this.deck = new ArrayList<Card>();
        createDeck();
    }

    public int deckSize() {
        return deck.size();
    }

    public void createDeck() {
        int slNo = 1;
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                deck.add(new Card(slNo++, suit, rank));
            }
        }
    }

    public ArrayList<Card> getFourCardOnTop() {
        ArrayList<Card> fourCards = new ArrayList<>();
        for (int i=0; i<4; i++) {
            fourCards.add(i,deck.remove(0));
        }
        return fourCards;
    }

    public void shuffleDeck() {
        int indexAny = randomGenerator.nextInt(deck.size()) % 32;
        Card tmp = deck.get(indexAny);
        deck.set(indexAny, deck.get(31));
        deck.set(31, tmp);
        int index1 = randomGenerator.nextInt(deck.size()) % 32;
        int index2 = randomGenerator.nextInt(deck.size()) % 32;
        if (index1 > index2) {
            int tmpIndex = index1;
            index1 = index2;
            index2 = tmpIndex;
        }
        Logger.d("index1 = " + index1);
        Logger.d("index2 = " + index2);
        ArrayList<Card> tempDeck = new ArrayList<>();
        for (int i = index1; i < index2; i++) {
            tempDeck.add(deck.get(i));
        }
        for (int i = 0; i < index1; i++) {
            tempDeck.add(deck.get(i));
        }
        for (int i = index2; i < 32; i++) {
            tempDeck.add(deck.get(i));
        }
        deck.clear();
        deck = tempDeck;
        Logger.d("New deck size = " + deck.size());


    }

    public void logDeck() {
        StringBuilder cardsStr = new StringBuilder();
        for (Card card : deck) {
            cardsStr.append("-> ").append(card.slNo).append(": ").append(card.getCardDetails()).append(" <-\n");
        }
        Logger.d(cardsStr.toString());
    }

}