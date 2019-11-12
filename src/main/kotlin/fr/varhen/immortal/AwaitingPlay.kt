package fr.varhen.immortal

import fr.varhen.Error
import fr.varhen.User
import fr.varhen.sendError
import io.javalin.websocket.WsSession

class AwaitingPlay(game: ImmortalGame, user: User) : GameState(game, user) {

    override fun nextState(action: Action, session: WsSession): GameState {
        return when(action) {
            is Action.Invalid -> this // do nothing when action was invalid
            is Action.UseAction -> {
                // check if move is valid
                this
            }
            is Action.Pass -> {
                nextPlayer(user!!)
            }
            else -> {
                sendError("Illegal action $action from $this", Error.ILLEGAL_ACTION, session)
                this
            }
        }
    }

    fun nextPlayer(user: User): GameState {
        // TODO
        return this
    }

}
