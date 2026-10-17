package com.agames.thuruppugulan.domain

import kotlinx.serialization.Serializable

@Serializable
data class Player(
    val id: String,
    val displayName: String,
    val seat: Int,
    val isDealer: Boolean = false,
    val isLocal: Boolean = false,
    val hand: List<Card> = emptyList(),
    val wonCards: List<Card> = emptyList(),
    val bid: Int? = null,
    val isTrumpChooser: Boolean = false,
) {
    val team: Int get() = seat % 2

    fun pointsWon(): Int = wonCards.sumOf { it.points }

    fun withHand(hand: List<Card>) = copy(hand = hand.sorted())
    fun withoutCard(card: Card) = copy(hand = hand.filterNot { it == card })
    fun withWon(cards: List<Card>) = copy(wonCards = wonCards + cards)
}
