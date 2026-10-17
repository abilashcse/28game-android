package com.agames.thuruppugulan.domain

import kotlin.random.Random

class Deck(private var random: Random = Random.Default) {
    private val cards: MutableList<Card> = mutableListOf()

    init {
        reset()
    }

    fun reset() {
        cards.clear()
        for (suit in Suit.entries) {
            for (rank in Rank.entries) {
                cards += Card(suit, rank)
            }
        }
        require(cards.size == 32) { "28 uses a 32-card deck" }
    }

    fun size(): Int = cards.size

    /** Fisher–Yates shuffle — replaces the weak single-cut shuffle from the legacy app. */
    fun shuffle(seed: Long? = null) {
        if (seed != null) {
            random = Random(seed)
        }
        for (i in cards.lastIndex downTo 1) {
            val j = random.nextInt(i + 1)
            val tmp = cards[i]
            cards[i] = cards[j]
            cards[j] = tmp
        }
    }

    fun draw(count: Int): List<Card> {
        require(count <= cards.size) { "Not enough cards: need $count have ${cards.size}" }
        val drawn = cards.take(count)
        repeat(count) { cards.removeAt(0) }
        return drawn
    }

    fun remaining(): List<Card> = cards.toList()
}
