package fr.varhen.immortal

import fr.varhen.Error
import fr.varhen.User
import fr.varhen.oriflame.GameState
import fr.varhen.sendError
import io.javalin.websocket.WsSession

class AwaitingNarashimaDestroy(game: ImmortalGame, user: User): GameState(game, user) {
    override fun nextState(action: Action, session: WsSession): GameState {
        return when(action) {
            is Action.Invalid -> this // do nothing when action was invalid
            is Action.ChooseWhatToDestroy -> {
                return if (action.cards.all { game.hasCard(it as Int, user!!) }) {
                    for (cardId in action.cards) {
                        val card = game.findCard(cardId as Int)!!
                        game.narashimaCulture += card.culture // get culture
                        game.destroy(card, user!!)
                    }
                    return game.reveal()
                } else {
                    this // invalid
                }
            }
            else -> {
                sendError("Illegal action $action from $this", Error.ILLEGAL_ACTION, session)
                this
            }
        }
    }
}