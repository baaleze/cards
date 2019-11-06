package fr.varhen.immortal

import fr.varhen.Error
import fr.varhen.User
import fr.varhen.sendError
import io.javalin.websocket.WsSession

class AwatingPlay(game: ImmortalGame, user: User) : GameState(game, user) {

    override fun nextState(action: Action, session: WsSession): GameState {
        return when(action) {
            is Action.Invalid -> this // do nothing when action was invalid
            is Action.PlayTile -> {
                // check if move is valid
                val tile = cardGame.supply.find { it.id == action.tileId }
                if (tile == null || cardGame.gold[user]!! < tile.cost) {
                    // invalid
                    sendError("Illegal action $action", Error.ILLEGAL_ACTION, session)
                    this
                } else {
                    // valid! do it
                    cardGame.putTileOnPlayerBoard(user!!, action.tileId, action.x, action.y, action.direction)
                    // next
                    nextPlayer(user)
                }
            }
            is Action.Pass -> {
                nextPlayer(user!!)
            }
            else -> {
                sendError("Illegal action $action from $this", Error.ILLEGAL_ACTION, session)
                this
            }
        }
    }

    fun nextPlayer(user: User): GameState {
        cardGame.turn++
        if (cardGame.turn > NB_TURNS * cardGame.players.size) {
            // END GAME!!
            return Ended(cardGame)
        }

        // Next Player new roll
        cardGame.roll()
        val nextIndex = (cardGame.players.indexOf(user) + 1) % cardGame.players.size
        val nextUser = cardGame.players[nextIndex]

        return if (cardGame.minusTokens[nextUser]!! > 0 || cardGame.plusTokens[nextUser]!! > 0) {
            AwaitingUseTokens(cardGame, nextUser) // he can choose to use tokens
        } else {
            cardGame.applyRoll(nextUser)
            AwatingPlay(cardGame, nextUser) // no tokens whatever
        }
    }


}
