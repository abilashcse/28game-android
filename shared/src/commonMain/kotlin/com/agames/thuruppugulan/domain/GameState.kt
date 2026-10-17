package com.agames.thuruppugulan.domain

enum class GamePhase {
    LOBBY,
    WAITING_FOR_PLAYERS,
    READY_TO_DEAL,
    DEALING_FIRST4,
    BIDDING,
    DEALING_LAST4,
    CHOOSING_TRUMP,
    FIRST_PHASE,
    SECOND_PHASE,
    ROUND_OVER,
    MATCH_OVER,
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
)

data class GameSnapshot(
    val phase: GamePhase = GamePhase.LOBBY,
    val tableId: String? = null,
    val players: List<Player> = emptyList(),
    val localSeat: Int = 0,
    val currentBid: Int = Rank.MIN_BID - 1,
    val highestBidderSeat: Int? = null,
    val bidToActSeat: Int? = null,
    val canPass: Boolean = false,
    val bids: List<BidAction> = emptyList(),
    val trumpSuit: Suit? = null,
    val concealedTrumpCard: Card? = null,
    val trumpExposed: Boolean = false,
    val currentTrick: List<TrickPlay> = emptyList(),
    val ledSuit: Suit? = null,
    val leadSeat: Int? = null,
    val turnSeat: Int? = null,
    val tricksPlayed: Int = 0,
    val teamScores: Map<Int, Int> = mapOf(0 to 0, 1 to 0),
    val lastRound: RoundScore? = null,
    val statusMessage: String = "Welcome to Thuruppugulan",
    val offlineMode: Boolean = true,
)
