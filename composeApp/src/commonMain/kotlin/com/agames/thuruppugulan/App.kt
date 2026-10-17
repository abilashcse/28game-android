package com.agames.thuruppugulan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.agames.thuruppugulan.domain.GamePhase
import com.agames.thuruppugulan.session.GameController
import com.agames.thuruppugulan.ui.LobbyScreen
import com.agames.thuruppugulan.ui.TableScreen
import com.agames.thuruppugulan.ui.theme.ThuruppuTheme

private enum class AppScreen { Lobby, Table }

@Composable
fun App() {
    ThuruppuTheme {
        val controller = remember { GameController() }
        var screen by remember { mutableStateOf(AppScreen.Lobby) }
        val snapshot by controller.state.collectAsState()

        DisposableEffect(Unit) {
            onDispose { controller.dispose() }
        }

        when (screen) {
            AppScreen.Lobby -> LobbyScreen(
                onPlayOffline = {
                    controller.startOffline(it)
                    screen = AppScreen.Table
                },
                onHostOnline = {
                    controller.startOnlineHost(it)
                    screen = AppScreen.Table
                },
                onJoinOnline = { name, tableId ->
                    controller.joinOnline(name, tableId)
                    screen = AppScreen.Table
                },
            )

            AppScreen.Table -> TableScreen(
                snapshot = snapshot,
                legalPlays = controller.legalPlays(),
                bidOptions = controller.bidOptions(),
                isLocalTurn = controller.isLocalTurn(),
                onShuffle = controller::shuffle,
                onDeal = controller::deal,
                onBid = controller::bid,
                onPass = controller::pass,
                onChooseTrump = controller::chooseTrump,
                onPlay = controller::play,
                onNewRound = controller::newRound,
                onLeave = {
                    controller.dispose()
                    screen = AppScreen.Lobby
                },
            )
        }

        // Keep bid sheet in sync when bots advance auction to local seat
        if (screen == AppScreen.Table &&
            snapshot.phase == GamePhase.WAITING_FOR_PLAYERS
        ) {
            // no-op placeholder for future online seat UI
        }
    }
}
