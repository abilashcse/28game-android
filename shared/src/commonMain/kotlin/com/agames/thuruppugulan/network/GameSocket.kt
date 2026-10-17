package com.agames.thuruppugulan.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString

/**
 * Thin multiplatform WebSocket client for the achex-style hub used by the legacy app.
 * Prefer offline mode for local play; online is best-effort peer sync.
 */
class GameSocket(
    private val url: String = DEFAULT_WS_URL,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var session: WebSocketSession? = null
    private var listenJob: Job? = null

    private val _incoming = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val incoming: SharedFlow<String> = _incoming.asSharedFlow()

    private val client = HttpClient(platformHttpEngine()) {
        install(WebSockets)
        install(ContentNegotiation) {
            json(gameJson)
        }
    }

    suspend fun connect(username: String, tableId: String) {
        close()
        val ws = client.webSocketSession(url)
        session = ws
        ws.sendText(gameJson.encodeToString(AuthMessage(auth = "$username@$DEFAULT_WS_INSTANCE")))
        ws.sendText(gameJson.encodeToString(JoinHubMessage(hubName = tableId)))
        listenJob = scope.launch {
            while (isActive) {
                val frame = ws.incoming.receive() as? Frame.Text ?: continue
                _incoming.emit(frame.readText())
            }
        }
    }

    suspend fun broadcast(message: HubBroadcast) {
        session?.sendText(gameJson.encodeToString(message))
    }

    suspend fun close() {
        listenJob?.cancel()
        listenJob = null
        session?.close()
        session = null
    }

    private suspend fun WebSocketSession.sendText(text: String) {
        send(Frame.Text(text))
    }
}
