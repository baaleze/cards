package fr.varhen.oriflame

import fr.varhen.Error
import fr.varhen.User
import fr.varhen.sendError
import io.javalin.websocket.WsSession

class AwaitingTarget(game: OriflameGame, user: User, val from: String) : GameState(game, user) {

    override fun nextState(action: Action, session: WsSession): GameState {
        return when(action) {
            is Action.Invalid -> this // do nothing when action was invalid
            is Action.ChooseTarget -> {
                when(from) {
                    "ARCHER", "SOLDAT" -> {
                        game.destroy(action.pos, user!!)
                        nextCard(game, user)
                    }
                    "ASSASSIN" -> {
                        game.destroy(action.pos, user!!)
                        game.destroyCurrentCard()
                        nextCard(game, user)
                    }
                    "ESPION" -> {
                        val cardCopied = game.board[action.pos].first()
                        game.stealFrom(user, cardCopied.owner)
                        nextCard(game, user!!)
                    }
                    "CHANGEFORME" -> {
                        val cardCopied = game.board[action.pos].first()
                        cardCopied.caradMechanic.onPlay(user!!, game) ?: nextCard(game, user)
                    }
                    "DECRET_ROYAL" -> {
                        game.moveCard(action.pos, action.destPos)
                        game.destroyCurrentCard()
                        nextCard(game, user!!)
                    }
                    else -> nextCard(game, user!!)
                }
            }
            else -> {
                sendError("Illegal action $action from $this", Error.ILLEGAL_ACTION, session)
                this
            }
        }
    }

}
