package fr.varhen.immortal

import fr.varhen.Error
import fr.varhen.User
import fr.varhen.oriflame.GameState
import fr.varhen.sendError
import io.javalin.websocket.WsSession

class AwaitingCommerceChoice(game: ImmortalGame, user: User): GameState(game, user) {

    override fun nextState(action: Action, session: WsSession): GameState {
        return when(action) {
            is Action.Invalid -> this // do nothing when action was invalid
            is Action.ChooseCommerce -> {
                // valid choice ?
                return if (action.commerce != null && game.commerceChoice.contains(action.commerce)) {
                    game.addToken(user!!, action.commerce, if (action.commerce == Commerce.COIN) 2 else 1)
                    AwaitingPlay(game, user)
                } else if (action.commerce == null){
                    AwaitingPlay(game, user!!) // the choice is take nothing
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
