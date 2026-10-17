package com.agames.thuruppugulan.domain

import kotlinx.serialization.Serializable

@Serializable
data class Card(
    val suit: Suit,
    val rank: Rank,
) : Comparable<Card> {
    val points: Int get() = rank.points
    val shortLabel: String get() = "${rank.label}${suit.symbol}"
    val details: String get() = "${rank.name} of ${suit.name}"

    override fun compareTo(other: Card): Int {
        val suitCmp = suit.ordinal.compareTo(other.suit.ordinal)
        if (suitCmp != 0) return suitCmp
        // Lower ordinal = higher rank in our enum order
        return rank.ordinal.compareTo(other.rank.ordinal)
    }

    fun beats(other: Card, ledSuit: Suit, trump: Suit?, trumpExposed: Boolean): Boolean {
        if (trumpExposed && trump != null) {
            val thisTrump = suit == trump
            val otherTrump = other.suit == trump
            return when {
                thisTrump && !otherTrump -> true
                !thisTrump && otherTrump -> false
                thisTrump && otherTrump -> rank.ordinal < other.rank.ordinal
                suit == ledSuit && other.suit == ledSuit -> rank.ordinal < other.rank.ordinal
                suit == ledSuit -> true
                other.suit == ledSuit -> false
                else -> false
            }
        }
        // Phase 1: highest of led suit wins; trump suit has no special power yet
        return if (suit == ledSuit && other.suit == ledSuit) {
            rank.ordinal < other.rank.ordinal
        } else {
            suit == ledSuit && other.suit != ledSuit
        }
    }
}
