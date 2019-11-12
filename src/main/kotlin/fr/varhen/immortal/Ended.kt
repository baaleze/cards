package fr.varhen.immortal

import fr.varhen.Error
import fr.varhen.sendError
import io.javalin.websocket.WsSession

class Ended(game: ImmortalGame): GameState(game, null) {
    override fun nextState(action: Action, session: WsSession): GameState {
        sendError("Game is finished", Error.ILLEGAL_ACTION, session)
        return this
    }

}
