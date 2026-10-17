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
import kotlin.math.abs
import kotlin.random.Random

/**
 * Complete 4-player Thuruppukali / 28 engine (README rules).
 *
 * Partnerships: seats 0+2 vs 1+3. Deal & play counter-clockwise.
 */
class ThuruppuGame(
    private var random: Random = Random.Default,
) {
    private var deck = Deck(random)
    private var state = GameSnapshot()
    private var consecutivePasses = 0

    fun snapshot(): GameSnapshot = state

    fun createOfflineTable(localName: String = "You"): GameSnapshot {
        state = GameSnapshot(
            phase = GamePhase.READY_TO_DEAL,
            tableId = "OFFLINE",
            players = fourPlayers(localName),
            localSeat = 0,
            dealerSeat = 0,
            offlineMode = true,
            statusMessage = "4 players ready — shuffle & deal",
        )
        return state
    }

    fun createOnlineHost(localName: String, tableId: String): GameSnapshot {
        state = GameSnapshot(
            phase = GamePhase.WAITING_FOR_PLAYERS,
            tableId = tableId,
            players = listOf(Player("host", localName, 0, isDealer = true, isLocal = true)),
            localSeat = 0,
            dealerSeat = 0,
            offlineMode = false,
            statusMessage = "Table $tableId — waiting for 3 friends",
        )
        return state
    }

    fun joinOnline(localName: String, tableId: String, seat: Int): GameSnapshot {
        state = GameSnapshot(
            phase = GamePhase.WAITING_FOR_PLAYERS,
            tableId = tableId,
            players = listOf(Player("me", localName, seat, isLocal = true)),
            localSeat = seat,
            offlineMode = false,
            statusMessage = "Joined $tableId",
        )
        return state
    }

    fun replacePlayers(players: List<Player>): GameSnapshot {
        val sorted = players.sortedBy { it.seat }
        state = state.copy(
            players = sorted,
            phase = if (sorted.size == 4) GamePhase.READY_TO_DEAL else GamePhase.WAITING_FOR_PLAYERS,
            statusMessage = if (sorted.size == 4) "All seated" else "Players ${sorted.size}/4",
        )
        return state
    }

    fun shuffle(): GameSnapshot {
        requirePhase(GamePhase.READY_TO_DEAL, GamePhase.ROUND_OVER, GamePhase.CANCELLED)
        deck = Deck(random)
        deck.shuffle()
        state = state.copy(
            phase = GamePhase.READY_TO_DEAL,
            cancelReason = null,
            statusMessage = "Deck shuffled — deal when ready",
        )
        return state
    }

    fun dealFirstFour(): GameSnapshot {
        requirePhase(GamePhase.READY_TO_DEAL, GamePhase.ROUND_OVER, GamePhase.CANCELLED)
        prepareFreshHands()
        if (deck.size() != 32) {
            deck = Deck(random)
            deck.shuffle()
        }
        var players = dealAround(clearedHands(state.players), count = 4)

        // Pointless first packet for first-to-speak → reshuffle
        val firstSeat = nextSeat(state.dealerSeat)
        if (HandStrength.isPointless(players[firstSeat].hand)) {
            state = state.copy(
                phase = GamePhase.CANCELLED,
                players = players,
                cancelReason = "${players[firstSeat].displayName} was dealt 0 points — reshuffle",
                statusMessage = "Hand cancelled: first bidder packet is pointless",
            )
            return state
        }

        consecutivePasses = 0
        state = state.copy(
            phase = GamePhase.BIDDING,
            players = players,
            currentBid = Rank.MIN_BID - 1,
            highestBidderSeat = null,
            bidToActSeat = firstSeat,
            canPass = false,
            bids = emptyList(),
            thani = false,
            trumpSuit = null,
            concealedTrumpCard = null,
            trumpExposed = false,
            currentTrick = emptyList(),
            completedTricks = emptyList(),
            tricksPlayed = 0,
            lastRound = null,
            stakeMultiplier = 1,
            doubled = false,
            redoubled = false,
            canDouble = false,
            canRedouble = false,
            canExposeTrump = false,
            cancelReason = null,
            statusMessage = "${players[firstSeat].displayName} must open (≥${Rank.MIN_BID})",
        )
        maybeAutoAct()
        return state
    }

    fun placeBid(amount: Int): GameSnapshot {
        requirePhase(GamePhase.BIDDING)
        val seat = state.bidToActSeat ?: error("No bidder")
        require(amount in (state.currentBid + 1)..Rank.MAX_BID)
        consecutivePasses = 0
        val players = state.players.map {
            if (it.seat == seat) it.copy(bid = amount) else it
        }
        state = state.copy(
            players = players,
            currentBid = amount,
            highestBidderSeat = seat,
            canPass = true,
            thani = false,
            bids = state.bids + BidAction(seat, amount, passed = false),
            statusMessage = "${players[seat].displayName} bids $amount",
        )
        advanceBidder()
        maybeAutoAct()
        return state
    }

    /** Thani = bidder plays alone (partner sits out conceptually; still 4 hands for simplicity we keep partner inactive scoring as same team alone). */
    fun declareThani(): GameSnapshot {
        requirePhase(GamePhase.BIDDING)
        val seat = state.bidToActSeat ?: error("No bidder")
        consecutivePasses = 0
        val amount = maxOf(state.currentBid + 1, Rank.MIN_BID)
        val players = state.players.map {
            if (it.seat == seat) it.copy(bid = amount) else it
        }
        state = state.copy(
            players = players,
            currentBid = amount,
            highestBidderSeat = seat,
            thani = true,
            canPass = true,
            bids = state.bids + BidAction(seat, amount, passed = false, thani = true),
            statusMessage = "${players[seat].displayName} declares Thani at $amount",
        )
        // Thani ends auction immediately
        return finishBidding()
    }

    fun passBid(): GameSnapshot {
        requirePhase(GamePhase.BIDDING)
        val seat = state.bidToActSeat ?: error("No bidder")
        require(state.canPass || state.highestBidderSeat != null) { "Opener cannot pass" }
        consecutivePasses++
        state = state.copy(
            bids = state.bids + BidAction(seat, null, passed = true),
            statusMessage = "${state.players[seat].displayName} passes",
        )
        if (state.highestBidderSeat != null && consecutivePasses >= 3) {
            return finishBidding()
        }
        // All four passed without a bid — cancel / redeal
        if (state.highestBidderSeat == null && consecutivePasses >= 4) {
            state = state.copy(
                phase = GamePhase.CANCELLED,
                cancelReason = "All passed — redeal",
                statusMessage = "Auction failed — reshuffle",
            )
            return state
        }
        advanceBidder()
        // After opener, everyone may pass
        if (state.highestBidderSeat == null) {
            state = state.copy(canPass = true)
        }
        maybeAutoAct()
        return state
    }

    fun chooseTrump(card: Card): GameSnapshot {
        requirePhase(GamePhase.CHOOSING_TRUMP)
        val bidderSeat = state.highestBidderSeat ?: error("No bidder")
        val bidder = state.players[bidderSeat]
        require(card in bidder.hand) { "Trump must come from the first four" }
        val players = state.players.map {
            if (it.seat == bidderSeat) it.withoutCard(card).copy(isTrumpChooser = true) else it
        }
        // High bids (≥21) are worth 2 game points before any double
        val baseStake = if (state.currentBid >= 21) 2 else 1
        state = state.copy(
            phase = GamePhase.DOUBLING,
            players = players,
            trumpSuit = card.suit,
            concealedTrumpCard = card,
            trumpExposed = false,
            stakeMultiplier = baseStake,
            canDouble = true,
            canRedouble = false,
            doubled = false,
            redoubled = false,
            statusMessage = "${bidder.displayName} set concealed trump — opponents may Double",
        )
        maybeAutoAct()
        return state
    }

    fun declareDouble(): GameSnapshot {
        requirePhase(GamePhase.DOUBLING)
        require(state.canDouble && !state.doubled)
        val bidderTeam = (state.highestBidderSeat ?: 0) % 2
        // Only defending team may double; in UI we gate by seat
        val next = if (state.currentBid >= 21) {
            // Already worth 2; double raises to 4 (treated as redouble ceiling)
            state.copy(
                doubled = true,
                redoubled = true,
                stakeMultiplier = 4,
                canDouble = false,
                canRedouble = false,
                statusMessage = "Doubled to 4 game points — dealing remaining cards",
            )
        } else {
            state.copy(
                doubled = true,
                stakeMultiplier = 2,
                canDouble = false,
                canRedouble = true,
                statusMessage = "Doubled! Bidding side may Redouble",
            )
        }
        state = next
        if (!state.canRedouble) {
            return dealSecondFour()
        }
        maybeAutoAct()
        return state
    }

    fun declareRedouble(): GameSnapshot {
        requirePhase(GamePhase.DOUBLING)
        require(state.canRedouble && state.doubled && !state.redoubled)
        state = state.copy(
            redoubled = true,
            stakeMultiplier = 4,
            canRedouble = false,
            canDouble = false,
            statusMessage = "Redoubled to 4 — dealing remaining cards",
        )
        return dealSecondFour()
    }

    fun skipDoubling(): GameSnapshot {
        requirePhase(GamePhase.DOUBLING)
        state = state.copy(canDouble = false, canRedouble = false)
        return dealSecondFour()
    }

    fun playCard(card: Card): GameSnapshot {
        requirePhase(GamePhase.FIRST_PHASE, GamePhase.SECOND_PHASE)
        val seat = state.turnSeat ?: error("No turn")
        val player = state.players[seat]
        val legal = legalPlaysFor(seat)
        require(card in legal) { "Illegal play ${card.shortLabel}" }

        // Last remaining concealed trump played as a normal card
        if (card == state.concealedTrumpCard && card !in player.hand) {
            state = state.copy(
                phase = GamePhase.SECOND_PHASE,
                trumpExposed = true,
                concealedTrumpCard = null,
                players = state.players.map {
                    if (it.seat == seat) it.copy(hand = listOf(card)) else it
                },
            )
            cancelIfDefendersVoidOfTrump()?.let { return it }
            return playCard(card)
        }

        var players = state.players.toMutableList()
        players[seat] = player.withoutCard(card)

        val ledSuit = state.ledSuit ?: card.suit
        val trick = state.currentTrick + TrickPlay(seat, card)
        state = state.copy(
            players = players,
            currentTrick = trick,
            ledSuit = ledSuit,
            canExposeTrump = false,
            statusMessage = "${player.displayName} plays ${card.shortLabel}",
        )

        if (trick.size == 4) return resolveTrick()
        state = state.copy(
            turnSeat = nextSeat(seat),
            canExposeTrump = canCurrentSeatExpose(),
        )
        maybeAutoAct()
        return state
    }

    fun exposeTrumpAndPlay(card: Card): GameSnapshot {
        requirePhase(GamePhase.FIRST_PHASE)
        val seat = state.turnSeat ?: error("No turn")
        require(canCurrentSeatExpose()) { "Cannot expose trump now" }
        val trump = state.trumpSuit ?: error("No trump")
        val player = state.players[seat]
        val handAfterMerge = if (seat == state.highestBidderSeat) {
            (player.hand + listOfNotNull(state.concealedTrumpCard)).distinct()
        } else {
            player.hand
        }
        require(card in handAfterMerge) { "Card not available" }
        require(card.suit == trump || handAfterMerge.none { it.suit == trump }) {
            "Must play a trump after exposing if you hold one"
        }

        var players = state.players.toMutableList()
        players[seat] = player.copy(hand = handAfterMerge.filterNot { it == card }.sorted())

        val ledSuit = state.ledSuit ?: card.suit
        val trick = state.currentTrick + TrickPlay(seat, card)
        state = state.copy(
            phase = GamePhase.SECOND_PHASE,
            trumpExposed = true,
            concealedTrumpCard = null,
            players = players,
            currentTrick = trick,
            ledSuit = ledSuit,
            canExposeTrump = false,
            statusMessage = "Trump is ${trump.displayName}! ${player.displayName} plays ${card.shortLabel}",
        )

        // Defenders hold no trump → cancel
        cancelIfDefendersVoidOfTrump()?.let { return it }

        if (trick.size == 4) return resolveTrick()
        state = state.copy(turnSeat = nextSeat(seat))
        maybeAutoAct()
        return state
    }

    fun legalPlaysFor(seat: Int): List<Card> {
        val player = state.players.getOrNull(seat) ?: return emptyList()
        var hand = player.hand
        // Last-trick force: bidder's only remaining card is the concealed trump
        if (seat == state.highestBidderSeat &&
            state.concealedTrumpCard != null &&
            !state.trumpExposed &&
            hand.isEmpty() &&
            state.tricksPlayed == 7
        ) {
            return listOf(state.concealedTrumpCard!!)
        }
        if (seat == state.highestBidderSeat && state.trumpExposed && state.concealedTrumpCard != null) {
            hand = hand // already merged on expose
        }

        val led = state.ledSuit
        if (led == null) {
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
        if (followers.isNotEmpty()) return followers

        // Void of led suit
        if (state.phase == GamePhase.SECOND_PHASE && state.trumpSuit != null) {
            val trump = state.trumpSuit!!
            val highestTrumpInTrick = state.currentTrick
                .map { it.card }
                .filter { it.suit == trump }
                .minByOrNull { it.rank.ordinal }
            val trumps = hand.filter { it.suit == trump }
            if (highestTrumpInTrick != null && trumps.isNotEmpty()) {
                // Must overtrump or discard a non-trump — cannot undertrump
                val overs = trumps.filter { it.rank.ordinal < highestTrumpInTrick.rank.ordinal }
                val nonTrumps = hand.filter { it.suit != trump }
                return if (overs.isNotEmpty()) overs + nonTrumps else nonTrumps.ifEmpty { trumps }
            }
        }
        return hand
    }

    fun canCurrentSeatExpose(): Boolean {
        if (state.phase != GamePhase.FIRST_PHASE) return false
        if (state.trumpExposed || state.concealedTrumpCard == null) return false
        val seat = state.turnSeat ?: return false
        val led = state.ledSuit ?: return false // only when unable to follow (after lead exists)
        val player = state.players[seat]
        // May call trump when void of led suit
        return player.hand.none { it.suit == led }
    }

    fun startNextRound(): GameSnapshot {
        requirePhase(GamePhase.ROUND_OVER, GamePhase.MATCH_OVER, GamePhase.CANCELLED)
        if (state.phase == GamePhase.MATCH_OVER) {
            // Fresh match
            val names = state.players.map { it.displayName }
            val local = state.players.firstOrNull { it.isLocal }?.displayName ?: "You"
            return createOfflineTable(local).also {
                // keep names of bots if present
                if (names.size == 4) {
                    state = state.copy(
                        players = state.players.mapIndexed { i, p -> p.copy(displayName = names[i]) },
                    )
                }
            }
        }
        val newDealer = nextSeat(state.dealerSeat)
        val players = state.players.map {
            it.copy(
                hand = emptyList(),
                wonCards = emptyList(),
                bid = null,
                isTrumpChooser = false,
                isDealer = it.seat == newDealer,
            )
        }
        state = state.copy(
            phase = GamePhase.READY_TO_DEAL,
            players = players,
            dealerSeat = newDealer,
            currentBid = Rank.MIN_BID - 1,
            highestBidderSeat = null,
            bidToActSeat = null,
            bids = emptyList(),
            trumpSuit = null,
            concealedTrumpCard = null,
            trumpExposed = false,
            currentTrick = emptyList(),
            completedTricks = emptyList(),
            tricksPlayed = 0,
            lastRound = null,
            stakeMultiplier = 1,
            doubled = false,
            redoubled = false,
            canDouble = false,
            canRedouble = false,
            canExposeTrump = false,
            cancelReason = null,
            thani = false,
            statusMessage = "Dealer is now ${players[newDealer].displayName} — shuffle & deal",
        )
        return state
    }

    // --- internals ---

    private fun finishBidding(): GameSnapshot {
        val winner = state.highestBidderSeat ?: error("No bid")
        state = state.copy(
            phase = GamePhase.CHOOSING_TRUMP,
            bidToActSeat = null,
            players = state.players.map { it.copy(isTrumpChooser = it.seat == winner) },
            statusMessage = "${state.players[winner].displayName} won at ${state.currentBid} — choose concealed trump",
        )
        maybeAutoAct()
        return state
    }

    private fun dealSecondFour(): GameSnapshot {
        val players = dealAround(state.players, count = 4)

        // Cancel: 0-point full hand, 4 jacks, mono suit
        for (p in players) {
            when {
                HandStrength.isPointless(p.hand) -> {
                    state = state.copy(
                        phase = GamePhase.CANCELLED,
                        players = players,
                        cancelReason = "${p.displayName} has 8 worthless cards",
                        statusMessage = "Round cancelled — reshuffle",
                    )
                    return state
                }
                HandStrength.isAllJacks(p.hand) -> {
                    state = state.copy(
                        phase = GamePhase.CANCELLED,
                        players = players,
                        cancelReason = "${p.displayName} holds all four Jacks",
                        statusMessage = "Round cancelled — reshuffle",
                    )
                    return state
                }
                HandStrength.isMonoSuit(p.hand) -> {
                    state = state.copy(
                        phase = GamePhase.CANCELLED,
                        players = players,
                        cancelReason = "${p.displayName} has all eight of one suit",
                        statusMessage = "Round cancelled — reshuffle",
                    )
                    return state
                }
            }
        }

        val lead = nextSeat(state.dealerSeat)
        state = state.copy(
            phase = GamePhase.FIRST_PHASE,
            players = players,
            leadSeat = lead,
            turnSeat = lead,
            currentTrick = emptyList(),
            ledSuit = null,
            canExposeTrump = false,
            statusMessage = "${players[lead].displayName} leads — trump still concealed",
        )
        maybeAutoAct()
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
        val players = state.players.map {
            if (it.seat == winner.seat) it.withWon(trick.map { t -> t.card }) else it
        }
        val tricksPlayed = state.tricksPlayed + 1
        state = state.copy(
            players = players,
            completedTricks = state.completedTricks + listOf(trick),
            currentTrick = emptyList(),
            ledSuit = null,
            leadSeat = winner.seat,
            turnSeat = winner.seat,
            tricksPlayed = tricksPlayed,
            canExposeTrump = false,
            statusMessage = "${players[winner.seat].displayName} wins trick $tricksPlayed",
        )
        if (tricksPlayed >= 8) return scoreRound()

        // Force: if 7 tricks done and trump still down, ensure bidder can/must play it next
        if (tricksPlayed == 7 && !state.trumpExposed && state.concealedTrumpCard != null) {
            val bidder = state.highestBidderSeat!!
            if (state.turnSeat == bidder && state.players[bidder].hand.isEmpty()) {
                // Auto-expose into last trick lead context handled in legalPlays / play
                state = state.copy(
                    phase = GamePhase.SECOND_PHASE,
                    trumpExposed = true,
                    statusMessage = "Final trick — trump must come out",
                )
                // Put concealed into bidder hand for play
                val merged = state.players.toMutableList()
                val card = state.concealedTrumpCard!!
                merged[bidder] = merged[bidder].copy(hand = listOf(card))
                state = state.copy(players = merged, concealedTrumpCard = null)
                cancelIfDefendersVoidOfTrump()?.let { return it }
            }
        }

        state = state.copy(canExposeTrump = canCurrentSeatExpose())
        maybeAutoAct()
        return state
    }

    private fun scoreRound(): GameSnapshot {
        val bidderSeat = state.highestBidderSeat ?: 0
        val biddingTeam = bidderSeat % 2
        val teamPoints = intArrayOf(
            state.teamPointsOnTable(0),
            state.teamPointsOnTable(1),
        )
        val bid = state.currentBid
        val made = teamPoints[biddingTeam] >= bid
        val halfFail = !made && teamPoints[biddingTeam] < (bid + 1) / 2
        var delta = state.stakeMultiplier
        if (!made) {
            delta = -delta
            if (halfFail) delta *= 2
        }
        // Only bidding team's chaka moves
        val scores = state.teamScores.toMutableMap()
        scores[biddingTeam] = (scores[biddingTeam] ?: 0) + delta
        val round = RoundScore(
            biddingTeam = biddingTeam,
            bid = bid,
            biddingTeamPoints = teamPoints[biddingTeam],
            defendingTeamPoints = teamPoints[1 - biddingTeam],
            bidMade = made,
            gamePointsDelta = delta,
            stakeMultiplier = state.stakeMultiplier,
            halfFail = halfFail,
        )
        val matchOver = scores.values.any { abs(it) >= 6 }
        state = state.copy(
            phase = if (matchOver) GamePhase.MATCH_OVER else GamePhase.ROUND_OVER,
            teamScores = scores,
            lastRound = round,
            turnSeat = null,
            canExposeTrump = false,
            statusMessage = buildString {
                append(if (made) "Made $bid with ${teamPoints[biddingTeam]} pts" else "Failed $bid with ${teamPoints[biddingTeam]} pts")
                append(" (${if (delta > 0) "+$delta" else "$delta"} chaka)")
                if (halfFail) append(" — half-fail")
                if (matchOver) append(" · MATCH OVER")
            },
        )
        return state
    }

    private fun cancelIfDefendersVoidOfTrump(): GameSnapshot? {
        val trump = state.trumpSuit ?: return null
        if (!state.trumpExposed) return null
        val bidderTeam = (state.highestBidderSeat ?: 0) % 2
        val defenderTrumpInHands = state.players
            .filter { it.team != bidderTeam }
            .sumOf { p -> p.hand.count { it.suit == trump } }
        val defendersPlayedTrump = state.completedTricks.flatten().any {
            it.card.suit == trump && state.players[it.seat].team != bidderTeam
        } || state.currentTrick.any {
            it.card.suit == trump && state.players[it.seat].team != bidderTeam
        }
        val trumpSeen = state.players.sumOf { p ->
            p.hand.count { it.suit == trump } + p.wonCards.count { it.suit == trump }
        } + state.currentTrick.count { it.card.suit == trump } +
            if (state.concealedTrumpCard?.suit == trump) 1 else 0
        if (trumpSeen == 8 && defenderTrumpInHands == 0 && !defendersPlayedTrump) {
            state = state.copy(
                phase = GamePhase.CANCELLED,
                cancelReason = "Defending pair has no trump",
                statusMessage = "Round cancelled — defenders void of trump",
            )
            return state
        }
        return null
    }

    private fun advanceBidder() {
        val next = nextSeat(state.bidToActSeat ?: return)
        state = state.copy(
            bidToActSeat = next,
            canPass = state.highestBidderSeat != null || consecutivePasses > 0,
        )
    }

    private fun maybeAutoAct() {
        if (!state.offlineMode) return
        var guard = 0
        while (guard++ < 48) {
            when (state.phase) {
                GamePhase.BIDDING -> {
                    val seat = state.bidToActSeat ?: return
                    if (state.players[seat].isLocal) return
                    botBid(seat)
                }
                GamePhase.CHOOSING_TRUMP -> {
                    val seat = state.highestBidderSeat ?: return
                    if (state.players[seat].isLocal) return
                    val card = HandStrength.bestTrumpCandidate(state.players[seat].hand) ?: return
                    chooseTrump(card)
                }
                GamePhase.DOUBLING -> {
                    if (!botDoubling()) return // waiting for local human
                }
                GamePhase.FIRST_PHASE, GamePhase.SECOND_PHASE -> {
                    val seat = state.turnSeat ?: return
                    if (state.players[seat].isLocal) return
                    botPlay(seat)
                }
                else -> return
            }
        }
    }

    private fun botBid(seat: Int) {
        val hand = state.players[seat].hand
        val suggestion = HandStrength.suggestedBid(hand)
        val min = state.currentBid + 1
        when {
            state.highestBidderSeat == null && !state.canPass -> {
                placeBid(suggestion?.coerceAtLeast(Rank.MIN_BID) ?: Rank.MIN_BID)
            }
            suggestion != null && suggestion >= min && suggestion <= Rank.MAX_BID &&
                random.nextFloat() < 0.55f -> {
                placeBid(suggestion.coerceIn(min, Rank.MAX_BID))
            }
            else -> passBid()
        }
    }

    /** @return false when a local human must decide. */
    private fun botDoubling(): Boolean {
        val bidder = state.highestBidderSeat ?: return false
        val local = state.localSeat
        if (state.canDouble) {
            val defendersAreLocal = local % 2 != bidder % 2
            if (defendersAreLocal) return false
            if (state.currentBid >= 20 && random.nextFloat() < 0.35f) {
                declareDouble()
            } else {
                skipDoubling()
            }
            return true
        }
        if (state.canRedouble) {
            val biddingIsLocal = local % 2 == bidder % 2
            if (biddingIsLocal) return false
            if (random.nextFloat() < 0.25f) declareRedouble() else skipDoubling()
            return true
        }
        skipDoubling()
        return true
    }

    private fun botPlay(seat: Int) {
        if (state.canExposeTrump && canCurrentSeatExpose() && random.nextFloat() < 0.4f) {
            val trump = state.trumpSuit!!
            val hand = if (seat == state.highestBidderSeat) {
                state.players[seat].hand + listOfNotNull(state.concealedTrumpCard)
            } else {
                state.players[seat].hand
            }
            val card = hand.filter { it.suit == trump }.minByOrNull { it.rank.ordinal }
                ?: hand.minByOrNull { it.points }
                ?: return
            exposeTrumpAndPlay(card)
            return
        }
        // Force last concealed
        if (seat == state.highestBidderSeat &&
            state.concealedTrumpCard != null &&
            !state.trumpExposed &&
            state.players[seat].hand.isEmpty()
        ) {
            val card = state.concealedTrumpCard!!
            // Merge and play via expose path
            state = state.copy(
                phase = GamePhase.SECOND_PHASE,
                trumpExposed = true,
                players = state.players.map {
                    if (it.seat == seat) it.copy(hand = listOf(card)) else it
                },
                concealedTrumpCard = null,
            )
            playCard(card)
            return
        }
        val legal = legalPlaysFor(seat)
        val card = pickBotCard(seat, legal) ?: return
        playCard(card)
    }

    private fun pickBotCard(seat: Int, legal: List<Card>): Card? {
        if (legal.isEmpty()) return null
        val led = state.ledSuit
        if (led == null) {
            // Lead low non-trump if possible in phase 1
            return legal.minWith(compareBy<Card> { it.points }.thenByDescending { it.rank.ordinal })
        }
        val followers = legal.filter { it.suit == led }
        if (followers.isNotEmpty()) {
            val winning = state.currentTrick.fold(state.currentTrick.firstOrNull()) { best, play ->
                when {
                    best == null -> play
                    play.card.beats(best.card, led, state.trumpSuit, state.trumpExposed) -> play
                    else -> best
                }
            }
            val partnerWinning = winning?.seat?.let { it % 2 == seat % 2 } == true
            return if (partnerWinning) {
                followers.minByOrNull { it.points } ?: followers.last()
            } else {
                followers.minByOrNull { it.rank.ordinal } // highest
                    ?: followers.first()
            }
        }
        return legal.minByOrNull { it.points } ?: legal.first()
    }

    private fun dealAround(players: List<Player>, count: Int): List<Player> {
        val mutable = players.toMutableList()
        var seat = nextSeat(state.dealerSeat)
        repeat(4) {
            val drawn = deck.draw(count)
            mutable[seat] = mutable[seat].withHand(mutable[seat].hand + drawn)
            seat = nextSeat(seat)
        }
        return mutable
    }

    private fun clearedHands(players: List<Player>) = players.map {
        it.copy(hand = emptyList(), wonCards = emptyList(), bid = null, isTrumpChooser = false)
    }

    private fun prepareFreshHands() {
        state = state.copy(
            players = clearedHands(state.players).map {
                it.copy(isDealer = it.seat == state.dealerSeat)
            },
        )
    }

    private fun nextSeat(seat: Int): Int = if (seat == 0) 3 else seat - 1

    private fun requirePhase(vararg phases: GamePhase) {
        require(state.phase in phases) { "Invalid phase ${state.phase}, expected ${phases.toList()}" }
    }

    private fun fourPlayers(localName: String) = listOf(
        Player("p0", localName, 0, isDealer = true, isLocal = true),
        Player("p1", "East", 1),
        Player("p2", "Partner", 2),
        Player("p3", "West", 3),
    )
}
