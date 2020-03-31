package com.agames.thuruppugulan.ui.main.game.props;

import android.util.Log;

import net.fitken.rose.Rose;

import java.util.ArrayList;
import java.util.Random;

public class Deck {

    private Suit suit;
    private Rank rank;
    private ArrayList<Card> deck;
    private Random randomGenerator = new Random();

    Rose logger = Rose.INSTANCE;

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

    public void drawCards() {
        logDeck();
    }

    public void shuffleDeck() {
        logger.debugEnabled(true);
        logger.debug("-----shuffleDeck-----");
        int index1 = randomGenerator.nextInt(deck.size()) % 32;
        int index2 = randomGenerator.nextInt(deck.size()) % 32;
        if (index1 > index2) {
            int tmpIndex = index1;
            index1 = index2;
            index2 = tmpIndex;
        }
        logger.debug("index1 = " + index1);
        logger.debug("index2 = " + index2);
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
        logger.debug("New deck size = " + deck.size());
        logger.debug("-----shuffleDeck-----");


    }

    public void logDeck() {
        logger.debugEnabled(true);
        logger.debug("------------------------------");
        for (Card card : deck) {
            Log.d(logger.getTAG(), "-> " + card.slNo + ": " + card.getCardDetails() + " <-");
        }
        logger.debug("------------------------------");
    }

}