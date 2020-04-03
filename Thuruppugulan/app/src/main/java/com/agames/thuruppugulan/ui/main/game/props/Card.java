package com.agames.thuruppugulan.ui.main.game.props;


public class Card implements Comparable<Card>{

    private int value;
    public final int slNo;
    private String cardDetails;
    private String cardIcon;

    private Rule rule;
    private final Suit suit;
    private final Rank rank;


    public Card(int slNo, Suit suit, Rank rank) {
        this.rank = rank;
        this.suit = suit;
        this.slNo = slNo;
        this.cardDetails = getCardDetails(suit, rank);
    }

    public String getCardDetails(Suit suit, Rank rank) {
       return rank + " of " + suit;
    }

    public String getCardDetails() {
        return cardDetails;
    }

    public Rank getRank() {
        return this.rank;
    }

    public int getValue() {
        int value = rule.getValueFromRank(rank.toString());
        return value;
    }

    public void setValue(int newValue) {
        this.value = newValue;
    }

    public Suit getSuit() {
        return this.suit;
    }


    public String getCardIcon() {

        // Spades

        if (cardDetails.equals("ACE of SPADES")) {
            cardIcon = "ace_of_spades";
        } else if (cardDetails.equals("TWO of SPADES")) {
            cardIcon = "two_of_spades";
        } else if (cardDetails.equals("THREE of SPADES")) {
            cardIcon = "three_of_spades";
        } else if (cardDetails.equals("FOUR of SPADES")) {
            cardIcon = "four_of_spades";
        } else if (cardDetails.equals("FIVE of SPADES")) {
            cardIcon = "five_of_spades";
        } else if (cardDetails.equals("SIX of SPADES")) {
            cardIcon = "six_of_spades";
        } else if (cardDetails.equals("SEVEN of SPADES")) {
            cardIcon = "seven_of_spades";
        } else if (cardDetails.equals("EIGHT of SPADES")) {
            cardIcon = "eight_of_spades";
        } else if (cardDetails.equals("NINE of SPADES")) {
            cardIcon = "nine_of_spades";
        } else if (cardDetails.equals("TEN of SPADES")) {
            cardIcon = "ten_of_spades";
        } else if (cardDetails.equals("JACK of SPADES")) {
            cardIcon = "jack_of_spades";
        } else if (cardDetails.equals("QUEEN of SPADES")) {
            cardIcon = "queen_of_spades";
        } else if (cardDetails.equals("KING of SPADES")) {
            cardIcon = "king_of_spades";
        }

        // Clubs

        if (cardDetails.equals("ACE of CLUBS")) {
            cardIcon = "ace_of_clubs";
        } else if (cardDetails.equals("TWO of CLUBS")) {
            cardIcon = "two_of_clubs";
        } else if (cardDetails.equals("THREE of CLUBS")) {
            cardIcon = "three_of_clubs";
        } else if (cardDetails.equals("FOUR of CLUBS")) {
            cardIcon = "four_of_clubs";
        } else if (cardDetails.equals("FIVE of CLUBS")) {
            cardIcon = "five_of_clubs";
        } else if (cardDetails.equals("SIX of CLUBS")) {
            cardIcon = "six_of_clubs";
        } else if (cardDetails.equals("SEVEN of CLUBS")) {
            cardIcon = "seven_of_clubs";
        } else if (cardDetails.equals("EIGHT of CLUBS")) {
            cardIcon = "eight_of_clubs";
        } else if (cardDetails.equals("NINE of CLUBS")) {
            cardIcon = "nine_of_clubs";
        } else if (cardDetails.equals("TEN of CLUBS")) {
            cardIcon = "ten_of_clubs";
        } else if (cardDetails.equals("JACK of CLUBS")) {
            cardIcon = "jack_of_clubs";
        } else if (cardDetails.equals("QUEEN of CLUBS")) {
            cardIcon = "queen_of_clubs";
        } else if (cardDetails.equals("KING of CLUBS")) {
            cardIcon = "king_of_clubs";
        }

        // Hearts

        if (cardDetails.equals("ACE of HEARTS")) {
            cardIcon = "ace_of_hearts";
        } else if (cardDetails.equals("TWO of HEARTS")) {
            cardIcon = "two_of_hearts";
        } else if (cardDetails.equals("THREE of HEARTS")) {
            cardIcon = "three_of_hearts";
        } else if (cardDetails.equals("FOUR of HEARTS")) {
            cardIcon = "four_of_hearts";
        } else if (cardDetails.equals("FIVE of HEARTS")) {
            cardIcon = "five_of_hearts";
        } else if (cardDetails.equals("SIX of HEARTS")) {
            cardIcon = "six_of_hearts";
        } else if (cardDetails.equals("SEVEN of HEARTS")) {
            cardIcon = "seven_of_hearts";
        } else if (cardDetails.equals("EIGHT of HEARTS")) {
            cardIcon = "eight_of_hearts";
        } else if (cardDetails.equals("NINE of HEARTS")) {
            cardIcon = "nine_of_hearts";
        } else if (cardDetails.equals("TEN of HEARTS")) {
            cardIcon = "ten_of_hearts";
        } else if (cardDetails.equals("JACK of HEARTS")) {
            cardIcon = "jack_of_hearts";
        } else if (cardDetails.equals("QUEEN of HEARTS")) {
            cardIcon = "queen_of_hearts";
        } else if (cardDetails.equals("KING of HEARTS")) {
            cardIcon = "king_of_hearts";
        }

        // Diamonds

        if (cardDetails.equals("ACE of DIAMONDS")) {
            cardIcon = "ace_of_diamonds";
        } else if (cardDetails.equals("TWO of DIAMONDS")) {
            cardIcon = "two_of_diamonds";
        } else if (cardDetails.equals("THREE of DIAMONDS")) {
            cardIcon = "three_of_diamonds";
        } else if (cardDetails.equals("FOUR of DIAMONDS")) {
            cardIcon = "four_of_diamonds";
        } else if (cardDetails.equals("FIVE of DIAMONDS")) {
            cardIcon = "five_of_diamonds";
        } else if (cardDetails.equals("SIX of DIAMONDS")) {
            cardIcon = "six_of_diamonds";
        } else if (cardDetails.equals("SEVEN of DIAMONDS")) {
            cardIcon = "seven_of_diamonds";
        } else if (cardDetails.equals("EIGHT of DIAMONDS")) {
            cardIcon = "eight_of_diamonds";
        } else if (cardDetails.equals("NINE of DIAMONDS")) {
            cardIcon = "nine_of_diamonds";
        } else if (cardDetails.equals("TEN of DIAMONDS")) {
            cardIcon = "ten_of_diamonds";
        } else if (cardDetails.equals("JACK of DIAMONDS")) {
            cardIcon = "jack_of_diamonds";
        } else if (cardDetails.equals("QUEEN of DIAMONDS")) {
            cardIcon = "queen_of_diamonds";
        } else if (cardDetails.equals("KING of DIAMONDS")) {
            cardIcon = "king_of_diamonds";
        }

        return this.cardIcon;
    }

    @Override
    public int compareTo(Card card) {
        if (card.suit == this.suit)  {
            return card.rank.compareTo(this.rank);
        } else {
            
        }
        return 0;
    }
}
