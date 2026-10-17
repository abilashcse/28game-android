package com.agames.thuruppugulan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agames.thuruppugulan.domain.Card
import com.agames.thuruppugulan.domain.GamePhase
import com.agames.thuruppugulan.domain.GameSnapshot
import com.agames.thuruppugulan.domain.Player
import com.agames.thuruppugulan.ui.components.PlayingCard
import com.agames.thuruppugulan.ui.theme.TableColors

@Composable
fun TableScreen(
    snapshot: GameSnapshot,
    legalPlays: List<Card>,
    bidOptions: List<Int>,
    isLocalTurn: Boolean,
    onShuffle: () -> Unit,
    onDeal: () -> Unit,
    onBid: (Int) -> Unit,
    onThani: () -> Unit,
    onPass: () -> Unit,
    onChooseTrump: (Card) -> Unit,
    onDouble: () -> Unit,
    onRedouble: () -> Unit,
    onSkipDoubling: () -> Unit,
    onPlay: (Card) -> Unit,
    onExposeAndPlay: (Card) -> Unit,
    onNewRound: () -> Unit,
    onLeave: () -> Unit,
) {
    var showBidSheet by remember(snapshot.phase, snapshot.bidToActSeat, isLocalTurn) {
        mutableStateOf(snapshot.phase == GamePhase.BIDDING && isLocalTurn)
    }
    var selectingTrump by remember(snapshot.phase, isLocalTurn) {
        mutableStateOf(snapshot.phase == GamePhase.CHOOSING_TRUMP && isLocalTurn)
    }
    var exposeMode by remember(snapshot.canExposeTrump, isLocalTurn) {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TableColors.feltDeep)
            .padding(12.dp),
    ) {
        TopBar(snapshot, onLeave)
        Spacer(modifier = Modifier.height(8.dp))
        ScoreRow(snapshot)
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(TableColors.felt)
                .padding(12.dp),
        ) {
            val seats = seatViews(snapshot)
            seats["top"]?.let { SeatBadge(it, snapshot, Modifier.align(Alignment.TopCenter)) }
            seats["left"]?.let { SeatBadge(it, snapshot, Modifier.align(Alignment.CenterStart)) }
            seats["right"]?.let { SeatBadge(it, snapshot, Modifier.align(Alignment.CenterEnd)) }

            TrickArea(snapshot, Modifier.align(Alignment.Center))

            Text(
                text = snapshot.statusMessage,
                color = TableColors.cream.copy(alpha = 0.92f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp, start = 8.dp, end = 8.dp),
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        ActionRow(
            snapshot = snapshot,
            isLocalTurn = isLocalTurn,
            exposeMode = exposeMode,
            onShuffle = onShuffle,
            onDeal = onDeal,
            onDouble = onDouble,
            onRedouble = onRedouble,
            onSkipDoubling = onSkipDoubling,
            onToggleExpose = { exposeMode = !exposeMode },
            onNewRound = onNewRound,
        )

        val local = snapshot.players.getOrNull(snapshot.localSeat)
        if (local != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when {
                    selectingTrump -> "Tap a card from your first four to set concealed trump"
                    exposeMode -> "Tap a card to expose trump and play it"
                    else -> "${local.displayName} · ${local.hand.size} cards · team ${local.team}"
                },
                color = TableColors.gold,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(local.hand, key = { "${it.suit}-${it.rank}" }) { card ->
                    val playable = card in legalPlays && isLocalTurn &&
                        snapshot.phase in listOf(GamePhase.FIRST_PHASE, GamePhase.SECOND_PHASE)
                    PlayingCard(
                        card = card,
                        selected = selectingTrump || (exposeMode && playable),
                        enabled = selectingTrump || playable || (exposeMode && isLocalTurn),
                        onClick = {
                            when {
                                selectingTrump -> {
                                    onChooseTrump(card)
                                    selectingTrump = false
                                }
                                exposeMode && snapshot.canExposeTrump -> {
                                    onExposeAndPlay(card)
                                    exposeMode = false
                                }
                                playable -> onPlay(card)
                            }
                        },
                    )
                }
            }
        }
    }

    if (showBidSheet && snapshot.phase == GamePhase.BIDDING && isLocalTurn) {
        BidSheet(
            options = bidOptions,
            canPass = snapshot.canPass,
            onBid = {
                onBid(it)
                showBidSheet = false
            },
            onThani = {
                onThani()
                showBidSheet = false
            },
            onPass = {
                onPass()
                showBidSheet = false
            },
            onDismiss = { showBidSheet = false },
        )
    }
}

@Composable
private fun TopBar(snapshot: GameSnapshot, onLeave: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text("Thuruppugulan", color = TableColors.gold, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(
                text = buildString {
                    append("4 players · ${snapshot.phase.name}")
                    snapshot.tableId?.let { append(" · $it") }
                    if (snapshot.thani) append(" · THANI")
                    if (snapshot.doubled) append(" · x${snapshot.stakeMultiplier}")
                },
                color = TableColors.cream.copy(alpha = 0.7f),
                fontSize = 12.sp,
            )
        }
        TextButton(onClick = onLeave) {
            Text("Leave", color = TableColors.cream)
        }
    }
}

@Composable
private fun ScoreRow(snapshot: GameSnapshot) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TableColors.felt)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text("Us ${chaka(snapshot.teamScores[0] ?: 0)}", color = TableColors.cream)
        Text("Them ${chaka(snapshot.teamScores[1] ?: 0)}", color = TableColors.cream)
        Text(
            text = when {
                snapshot.trumpExposed -> "Trump ${snapshot.trumpSuit?.symbol ?: "?"}"
                snapshot.trumpSuit != null -> "Trump ?"
                else -> "Tricks ${snapshot.tricksPlayed}/8"
            },
            color = TableColors.gold,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private fun chaka(score: Int): String = when {
    score > 0 -> "●".repeat(score.coerceAtMost(6)) + " +$score"
    score < 0 -> "○".repeat((-score).coerceAtMost(6)) + " $score"
    else -> "—"
}

@Composable
private fun SeatBadge(player: Player, snapshot: GameSnapshot, modifier: Modifier = Modifier) {
    val isTurn = snapshot.turnSeat == player.seat ||
        (snapshot.phase == GamePhase.BIDDING && snapshot.bidToActSeat == player.seat)
    Column(modifier = modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(
                    if (isTurn) TableColors.gold.copy(alpha = 0.45f)
                    else TableColors.gold.copy(alpha = 0.2f),
                )
                .padding(10.dp),
        ) {
            Text(player.displayName.take(1), color = TableColors.gold, fontWeight = FontWeight.Bold)
        }
        Text(player.displayName, color = TableColors.cream, fontSize = 12.sp)
        Text(
            "${player.hand.size}c · ${player.pointsWon()}pts",
            color = TableColors.cream.copy(alpha = 0.6f),
            fontSize = 11.sp,
        )
        if (player.isDealer) {
            Text("Dealer", color = TableColors.gold, fontSize = 10.sp)
        }
    }
}

@Composable
private fun TrickArea(snapshot: GameSnapshot, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (snapshot.currentTrick.isEmpty()) {
            Text("Trick", color = TableColors.cream.copy(alpha = 0.35f))
        } else {
            snapshot.currentTrick.forEach { play ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PlayingCard(card = play.card, width = 48.dp, height = 72.dp)
                    Text(
                        snapshot.players.getOrNull(play.seat)?.displayName?.take(3) ?: "?",
                        color = TableColors.cream.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionRow(
    snapshot: GameSnapshot,
    isLocalTurn: Boolean,
    exposeMode: Boolean,
    onShuffle: () -> Unit,
    onDeal: () -> Unit,
    onDouble: () -> Unit,
    onRedouble: () -> Unit,
    onSkipDoubling: () -> Unit,
    onToggleExpose: () -> Unit,
    onNewRound: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        when (snapshot.phase) {
            GamePhase.READY_TO_DEAL, GamePhase.CANCELLED -> {
                Button(
                    onClick = onShuffle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TableColors.felt,
                        contentColor = TableColors.cream,
                    ),
                ) { Text("Shuffle") }
                Button(
                    onClick = onDeal,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TableColors.gold,
                        contentColor = TableColors.feltDeep,
                    ),
                ) { Text("Deal 4") }
                if (snapshot.phase == GamePhase.CANCELLED) {
                    Text(snapshot.cancelReason ?: "Cancelled", color = TableColors.cream, fontSize = 12.sp)
                }
            }
            GamePhase.DOUBLING -> {
                if (isLocalTurn && snapshot.canDouble) {
                    Button(onClick = onDouble) { Text("Double") }
                    Button(onClick = onSkipDoubling) { Text("No double") }
                } else if (isLocalTurn && snapshot.canRedouble) {
                    Button(onClick = onRedouble) { Text("Redouble") }
                    Button(onClick = onSkipDoubling) { Text("Accept") }
                } else {
                    Text("Waiting on double…", color = TableColors.cream)
                }
            }
            GamePhase.FIRST_PHASE -> {
                if (snapshot.canExposeTrump && isLocalTurn) {
                    Button(
                        onClick = onToggleExpose,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (exposeMode) TableColors.gold else TableColors.felt,
                            contentColor = if (exposeMode) TableColors.feltDeep else TableColors.cream,
                        ),
                    ) { Text(if (exposeMode) "Exposing…" else "Call Trump") }
                }
                Text("Phase 1 · trick ${snapshot.tricksPlayed + 1}", color = TableColors.cream)
            }
            GamePhase.SECOND_PHASE -> {
                Text(
                    "Phase 2 · Trump ${snapshot.trumpSuit?.symbol} · trick ${snapshot.tricksPlayed + 1}",
                    color = TableColors.cream,
                )
            }
            GamePhase.ROUND_OVER, GamePhase.MATCH_OVER -> {
                snapshot.lastRound?.let { r ->
                    Text(
                        if (r.bidMade) "Made ${r.bid}" else "Down ${r.bid}",
                        color = TableColors.gold,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onNewRound) {
                    Text(if (snapshot.phase == GamePhase.MATCH_OVER) "New match" else "Next round")
                }
            }
            GamePhase.BIDDING -> Text("Auction…", color = TableColors.cream)
            GamePhase.CHOOSING_TRUMP -> Text("Choosing trump…", color = TableColors.cream)
            else -> Unit
        }
    }
}

private fun seatViews(snapshot: GameSnapshot): Map<String, Player> {
    if (snapshot.players.size < 4) return emptyMap()
    val local = snapshot.localSeat
    fun at(offset: Int) = snapshot.players[(local + offset) % 4]
    return mapOf("top" to at(2), "left" to at(1), "right" to at(3))
}
