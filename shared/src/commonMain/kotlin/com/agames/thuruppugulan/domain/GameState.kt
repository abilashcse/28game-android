package com.agames.thuruppugulan.domain

enum class GamePhase {
    LOBBY,
    WAITING_FOR_PLAYERS,
    READY_TO_DEAL,
    BIDDING,
    CHOOSING_TRUMP,
    DOUBLING,
    FIRST_PHASE,
    SECOND_PHASE,
    ROUND_OVER,
    MATCH_OVER,
    CANCELLED,
}

data class TrickPlay(
    val seat: Int,
    val card: Card,
)

data class BidAction(
    val seat: Int,
    val amount: Int?,
    val passed: Boolean,
    val thani: Boolean = false,
)

data class RoundScore(
    val biddingTeam: Int,
    val bid: Int,
    val biddingTeamPoints: Int,
    val defendingTeamPoints: Int,
    val bidMade: Boolean,
    val gamePointsDelta: Int,
    val stakeMultiplier: Int = 1,
    val halfFail: Boolean = false,
)

/**
 * Snapshot of a full 4-player Thuruppukali / 28 table.
 *
 * Seats 0+2 are partners (team 0); seats 1+3 are partners (team 1).
 * Play and deal are counter-clockwise: 0 → 3 → 2 → 1 → 0.
 */
data class GameSnapshot(
    val phase: GamePhase = GamePhase.LOBBY,
    val tableId: String? = null,
    val players: List<Player> = emptyList(),
    val localSeat: Int = 0,
    val dealerSeat: Int = 0,
    val currentBid: Int = Rank.MIN_BID - 1,
    val highestBidderSeat: Int? = null,
    val bidToActSeat: Int? = null,
    val canPass: Boolean = false,
    val bids: List<BidAction> = emptyList(),
    val thani: Boolean = false,
    val trumpSuit: Suit? = null,
    val concealedTrumpCard: Card? = null,
    val trumpExposed: Boolean = false,
    val currentTrick: List<TrickPlay> = emptyList(),
    val completedTricks: List<List<TrickPlay>> = emptyList(),
    val ledSuit: Suit? = null,
    val leadSeat: Int? = null,
    val turnSeat: Int? = null,
    val tricksPlayed: Int = 0,
    /** Team pip scores (chaka). Positive = red, negative = black. Match ends at ±6. */
    val teamScores: Map<Int, Int> = mapOf(0 to 0, 1 to 0),
    val lastRound: RoundScore? = null,
    /** 1 normal, 2 doubled, 4 redoubled. Bid ≥21 starts at base 2 before doubles. */
    val stakeMultiplier: Int = 1,
    val canDouble: Boolean = false,
    val canRedouble: Boolean = false,
    val doubled: Boolean = false,
    val redoubled: Boolean = false,
    val canExposeTrump: Boolean = false,
    val cancelReason: String? = null,
    val statusMessage: String = "Welcome to Thuruppugulan",
    val offlineMode: Boolean = true,
) {
    fun playerAt(seat: Int): Player? = players.getOrNull(seat)

    fun teamPointsOnTable(team: Int): Int =
        players.filter { it.team == team }.sumOf { it.pointsWon() } +
            if (concealedTrumpCard != null && (highestBidderSeat ?: -1) % 2 == team) {
                concealedTrumpCard.points
            } else {
                0
            }
}
