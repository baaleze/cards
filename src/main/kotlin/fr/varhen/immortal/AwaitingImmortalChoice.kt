package fr.varhen.immortal

import fr.varhen.Error
import fr.varhen.sendError
import io.javalin.websocket.WsSession


class AwaitingImmortalChoice(game: ImmortalGame) : GameState(game,  null) {
    override fun nextState(action: Action, session: WsSession): GameState {
        return when(action) {
            is Action.Invalid -> this // do nothing when action was invalid
            is Action.ChooseImmortal -> {
                if (game.immortals[user]?.any { it.name == action.name } == true) {
                    game.immortals[user]?.removeIf { it.name != action.name } // remove other choices
                } else {
                    return this // invalid!
                }

                // check if every one has done the choice
                return if (game.immortals.all { it.value.count() == 1 }) {
                    game.reveal()
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
