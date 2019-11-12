package fr.varhen.immortal

import fr.varhen.Error
import fr.varhen.sendError
import io.javalin.websocket.WsSession

class AwaitingDraft(game: ImmortalGame) : GameState(game, null) {

    override fun nextState(action: Action, session: WsSession): GameState {
        return when(action) {
            is Action.Invalid -> this // do nothing when action was invalid
            is Action.PlayCard -> {
                // check if player has card and can play it

                // Play for real

                // Gain coin instead

                // check if every one has played
                return when {
                    // start the first round
                    game.hasDrafted.all { it.value } -> AwaitingPlay(game, game.orderPlayers()) // todo find user
                    else -> this
                }
            }
            else -> {
                sendError("Illegal action $action from $this", Error.ILLEGAL_ACTION, session)
                this
            }
        }
    }

}
