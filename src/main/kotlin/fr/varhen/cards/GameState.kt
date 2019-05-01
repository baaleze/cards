package fr.varhen.cards

import fr.varhen.User
import io.javalin.websocket.WsSession

abstract class GameState(val cardGame: CardGame, val user: User?) {
    abstract fun nextState(action: Action, session: WsSession): GameState
    fun name(): String? {
        return when(this) {
            is Starting -> "STARTING"
            is AwatingPlay -> "AWAITING_PLAY"
            is AwaitingUseTokens -> "AWAITING_USE_TOKENS"
            is Ended -> "ENDED"
            else -> "NONE"
        }
    }
}