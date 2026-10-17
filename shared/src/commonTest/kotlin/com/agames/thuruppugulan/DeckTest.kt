package com.agames.thuruppugulan

import com.agames.thuruppugulan.domain.Deck
import com.agames.thuruppugulan.domain.Rank
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeckTest {
    @Test
    fun deckHas32CardsAnd28Points() {
        val deck = Deck()
        assertEquals(32, deck.size())
        assertEquals(Rank.DECK_POINT_TOTAL, deck.remaining().sumOf { it.points })
    }

    @Test
    fun shuffleIsDeterministicWithSeed() {
        val a = Deck().also { it.shuffle(42) }.remaining()
        val b = Deck().also { it.shuffle(42) }.remaining()
        assertEquals(a, b)
        val c = Deck().also { it.shuffle(7) }.remaining()
        assertTrue(a != c)
    }
}
