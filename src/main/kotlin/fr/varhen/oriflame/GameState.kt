package fr.varhen.oriflame

import fr.varhen.User
import io.javalin.websocket.WsSession

abstract class GameState(val game: OriflameGame, val user: User?) {
    abstract fun nextState(action: Action, session: WsSession): GameState
    fun name(): String? {
        return when(this) {
            is Starting -> "STARTING"
            is AwaitingPlay -> "AWAITING_PLAY"
            is AwaitingReveal -> "AWAITING_REVEAL"
            is AwaitingTarget -> "AWAITING_TARGET"
            is Ended -> "ENDED"
            else -> "NONE"
        }
    }
}