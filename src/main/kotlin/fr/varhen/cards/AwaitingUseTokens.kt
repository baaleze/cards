package fr.varhen.cards

import fr.varhen.Error
import fr.varhen.User
import fr.varhen.sendError
import io.javalin.websocket.WsSession

class AwaitingUseTokens(cardGame: CardGame, user: User) : GameState(cardGame, user) {
    override fun nextState(action: Action, session: WsSession): GameState {
        return when(action) {
            is Action.Invalid -> this // do nothing when action was invalid
            is Action.UseTokens -> {
                // check if move is valid
                if (cardGame.plusTokens[user]!! < action.nbPlusTokens || cardGame.minusTokens[user]!! < action.nbMinusTokens) {
                    // invalid
                    sendError("Illegal action $action", Error.ILLEGAL_ACTION, session)
                    this
                } else {
                    // valid! consume tokens and modify diceroll (in allowed range)
                    cardGame.plusTokens[user!!] = cardGame.plusTokens[user]!! - action.nbPlusTokens
                    cardGame.minusTokens[user] = cardGame.minusTokens[user]!! - action.nbMinusTokens
                    cardGame.diceRoll = Math.max(Math.min(cardGame.diceRoll + action.nbPlusTokens - action.nbMinusTokens, 6), 1)
                    cardGame.applyRoll(user)
                    AwatingPlay(cardGame, user)
                }
            }
            else -> {
                sendError("Illegal action $action from $this", Error.ILLEGAL_ACTION, session)
                this
            }
        }
    }

}
