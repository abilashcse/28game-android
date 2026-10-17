package com.agames.thuruppugulan.ui.main.game.props;

public class Rule {

    private String rank;
    private int value;

    private int player1HandValue;
    private int player2HandValue;

    private String result;

    public Rule() {

    }

    public Rule(int player1HandValue, int player2HandValue) {
        this.player1HandValue = player1HandValue;
        this.player2HandValue = player2HandValue;
    }

    public Rule(String rank) {
        this.rank = rank;
        this.value = value;
    }

    public String getRank() {
        return this.rank;
    }

    public int getValueFromRank(String rank) {
        switch(rank) {
            case "ACE":
            case "TEN":
                value = 1;
                break;
            case "SEVEN":
            case "EIGHT":
            case "QUEEN":
            case "KING":
                value = 0;
                break;
            case "NINE":
                value = 2;
                break;
            case "JACK":
                value = 3;
                break;
        }
        return value;
    }

    public String getResult(int player1HandNewValue, int player2HandNewValue) {
        if (player1HandNewValue > player2HandNewValue) {
            return "Player1";
        } else if (player2HandNewValue > player1HandNewValue) {
            return "Player2";
        } else {
            return "Game Drawn";
        }
    }

}