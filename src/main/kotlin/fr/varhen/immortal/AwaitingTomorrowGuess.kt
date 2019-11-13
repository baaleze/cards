package fr.varhen.immortal

import fr.varhen.Error
import fr.varhen.User
import fr.varhen.sendError
import io.javalin.websocket.WsSession

class AwaitingTomorrowGuess(game: ImmortalGame, user: User): GameState(game, user) {
    override fun nextState(action: Action, session: WsSession): GameState {
        return when(action) {
            is Action.Invalid -> this // do nothing when action was invalid
            is Action.Guess -> {
                return if ((action.guesses.count() == game.players.count() - 1 && !action.usedJoker)||
                    (action.guesses.count() == game.players.count() - 2 && action.usedJoker)) {
                    // ok check
                    for((userId, name) in action.guesses) {
                        var u = game.players.find { it.id == userId }!!
                        if (game.immortals[u]!![0].name != name) {
                            // wrong!!
                            return game.reveal(game)
                        }
                    }
                    // correct
                    game.tomorrowGuessPoints = if (action.usedJoker) 10 else 15
                    return game.reveal(game)
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