package fr.varhen.immortal

import fr.varhen.Error
import fr.varhen.oriflame.GameState
import fr.varhen.sendError
import io.javalin.websocket.WsSession

class Starting(game: ImmortalGame) : GameState(game, null) {

    override fun nextState(action: Action, session: WsSession): GameState {
        return when(action) {
            is Action.Invalid -> this // do nothing when action was invalid
            is Action.Start -> {
                // build the hands

                AwaitingDraft(game)
            }
            else -> {
                sendError("Illegal action $action from $this", Error.ILLEGAL_ACTION, session)
                this
            }
        }
    }

}
