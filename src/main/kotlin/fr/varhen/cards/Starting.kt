package fr.varhen.cards

import fr.varhen.Error
import fr.varhen.User
import fr.varhen.sendError
import io.javalin.websocket.WsSession

class Starting(cardGame: CardGame) : GameState(cardGame, null) {

    override fun nextState(action: Action, session: WsSession): GameState {
        return when(action) {
            is Action.Invalid -> this // do nothing when action was invalid
            is Action.Start -> {
                // build the boards
                cardGame.players.forEach {
                    val b = Array(boardSize) {
                        arrayOfNulls<Tile>(boardSize)
                    }
                    // start tile
                    b[boardSize / 2][boardSize / 2] = Tile(0, arrayOf(0,1,2,3,4,5), 1, 0,0,0)
                    cardGame.boards[it] = b
                    cardGame.gold[it] = 5 // starting gold
                    cardGame.points[it] = 0
                    cardGame.minusTokens[it] = 0
                    cardGame.plusTokens[it] = 0
                }

                // fill supply
                repeat(10) {
                    cardGame.allTiles.random().apply {
                        cardGame.allTiles.remove(this)
                        cardGame.supply.add(this)
                    }
                }

                // start turns
                cardGame.turn++

                cardGame.started = true


                // get random start user
                cardGame.roll()
                val random = cardGame.players.random()
                cardGame.applyRoll(random)
                AwatingPlay(cardGame, random)
            }
            else -> {
                sendError("Illegal action $action from $this", Error.ILLEGAL_ACTION, session)
                this
            }
        }
    }

}
