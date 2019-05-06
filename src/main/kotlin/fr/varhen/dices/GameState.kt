package fr.varhen.dices

import fr.varhen.User
import io.javalin.websocket.WsSession

abstract class GameState(val diceGame: DiceGame, val user: User?) {
    abstract fun nextState(action: Action, session: WsSession): GameState
    fun name(): String? {
        return when(this) {
            is Starting -> "STARTING"
            is Ended -> "ENDED"
            is AwatingBuyDice -> "AWAITING_BUY_DICE"
            else -> "NONE"
        }
    }
}