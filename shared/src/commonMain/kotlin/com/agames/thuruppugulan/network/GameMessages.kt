package com.agames.thuruppugulan.network

import com.agames.thuruppugulan.domain.Card
import com.agames.thuruppugulan.domain.Player
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

val gameJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    isLenient = true
}

@Serializable
data class AuthMessage(
    val auth: String,
    val passwd: String = DEFAULT_HUB_PASSWORD,
)

@Serializable
data class JoinHubMessage(
    @SerialName("joinHub") val hubName: String,
    val passwd: String = DEFAULT_HUB_PASSWORD,
)

@Serializable
data class HubBroadcast(
    @SerialName("toH") val tableId: String,
    val msg: String,
    val players: List<Player>? = null,
    val player: Player? = null,
    val canPass: Boolean = false,
    val card: Card? = null,
    val bid: Int? = null,
    val seat: Int? = null,
)

const val DEFAULT_HUB_PASSWORD = "thuruppu-kmm"
const val DEFAULT_WS_INSTANCE = "00001023"
const val DEFAULT_WS_URL = "wss://cloud.achex.ca/$DEFAULT_WS_INSTANCE"

object MessageType {
    const val JOINED = "joined"
    const val PLAYER_DETAILS = "player_details"
    const val ALL_PLAYERS = "all_players_connected"
    const val SHUFFLING = "send_shuffling_event"
    const val FIRST_SET = "send_first_set_cards"
    const val SECOND_SET = "send_second_set_cards"
    const val SELECT_BID = "send_select_bid"
    const val PLACE_BID = "place_bid"
    const val PASS_BID = "pass_bid"
    const val CHOOSE_TRUMP = "choose_trump"
    const val PLAY_CARD = "play_card"
}
