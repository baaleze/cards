package fr.varhen.oriflame

import fr.varhen.Error
import fr.varhen.User
import fr.varhen.oriflame.GameState
import fr.varhen.sendError
import io.javalin.websocket.WsSession

fun nextCard(game: OriflameGame, user: User): GameState {
    val i = game.board.indexOf(game.currentPile) + 1
    return if (i == game.board.count()) {
        // arrived at the end of the board NEXT ROUND
        val nextUser = game.players[(game.players.indexOf(user) + 1) % game.players.count()]
        game.remainingCardsToPlayThisRound = game.players.count()
        AwaitingPlay(game, nextUser)
    } else {
        game.currentPile = game.board[i]
        AwaitingReveal(game, user)
    }
}

class AwaitingReveal(game: OriflameGame, user: User) : GameState(game, user) {

    override fun nextState(action: Action, session: WsSession): GameState {
        return when(action) {
            is Action.Invalid -> this // do nothing when action was invalid
            is Action.ChooseReveal -> {
                val card = game.getCurrentCard()
                if (action.reveal) {
                    game.points[user]!!.plus(card.points) // gain points stored on it
                    card.caradMechanic.onPlay(card.owner, game) ?: nextCard(game, user!!)
                } else {
                    // put point on it
                    card.points.plus(1)
                    nextCard(game, user!!)
                }
            }
            else -> {
                sendError("Illegal action $action from $this", Error.ILLEGAL_ACTION, session)
                this
            }
        }
    }



}
