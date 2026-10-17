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
import androidx.compose.foundation.layout.width // used in MATCH_OVER action row
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
    onPass: () -> Unit,
    onChooseTrump: (Card) -> Unit,
    onPlay: (Card) -> Unit,
    onNewRound: () -> Unit,
    onLeave: () -> Unit,
) {
    var showBidSheet by remember(snapshot.phase, snapshot.bidToActSeat) {
        mutableStateOf(snapshot.phase == GamePhase.BIDDING && isLocalTurn)
    }
    var selectingTrump by remember(snapshot.phase) {
        mutableStateOf(snapshot.phase == GamePhase.CHOOSING_TRUMP && isLocalTurn)
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
            seats["top"]?.let { SeatBadge(it, Modifier.align(Alignment.TopCenter)) }
            seats["left"]?.let { SeatBadge(it, Modifier.align(Alignment.CenterStart)) }
            seats["right"]?.let { SeatBadge(it, Modifier.align(Alignment.CenterEnd)) }

            TrickArea(
                snapshot = snapshot,
                modifier = Modifier.align(Alignment.Center),
            )

            Text(
                text = snapshot.statusMessage,
                color = TableColors.cream.copy(alpha = 0.9f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp),
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        ActionRow(snapshot, onShuffle, onDeal, onNewRound)

        val local = snapshot.players.getOrNull(snapshot.localSeat)
        if (local != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (selectingTrump) "Tap a card to set concealed trump" else local.displayName,
                color = TableColors.gold,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(local.hand, key = { it.details }) { card ->
                    val playable = card in legalPlays && isLocalTurn &&
                        snapshot.phase in listOf(GamePhase.FIRST_PHASE, GamePhase.SECOND_PHASE)
                    PlayingCard(
                        card = card,
                        selected = selectingTrump,
                        enabled = selectingTrump || playable,
                        onClick = {
                            when {
                                selectingTrump -> {
                                    onChooseTrump(card)
                                    selectingTrump = false
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
                text = "Table ${snapshot.tableId ?: "—"} · ${snapshot.phase.name}",
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
        Text("Us (0/2): ${snapshot.teamScores[0] ?: 0}", color = TableColors.cream)
        Text("Them (1/3): ${snapshot.teamScores[1] ?: 0}", color = TableColors.cream)
        snapshot.trumpSuit?.let {
            Text(
                text = if (snapshot.trumpExposed) "Trump ${it.symbol}" else "Trump ?",
                color = TableColors.gold,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun SeatBadge(player: Player, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(TableColors.gold.copy(alpha = 0.25f))
                .padding(10.dp),
        ) {
            Text(player.displayName.take(1), color = TableColors.gold, fontWeight = FontWeight.Bold)
        }
        Text(player.displayName, color = TableColors.cream, fontSize = 12.sp)
        Text("${player.hand.size} cards", color = TableColors.cream.copy(alpha = 0.6f), fontSize = 11.sp)
    }
}

@Composable
private fun TrickArea(snapshot: GameSnapshot, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (snapshot.currentTrick.isEmpty()) {
            Text("Trick pile", color = TableColors.cream.copy(alpha = 0.35f))
        } else {
            snapshot.currentTrick.forEach { play ->
                PlayingCard(card = play.card, width = 48.dp, height = 72.dp)
            }
        }
    }
}

@Composable
private fun ActionRow(
    snapshot: GameSnapshot,
    onShuffle: () -> Unit,
    onDeal: () -> Unit,
    onNewRound: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        when (snapshot.phase) {
            GamePhase.READY_TO_DEAL, GamePhase.ROUND_OVER -> {
                if (snapshot.players.getOrNull(snapshot.localSeat)?.isDealer == true || snapshot.offlineMode) {
                    Button(
                        onClick = onShuffle,
                        colors = ButtonDefaults.buttonColors(containerColor = TableColors.felt, contentColor = TableColors.cream),
                    ) { Text("Shuffle") }
                    Button(
                        onClick = onDeal,
                        colors = ButtonDefaults.buttonColors(containerColor = TableColors.gold, contentColor = TableColors.feltDeep),
                    ) { Text("Deal") }
                }
                if (snapshot.phase == GamePhase.ROUND_OVER) {
                    Button(onClick = onNewRound) { Text("Next round") }
                }
            }
            GamePhase.MATCH_OVER -> {
                Text("Match over", color = TableColors.gold, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onNewRound) { Text("Play again") }
            }
            GamePhase.BIDDING -> {
                Text("Bidding…", color = TableColors.cream)
            }
            else -> Unit
        }
    }
}

private fun seatViews(snapshot: GameSnapshot): Map<String, Player> {
    if (snapshot.players.size < 4) return emptyMap()
    val local = snapshot.localSeat
    fun at(offset: Int) = snapshot.players[(local + offset) % 4]
    // Offline seats: 0 local bottom, 1 left, 2 top partner, 3 right — but deal order is CCW
    return mapOf(
        "top" to at(2),
        "left" to at(1),
        "right" to at(3),
    )
}
