package fr.varhen.cards

import fr.varhen.Error
import fr.varhen.sendError
import io.javalin.websocket.WsSession

class Starting(cardGame: CardGame) : GameState(cardGame, null) {

    override fun nextState(action: Action, session: WsSession): GameState {
        return when(action) {
            is Action.Invalid -> this // do nothing when action was invalid
            is Action.Start -> {
                // build the boards
                cardGame.players.forEach {
                    val b = Array(6) {
                        arrayOfNulls<Tile>(6)
                    }
                    // start tile
                    b[2][2] = Tile(0, arrayOf(0,1,2,3,4,5), 1, 0,0,0)
                    cardGame.boards[it] = b
                    cardGame.gold[it] = 5 // starting gold
                    cardGame.points[it] = 0
                    cardGame.minusTokens[it] = 0
                    cardGame.plusTokens[it] = 0
                }

                // get random start user
                AwatingPlay(cardGame, cardGame.players.random())
            }
            else -> {
                sendError("Illegal action $action from $this", Error.ILLEGAL_ACTION, session)
                this
            }
        }
    }

}
