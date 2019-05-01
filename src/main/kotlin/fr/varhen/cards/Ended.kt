package fr.varhen.cards

import fr.varhen.Error
import fr.varhen.User
import fr.varhen.sendError
import io.javalin.websocket.WsSession

class Ended(cardGame: CardGame,user: User): GameState(cardGame, user) {
    override fun nextState(action: Action, session: WsSession): GameState {
        sendError("Game is finished", Error.ILLEGAL_ACTION, session)
        return this
    }

}
