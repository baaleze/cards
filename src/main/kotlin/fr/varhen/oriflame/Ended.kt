package fr.varhen.oriflame

import fr.varhen.Error
import fr.varhen.sendError
import io.javalin.websocket.WsSession

class Ended(game: OriflameGame): GameState(game, null) {
    override fun nextState(action: Action, session: WsSession): GameState {
        sendError("Game is finished", Error.ILLEGAL_ACTION, session)
        return this
    }

}
