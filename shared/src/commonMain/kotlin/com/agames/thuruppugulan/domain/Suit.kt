package com.agames.thuruppugulan.domain

enum class Suit(val symbol: String, val displayName: String) {
    HEARTS("♥", "Hearts"),
    DIAMONDS("♦", "Diamonds"),
    CLUBS("♣", "Clubs"),
    SPADES("♠", "Spades");

    val isRed: Boolean get() = this == HEARTS || this == DIAMONDS
}
