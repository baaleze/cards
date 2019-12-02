package fr.varhen.oriflame

import fr.varhen.Error
import fr.varhen.User
import fr.varhen.sendError
import io.javalin.websocket.WsSession

class AwaitingPlay(game: OriflameGame, user: User) : GameState(game, user) {

    override fun nextState(action: Action, session: WsSession): GameState {
        return when(action) {
            is Action.Invalid -> this // do nothing when action was invalid
            is Action.PlayCard -> {
                val card = game.hands[user]!!.find { it.name == action.name }!!
                game.hands[user]!!.remove(card)
                // play card on this position
                when {
                    action.pos == -1 -> // FIRST
                        game.board.add(0, mutableListOf(card))
                    action.pos == game.board.count() -> // LAST
                        game.board.add(mutableListOf(card))
                    else -> game.board[action.pos].add(0, card)
                }
                nextPlayer()
            }
            else -> {
                sendError("Illegal action $action from $this", Error.ILLEGAL_ACTION, session)
                this
            }
        }
    }

    private fun nextPlayer(): GameState {
        game.remainingCardsToPlayThisRound.minus(1)
        val nextUser = game.players[(game.players.indexOf(user) + 1) % game.players.count()]
        return if (game.remainingCardsToPlayThisRound == 0) {
            game.currentPile = game.board.first()
            // in this case nextUser is the first player of this round
            AwaitingReveal(game, nextUser)
        } else {
            AwaitingPlay(game, nextUser)
        }
    }

}
