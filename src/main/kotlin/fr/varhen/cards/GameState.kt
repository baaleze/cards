package fr.varhen.cards

import fr.varhen.User
import io.javalin.websocket.WsSession

abstract class GameState(val cardGame: CardGame, val user: User?) {
    abstract fun nextState(action: Action, session: WsSession): GameState
}