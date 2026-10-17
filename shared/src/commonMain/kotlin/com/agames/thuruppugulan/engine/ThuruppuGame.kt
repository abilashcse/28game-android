package com.agames.thuruppugulan.engine

import com.agames.thuruppugulan.domain.BidAction
import com.agames.thuruppugulan.domain.Card
import com.agames.thuruppugulan.domain.Deck
import com.agames.thuruppugulan.domain.GamePhase
import com.agames.thuruppugulan.domain.GameSnapshot
import com.agames.thuruppugulan.domain.Player
import com.agames.thuruppugulan.domain.Rank
import com.agames.thuruppugulan.domain.RoundScore
import com.agames.thuruppugulan.domain.Suit
import com.agames.thuruppugulan.domain.TrickPlay
import kotlin.random.Random

/**
 * Pure Kotlin game engine for 28 / Thuruppukali.
 * Platform UI and networking call into this; no Android/iOS APIs.
 */
class ThuruppuGame(
    private var random: Random = Random.Default,
) {
    private var deck = Deck(random)
    private var state = GameSnapshot()
    private var consecutivePasses = 0

    fun snapshot(): GameSnapshot = state

    fun createOfflineTable(localName: String = "You"): GameSnapshot {
        val players = listOf(
            Player("p0", localName, seat = 0, isDealer = true, isLocal = true),
            Player("p1", "North", seat = 1),
            Player("p2", "Partner", seat = 2),
            Player("p3", "West", seat = 3),
        )
        state = GameSnapshot(
            phase = GamePhase.READY_TO_DEAL,
            tableId = "OFFLINE",
            players = players,
            localSeat = 0,
            offlineMode = true,
            statusMessage = "Offline table ready — shuffle & deal",
        )
        return state
    }

    fun createOnlineHost(localName: String, tableId: String): GameSnapshot {
        state = GameSnapshot(
            phase = GamePhase.WAITING_FOR_PLAYERS,
            tableId = tableId,
            players = listOf(
                Player("host", localName, seat = 0, isDealer = true, isLocal = true),
            ),
            localSeat = 0,
            offlineMode = false,
            statusMessage = "Table $tableId created — waiting for 3 friends",
        )
        return state
    }

    fun joinOnline(localName: String, tableId: String, seat: Int): GameSnapshot {
        state = GameSnapshot(
            phase = GamePhase.WAITING_FOR_PLAYERS,
            tableId = tableId,
            players = listOf(
                Player("me", localName, seat = seat, isLocal = true),
            ),
            localSeat = seat,
            offlineMode = false,
            statusMessage = "Joined $tableId — waiting for players",
        )
        return state
    }

    fun replacePlayers(players: List<Player>): GameSnapshot {
        val phase = if (players.size == 4) GamePhase.READY_TO_DEAL else GamePhase.WAITING_FOR_PLAYERS
        state = state.copy(
            players = players.sortedBy { it.seat },
            phase = phase,
            statusMessage = if (players.size == 4) "All seated — dealer can shuffle" else "Players: ${players.size}/4",
        )
        return state
    }

    fun shuffle(): GameSnapshot {
        requirePhase(GamePhase.READY_TO_DEAL, GamePhase.ROUND_OVER)
        deck = Deck(random)
        deck.shuffle()
        state = state.copy(statusMessage = "Deck shuffled")
        return state
    }

    fun dealFirstFour(): GameSnapshot {
        requirePhase(GamePhase.READY_TO_DEAL, GamePhase.ROUND_OVER)
        if (deck.size() != 32) {
            deck = Deck(random)
            deck.shuffle()
        }
        val players = dealAround(state.players, count = 4)
        val firstBidder = nextSeat(dealerSeat())
        consecutivePasses = 0
        state = state.copy(
            phase = GamePhase.BIDDING,
            players = players,
            currentBid = Rank.MIN_BID - 1,
            highestBidderSeat = null,
            bidToActSeat = firstBidder,
            canPass = false,
            bids = emptyList(),
            trumpSuit = null,
            concealedTrumpCard = null,
            trumpExposed = false,
            currentTrick = emptyList(),
            tricksPlayed = 0,
            lastRound = null,
            statusMessage = "${players[firstBidder].displayName} opens the bidding (min ${Rank.MIN_BID})",
        )
        maybeAutoBid()
        return state
    }

    fun placeBid(amount: Int): GameSnapshot {
        requirePhase(GamePhase.BIDDING)
        val seat = state.bidToActSeat ?: error("No bidder")
        require(amount in (state.currentBid + 1)..Rank.MAX_BID) {
            "Bid must be between ${state.currentBid + 1} and ${Rank.MAX_BID}"
        }
        consecutivePasses = 0
        val players = state.players.map {
            if (it.seat == seat) it.copy(bid = amount, isTrumpChooser = false) else it
        }
        state = state.copy(
            players = players,
            currentBid = amount,
            highestBidderSeat = seat,
            canPass = true,
            bids = state.bids + BidAction(seat, amount, passed = false),
            statusMessage = "${players[seat].displayName} bid $amount",
        )
        advanceBidder()
        maybeAutoBid()
        return state
    }

    fun passBid(): GameSnapshot {
        requirePhase(GamePhase.BIDDING)
        val seat = state.bidToActSeat ?: error("No bidder")
        require(state.canPass || state.highestBidderSeat != null) { "Opening bidder cannot pass" }
        consecutivePasses++
        state = state.copy(
            bids = state.bids + BidAction(seat, null, passed = true),
            statusMessage = "${state.players[seat].displayName} passed",
        )
        if (state.highestBidderSeat != null && consecutivePasses >= 3) {
            return finishBidding()
        }
        advanceBidder()
        maybeAutoBid()
        return state
    }

    fun chooseTrump(card: Card): GameSnapshot {
        requirePhase(GamePhase.CHOOSING_TRUMP)
        val bidderSeat = state.highestBidderSeat ?: error("No bidder")
        val bidder = state.players[bidderSeat]
        require(card in bidder.hand) { "Trump card must be from bidder hand" }
        val players = state.players.map {
            if (it.seat == bidderSeat) it.withoutCard(card) else it
        }
        // Deal remaining 4 after trump is set aside from first four
        val afterSecondDeal = dealAround(players, count = 4)
        val lead = nextSeat(dealerSeat())
        state = state.copy(
            phase = GamePhase.FIRST_PHASE,
            players = afterSecondDeal,
            trumpSuit = card.suit,
            concealedTrumpCard = card,
            trumpExposed = false,
            leadSeat = lead,
            turnSeat = lead,
            currentTrick = emptyList(),
            ledSuit = null,
            statusMessage = "${bidder.displayName} set concealed trump — ${afterSecondDeal[lead].displayName} leads",
        )
        maybeAutoPlay()
        return state
    }

    fun playCard(card: Card): GameSnapshot {
        requirePhase(GamePhase.FIRST_PHASE, GamePhase.SECOND_PHASE)
        val seat = state.turnSeat ?: error("No turn")
        val player = state.players[seat]
        require(card in player.hand || (seat == state.highestBidderSeat && card == state.concealedTrumpCard && state.trumpExposed.not() && player.hand.isEmpty())) {
            "Card not in hand"
        }
        validatePlay(player, card)

        var players = state.players.toMutableList()
        var concealed = state.concealedTrumpCard
        var trumpExposed = state.trumpExposed
        var phase = state.phase

        val playingConcealed = concealed != null && card == concealed && card !in player.hand
        if (playingConcealed) {
            concealed = null
            trumpExposed = true
            phase = GamePhase.SECOND_PHASE
        } else {
            players[seat] = player.withoutCard(card)
        }

        val ledSuit = state.ledSuit ?: card.suit
        val trick = state.currentTrick + TrickPlay(seat, card)
        state = state.copy(
            players = players,
            currentTrick = trick,
            ledSuit = ledSuit,
            concealedTrumpCard = concealed,
            trumpExposed = trumpExposed,
            phase = phase,
            statusMessage = "${player.displayName} played ${card.shortLabel}",
        )

        if (trick.size == 4) {
            return resolveTrick()
        }
        state = state.copy(turnSeat = nextSeat(seat))
        maybeAutoPlay()
        return state
    }

    fun exposeTrumpAndPlay(card: Card): GameSnapshot {
        requirePhase(GamePhase.FIRST_PHASE)
        val seat = state.turnSeat ?: error("No turn")
        val player = state.players[seat]
        require(state.concealedTrumpCard != null) { "No concealed trump" }
        // Caller may not follow suit; exposing moves to phase 2 and requires a trump if held
        val trump = state.trumpSuit ?: error("Trump suit unknown")
        val handWithTrump = if (seat == state.highestBidderSeat) {
            player.hand + listOfNotNull(state.concealedTrumpCard)
        } else {
            player.hand
        }
        require(card.suit == trump || handWithTrump.none { it.suit == trump }) {
            "Must play a trump after exposing"
        }
        var players = state.players.toMutableList()
        if (seat == state.highestBidderSeat) {
            val merged = (player.hand + listOfNotNull(state.concealedTrumpCard)).filterNot { it == card }
            players[seat] = player.copy(hand = merged.sorted())
        } else {
            players[seat] = player.withoutCard(card)
        }
        val ledSuit = state.ledSuit ?: card.suit
        val trick = state.currentTrick + TrickPlay(seat, card)
        state = state.copy(
            phase = GamePhase.SECOND_PHASE,
            trumpExposed = true,
            concealedTrumpCard = null,
            players = players,
            currentTrick = trick,
            ledSuit = ledSuit,
            statusMessage = "Trump exposed (${trump.displayName}) — ${player.displayName} played ${card.shortLabel}",
        )
        if (trick.size == 4) return resolveTrick()
        state = state.copy(turnSeat = nextSeat(seat))
        maybeAutoPlay()
        return state
    }

    fun legalPlaysFor(seat: Int): List<Card> {
        val player = state.players.getOrNull(seat) ?: return emptyList()
        val hand = player.hand.toMutableList()
        if (seat == state.highestBidderSeat && state.concealedTrumpCard != null && state.trumpExposed) {
            // already merged when exposed
        }
        val led = state.ledSuit
        if (led == null) {
            // Phase 1: bidder cannot lead trump until exposed / void elsewhere
            if (state.phase == GamePhase.FIRST_PHASE &&
                seat == state.highestBidderSeat &&
                state.trumpSuit != null &&
                hand.any { it.suit != state.trumpSuit }
            ) {
                return hand.filter { it.suit != state.trumpSuit }
            }
            return hand
        }
        val followers = hand.filter { it.suit == led }
        return followers.ifEmpty { hand }
    }

    // --- internals ---

    private fun finishBidding(): GameSnapshot {
        val winner = state.highestBidderSeat ?: error("Auction ended with no bid")
        val players = state.players.map {
            it.copy(isTrumpChooser = it.seat == winner)
        }
        state = state.copy(
            phase = GamePhase.CHOOSING_TRUMP,
            players = players,
            bidToActSeat = null,
            statusMessage = "${players[winner].displayName} won auction at ${state.currentBid} — choose concealed trump",
        )
        maybeAutoChooseTrump()
        return state
    }

    private fun resolveTrick(): GameSnapshot {
        val trick = state.currentTrick
        val led = state.ledSuit ?: trick.first().card.suit
        var winner = trick.first()
        for (play in trick.drop(1)) {
            if (play.card.beats(winner.card, led, state.trumpSuit, state.trumpExposed)) {
                winner = play
            }
        }
        val wonCards = trick.map { it.card }
        val players = state.players.map {
            if (it.seat == winner.seat) it.withWon(wonCards) else it
        }
        val tricksPlayed = state.tricksPlayed + 1
        state = state.copy(
            players = players,
            currentTrick = emptyList(),
            ledSuit = null,
            leadSeat = winner.seat,
            turnSeat = winner.seat,
            tricksPlayed = tricksPlayed,
            statusMessage = "${players[winner.seat].displayName} won the trick",
        )
        if (tricksPlayed >= 8) {
            return scoreRound()
        }
        // Force expose on last card if still concealed
        if (tricksPlayed == 7 && !state.trumpExposed && state.concealedTrumpCard != null) {
            // will be played on last trick by bidder automatically if needed
        }
        maybeAutoPlay()
        return state
    }

    private fun scoreRound(): GameSnapshot {
        val bidderSeat = state.highestBidderSeat ?: 0
        val biddingTeam = bidderSeat % 2
        val teamPoints = intArrayOf(0, 0)
        state.players.forEach { teamPoints[it.team] += it.pointsWon() }
        // Return concealed trump points to bidder if never played
        state.concealedTrumpCard?.let { teamPoints[biddingTeam] += it.points }

        val bid = state.currentBid
        val made = teamPoints[biddingTeam] >= bid
        val delta = if (made) 1 else -1
        val scores = state.teamScores.toMutableMap()
        scores[biddingTeam] = (scores[biddingTeam] ?: 0) + delta
        val round = RoundScore(
            biddingTeam = biddingTeam,
            bid = bid,
            biddingTeamPoints = teamPoints[biddingTeam],
            defendingTeamPoints = teamPoints[1 - biddingTeam],
            bidMade = made,
            gamePointsDelta = delta,
        )
        val matchOver = scores.values.any { kotlin.math.abs(it) >= 6 }
        state = state.copy(
            phase = if (matchOver) GamePhase.MATCH_OVER else GamePhase.ROUND_OVER,
            teamScores = scores,
            lastRound = round,
            turnSeat = null,
            statusMessage = if (made) {
                "Bid $bid made with ${teamPoints[biddingTeam]} pts"
            } else {
                "Bid $bid failed with ${teamPoints[biddingTeam]} pts"
            },
        )
        return state
    }

    private fun validatePlay(player: Player, card: Card) {
        val legal = legalPlaysFor(player.seat)
        require(card in legal || (state.phase == GamePhase.FIRST_PHASE && card == state.concealedTrumpCard)) {
            "Illegal play ${card.shortLabel}"
        }
    }

    private fun dealAround(players: List<Player>, count: Int): List<Player> {
        val mutable = players.toMutableList()
        var seat = nextSeat(dealerSeat())
        repeat(4) {
            val drawn = deck.draw(count)
            val p = mutable[seat]
            mutable[seat] = p.withHand(p.hand + drawn)
            seat = nextSeat(seat)
        }
        return mutable
    }

    private fun dealerSeat(): Int = state.players.first { it.isDealer }.seat

    private fun nextSeat(seat: Int): Int = if (seat == 0) 3 else seat - 1 // counter-clockwise

    private fun advanceBidder() {
        val next = nextSeat(state.bidToActSeat ?: return)
        state = state.copy(
            bidToActSeat = next,
            canPass = state.highestBidderSeat != null || next != nextSeat(dealerSeat()),
        )
    }

    private fun maybeAutoBid() {
        if (!state.offlineMode) return
        val seat = state.bidToActSeat ?: return
        if (state.players[seat].isLocal) return
        // Simple bots: pass if auction started, else bid min
        if (state.highestBidderSeat == null && !state.canPass) {
            placeBid(Rank.MIN_BID)
        } else {
            passBid()
        }
    }

    private fun maybeAutoChooseTrump() {
        if (!state.offlineMode) return
        val seat = state.highestBidderSeat ?: return
        if (state.players[seat].isLocal) return
        val hand = state.players[seat].hand
        val card = hand.maxByOrNull { c -> hand.count { it.suit == c.suit } * 10 + (4 - c.rank.ordinal) }
            ?: return
        chooseTrump(card)
    }

    private fun maybeAutoPlay() {
        if (!state.offlineMode) return
        var guard = 0
        while (guard++ < 16) {
            val seat = state.turnSeat ?: return
            if (state.phase != GamePhase.FIRST_PHASE && state.phase != GamePhase.SECOND_PHASE) return
            if (state.players[seat].isLocal) return
            val legal = legalPlaysFor(seat)
            val card = legal.maxByOrNull { it.points * 10 - it.rank.ordinal } ?: return
            playCard(card)
        }
    }

    private fun requirePhase(vararg phases: GamePhase) {
        require(state.phase in phases) { "Invalid phase ${state.phase}, expected ${phases.toList()}" }
    }
}
