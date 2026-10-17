package com.agames.thuruppugulan.engine

import com.agames.thuruppugulan.domain.Card
import com.agames.thuruppugulan.domain.Rank
import com.agames.thuruppugulan.domain.Suit

/** Heuristics used by bots and for optional bid suggestions. */
object HandStrength {
    fun pointTotal(cards: List<Card>): Int = cards.sumOf { it.points }

    fun suitLength(cards: List<Card>, suit: Suit): Int = cards.count { it.suit == suit }

    fun bestTrumpCandidate(cards: List<Card>): Card? {
        if (cards.isEmpty()) return null
        return cards.maxWith(
            compareBy<Card> { suitLength(cards, it.suit) }
                .thenBy { it.points }
                .thenByDescending { it.rank.ordinal },
        )
    }

    /** Rough expected trick points support for opening / raising. */
    fun suggestedBid(cards: List<Card>): Int? {
        val points = pointTotal(cards)
        val longest = Suit.entries.maxOf { suitLength(cards, it) }
        val jacks = cards.count { it.rank == Rank.JACK }
        val raw = points * 2 + longest * 2 + jacks * 2
        return when {
            raw < 10 -> null
            raw < 14 -> Rank.MIN_BID
            raw < 18 -> (Rank.MIN_BID + 2).coerceAtMost(Rank.MAX_BID)
            raw < 22 -> 18
            raw < 26 -> 20
            else -> 22.coerceAtMost(Rank.MAX_BID)
        }
    }

    fun isPointless(cards: List<Card>): Boolean = pointTotal(cards) == 0

    fun isAllJacks(cards: List<Card>): Boolean =
        cards.size == 8 && cards.count { it.rank == Rank.JACK } == 4

    fun isMonoSuit(cards: List<Card>): Boolean =
        cards.size == 8 && cards.map { it.suit }.distinct().size == 1
}
