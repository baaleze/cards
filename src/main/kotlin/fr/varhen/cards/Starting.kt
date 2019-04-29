package fr.varhen.cards

import fr.varhen.Error
import fr.varhen.sendError
import io.javalin.websocket.WsSession

class Starting(cardGame: CardGame) : GameState(cardGame, null) {

    override fun nextState(action: Action, session: WsSession): GameState {
        return when(action) {
            is Action.Invalid -> this // do nothing when action was invalid
            is Action.Start -> {
                // get random start user
                AwatingPlay(cardGame, cardGame.players.random())
            }
            else -> {
                sendError("Illegal action $action from $this", Error.ILLEGAL_ACTION, session)
                this
            }
        }
    }

}
