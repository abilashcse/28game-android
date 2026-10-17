package com.agames.thuruppugulan.session

import com.agames.thuruppugulan.domain.Card
import com.agames.thuruppugulan.domain.GamePhase
import com.agames.thuruppugulan.domain.GameSnapshot
import com.agames.thuruppugulan.domain.Rank
import com.agames.thuruppugulan.engine.ThuruppuGame
import com.agames.thuruppugulan.network.GameSocket
import com.agames.thuruppugulan.network.HubBroadcast
import com.agames.thuruppugulan.network.MessageType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

/** UI-facing façade over [ThuruppuGame] for a perfect 4-player table. */
class GameController(
    private val game: ThuruppuGame = ThuruppuGame(),
    private val socket: GameSocket = GameSocket(),
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(game.snapshot())
    val state: StateFlow<GameSnapshot> = _state.asStateFlow()

    fun startOffline(playerName: String = "You") {
        publish(game.createOfflineTable(playerName))
    }

    fun startOnlineHost(playerName: String, tableId: String = randomTableId()) {
        publish(game.createOnlineHost(playerName, tableId))
        scope.launch {
            runCatching {
                socket.connect(playerName, tableId)
                socket.broadcast(
                    HubBroadcast(
                        tableId = tableId,
                        msg = MessageType.JOINED,
                        player = _state.value.players.first(),
                    ),
                )
            }.onFailure { err ->
                _state.update { it.copy(statusMessage = "Online failed: ${err.message}. Use offline.") }
            }
        }
    }

    fun joinOnline(playerName: String, tableId: String) {
        publish(game.joinOnline(playerName, tableId, seat = 1))
        scope.launch {
            runCatching {
                socket.connect(playerName, tableId)
                socket.broadcast(HubBroadcast(tableId = tableId, msg = MessageType.JOINED))
            }.onFailure { err ->
                _state.update { it.copy(statusMessage = "Join failed: ${err.message}") }
            }
        }
    }

    fun shuffle() = publish(game.shuffle())
    fun deal() = publish(game.dealFirstFour())
    fun bid(amount: Int) = publish(game.placeBid(amount))
    fun thani() = publish(game.declareThani())
    fun pass() = publish(game.passBid())
    fun chooseTrump(card: Card) = publish(game.chooseTrump(card))
    fun doubleStake() = publish(game.declareDouble())
    fun redoubleStake() = publish(game.declareRedouble())
    fun skipDoubling() = publish(game.skipDoubling())
    fun play(card: Card) = publish(game.playCard(card))
    fun exposeAndPlay(card: Card) = publish(game.exposeTrumpAndPlay(card))
    fun newRound() = publish(game.startNextRound())

    fun legalPlays(): List<Card> {
        val snap = _state.value
        val seat = snap.turnSeat ?: return emptyList()
        if (seat != snap.localSeat) return emptyList()
        return game.legalPlaysFor(seat)
    }

    fun isLocalTurn(): Boolean {
        val snap = _state.value
        val local = snap.localSeat
        return when (snap.phase) {
            GamePhase.BIDDING -> snap.bidToActSeat == local
            GamePhase.CHOOSING_TRUMP -> snap.highestBidderSeat == local
            GamePhase.DOUBLING -> {
                val bidderTeam = (snap.highestBidderSeat ?: 0) % 2
                val localTeam = local % 2
                when {
                    snap.canDouble && localTeam != bidderTeam -> true
                    snap.canRedouble && localTeam == bidderTeam -> true
                    else -> false
                }
            }
            GamePhase.FIRST_PHASE, GamePhase.SECOND_PHASE -> snap.turnSeat == local
            else -> false
        }
    }

    fun bidOptions(): List<Int> {
        val min = (_state.value.currentBid + 1).coerceAtLeast(Rank.MIN_BID)
        return (min..Rank.MAX_BID).toList()
    }

    fun dispose() {
        scope.launch { socket.close() }
    }

    private fun publish(snapshot: GameSnapshot) {
        _state.value = snapshot
    }

    private fun randomTableId(): String =
        Random.nextInt(0, 10000).toString().padStart(4, '0')
}
