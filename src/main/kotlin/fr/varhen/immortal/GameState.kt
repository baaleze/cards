package fr.varhen.immortal

import fr.varhen.User
import io.javalin.websocket.WsSession

abstract class GameState(val game: ImmortalGame, val user: User?) {
    abstract fun nextState(action: Action, session: WsSession): GameState
    fun name(): String? {
        return when(this) {
            is Starting -> "STARTING"
            is AwaitingPlay -> "AWAITING_PLAY"
            is AwaitingDraft -> "AWAITING_DRAFT"
            is AwaitingCommerceChoice -> "AWAITING_COMMERCE_CHOICE"
            is Ended -> "ENDED"
            else -> "NONE"
        }
    }
}