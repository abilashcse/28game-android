package com.agames.thuruppugulan.ui.main.game;

import androidx.lifecycle.ViewModel;

import com.agames.thuruppugulan.ui.main.game.props.Card;
import com.agames.thuruppugulan.ui.main.game.props.Deck;
import com.orhanobut.logger.Logger;

public class TableFragmentViewModel extends ViewModel {
    public Player[] players = new Player[4];
    public String tableID;
    public Deck deck = new Deck();
    public Card[] tableCards = new Card[4];
    public boolean createTable;
    public void shuffleDeck() {
        deck.shuffleDeck();
    }

    public void drawSet() {
        Player dealer = getDealerPlayer();
        if (dealer != null) {
            Logger.d("Dealer = "+dealer);
            int nextPosition = findNextPosition(dealer.playerPosition);
            for (int i = 0; i<4; i++) {
                Player player = players[nextPosition];
                player.cardsInHand.addAll(deck.getFourCardOnTop());
                StringBuilder cardStr = new StringBuilder();
                for (Card card: player.cardsInHand) {
                    cardStr.append(card.getCardDetails()).append(", ");
                }
                Logger.i("Returning cards "+cardStr + " for Player "+nextPosition);
                nextPosition = findNextPosition(nextPosition);
            }
        }
        Logger.d("Total cards in deck after draw = "+deck.deckSize());
        //deck.logDeck();
    }

    private int findNextPosition(int playerPosition) {
        if (playerPosition == 0) {
            return 3;
        } else {
            playerPosition --;
            return playerPosition;
        }
    }

    public Player getDealerPlayer() {
        for (int i = 0; i< 4; i++) {
            if (players[i].isDealer)
                return players[i];
        }
        return null;
    }
}
