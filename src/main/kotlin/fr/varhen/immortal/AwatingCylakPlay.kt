package fr.varhen.immortal

import fr.varhen.Error
import fr.varhen.User
import fr.varhen.sendError
import io.javalin.websocket.WsSession

class AwatingCylakPlay(g: ImmortalGame, u: User) : GameState(g,u) {

    override fun nextState(action: Action, session: WsSession): GameState {
        return when(action) {
            is Action.Invalid -> this // do nothing when action was invalid
            is Action.PlayOrNot -> {
                if (action.play && game.cylakTopCard.canDoAction(game.cylakTopCard, game, user!!, action.additionalArgs)) {
                    game.cylakTopCard.action(game.cylakTopCard, game, user, action.additionalArgs)
                    AwaitingPlay(game, user)
                } else {
                    this
                }
            }
            else -> {
                sendError("Illegal action $action from $this", Error.ILLEGAL_ACTION, session)
                this
            }
        }
    }

}
