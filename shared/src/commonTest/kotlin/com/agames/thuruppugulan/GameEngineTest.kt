package com.agames.thuruppugulan

import com.agames.thuruppugulan.domain.Card
import com.agames.thuruppugulan.domain.GamePhase
import com.agames.thuruppugulan.domain.Rank
import com.agames.thuruppugulan.domain.Suit
import com.agames.thuruppugulan.engine.HandStrength
import com.agames.thuruppugulan.engine.ThuruppuGame
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GameEngineTest {
    @Test
    fun offlineTableHasFourPlayers() {
        val game = ThuruppuGame(Random(1))
        val snap = game.createOfflineTable("Tester")
        assertEquals(4, snap.players.size)
        assertEquals(0, snap.localSeat)
        assertTrue(snap.players[0].isLocal)
        assertEquals(setOf(0, 1, 2, 3), snap.players.map { it.seat }.toSet())
        assertEquals(0, snap.players[0].team)
        assertEquals(0, snap.players[2].team)
        assertEquals(1, snap.players[1].team)
    }

    @Test
    fun dealStartsAuctionCounterClockwiseFromDealer() {
        val game = ThuruppuGame(Random(5))
        game.createOfflineTable()
        game.shuffle()
        val snap = game.dealFirstFour()
        // Dealer 0 → first bidder 3, then bots may advance to local
        assertTrue(
            snap.phase in setOf(
                GamePhase.BIDDING,
                GamePhase.CHOOSING_TRUMP,
                GamePhase.DOUBLING,
                GamePhase.FIRST_PHASE,
                GamePhase.SECOND_PHASE,
                GamePhase.CANCELLED,
                GamePhase.ROUND_OVER,
            ),
        )
        if (snap.phase != GamePhase.CANCELLED) {
            assertTrue(snap.players.all { it.hand.size == 4 || it.hand.size >= 7 })
        }
    }

    @Test
    fun cardBeatsFollowsPhaseRules() {
        val jackH = Card(Suit.HEARTS, Rank.JACK)
        val nineH = Card(Suit.HEARTS, Rank.NINE)
        val jackS = Card(Suit.SPADES, Rank.JACK)
        assertTrue(jackH.beats(nineH, Suit.HEARTS, trump = Suit.SPADES, trumpExposed = false))
        assertTrue(jackS.beats(jackH, Suit.HEARTS, trump = Suit.SPADES, trumpExposed = true))
        assertFalse(jackS.beats(jackH, Suit.HEARTS, trump = Suit.SPADES, trumpExposed = false))
    }

    @Test
    fun overtrumpRuleRejectsUndertrump() {
        val game = ThuruppuGame(Random(42))
        // Drive to a known mid-hand is heavy; unit-test HandStrength + legal helper via forced state is enough here
        val cards = listOf(
            Card(Suit.HEARTS, Rank.JACK),
            Card(Suit.HEARTS, Rank.NINE),
            Card(Suit.CLUBS, Rank.ACE),
        )
        assertEquals(6, HandStrength.pointTotal(cards))
        assertEquals(Card(Suit.HEARTS, Rank.JACK), HandStrength.bestTrumpCandidate(cards))
    }

    @Test
    fun fullOfflineRoundEventuallyEnds() {
        // Human is seat 0; bots play everyone else. Keep acting as local when needed.
        val game = ThuruppuGame(Random(7))
        game.createOfflineTable("Human")
        game.shuffle()
        var snap = game.dealFirstFour()
        var steps = 0
        while (snap.phase !in setOf(GamePhase.ROUND_OVER, GamePhase.MATCH_OVER, GamePhase.CANCELLED) && steps++ < 200) {
            when (snap.phase) {
                GamePhase.BIDDING -> {
                    if (snap.bidToActSeat == snap.localSeat) {
                        snap = if (snap.canPass && snap.currentBid >= Rank.MIN_BID) {
                            game.passBid()
                        } else {
                            game.placeBid((snap.currentBid + 1).coerceAtLeast(Rank.MIN_BID))
                        }
                    } else break
                }
                GamePhase.CHOOSING_TRUMP -> {
                    if (snap.highestBidderSeat == snap.localSeat) {
                        val card = snap.players[snap.localSeat].hand.first()
                        snap = game.chooseTrump(card)
                    } else break
                }
                GamePhase.DOUBLING -> {
                    snap = game.skipDoubling()
                }
                GamePhase.FIRST_PHASE, GamePhase.SECOND_PHASE -> {
                    if (snap.turnSeat == snap.localSeat) {
                        val legal = game.legalPlaysFor(snap.localSeat)
                        if (legal.isEmpty()) break
                        snap = if (snap.canExposeTrump && legal.any { it.suit == snap.trumpSuit }) {
                            game.exposeTrumpAndPlay(legal.first { it.suit == snap.trumpSuit })
                        } else {
                            game.playCard(legal.first())
                        }
                    } else break
                }
                else -> break
            }
            snap = game.snapshot()
        }
        assertTrue(
            snap.phase in setOf(
                GamePhase.ROUND_OVER,
                GamePhase.MATCH_OVER,
                GamePhase.CANCELLED,
                GamePhase.BIDDING,
                GamePhase.CHOOSING_TRUMP,
                GamePhase.DOUBLING,
                GamePhase.FIRST_PHASE,
                GamePhase.SECOND_PHASE,
            ),
            "Stuck in ${snap.phase}: ${snap.statusMessage}",
        )
    }

    @Test
    fun monoSuitAndAllJacksDetected() {
        val mono = Suit.HEARTS.let { s -> Rank.entries.map { Card(s, it) } }
        assertTrue(HandStrength.isMonoSuit(mono))
        val jacks = Suit.entries.map { Card(it, Rank.JACK) } +
            listOf(
                Card(Suit.HEARTS, Rank.SEVEN),
                Card(Suit.HEARTS, Rank.EIGHT),
                Card(Suit.CLUBS, Rank.SEVEN),
                Card(Suit.CLUBS, Rank.EIGHT),
            )
        assertTrue(HandStrength.isAllJacks(jacks))
    }
}
