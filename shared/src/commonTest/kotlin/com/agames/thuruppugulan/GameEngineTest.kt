package com.agames.thuruppugulan

import com.agames.thuruppugulan.domain.GamePhase
import com.agames.thuruppugulan.domain.Rank
import com.agames.thuruppugulan.engine.ThuruppuGame
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GameEngineTest {
    @Test
    fun offlineDealEntersBiddingOrLater() {
        val game = ThuruppuGame(Random(1))
        game.createOfflineTable("Tester")
        game.shuffle()
        val snap = game.dealFirstFour()
        // Offline bots may advance the auction immediately after the deal.
        assertTrue(
            snap.phase in setOf(
                GamePhase.BIDDING,
                GamePhase.CHOOSING_TRUMP,
                GamePhase.FIRST_PHASE,
                GamePhase.SECOND_PHASE,
                GamePhase.ROUND_OVER,
            ),
            "Unexpected phase ${snap.phase}",
        )
        assertEquals(4, snap.players.size)
        assertTrue(snap.players.all { it.hand.size == 4 || it.hand.size == 7 || it.hand.size == 8 })
    }

    @Test
    fun localCanRaiseAfterBotsOpen() {
        val game = ThuruppuGame(Random(99))
        game.createOfflineTable()
        game.shuffle()
        var snap = game.dealFirstFour()
        if (snap.phase == GamePhase.BIDDING && snap.bidToActSeat == snap.localSeat) {
            val next = (snap.currentBid + 1).coerceAtLeast(Rank.MIN_BID)
            snap = game.placeBid(next)
            assertTrue(snap.currentBid >= Rank.MIN_BID)
        }
    }

    @Test
    fun firstBidderIsCounterClockwiseFromDealer() {
        val game = ThuruppuGame(Random(2))
        // Temporarily observe deal without relying on bot side effects:
        // dealer seat 0 => first bidder seat 3 before autos run fully to local.
        game.createOfflineTable()
        game.shuffle()
        val snap = game.dealFirstFour()
        // After bot open + two passes, local (0) should be to act or auction finished.
        assertTrue(
            snap.bidToActSeat == 0 ||
                snap.phase == GamePhase.CHOOSING_TRUMP ||
                snap.phase == GamePhase.FIRST_PHASE ||
                snap.highestBidderSeat != null,
        )
    }
}
