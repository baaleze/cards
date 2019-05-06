package fr.varhen.dices

import fr.varhen.Error
import fr.varhen.User
import fr.varhen.sendError
import io.javalin.websocket.WsSession

class AwatingBuyDice(diceGame: DiceGame, user: User, var nbBuys: Int = 1) : GameState(diceGame, user) {

    override fun nextState(action: Action, session: WsSession): GameState {
        return when(action) {
            is Action.Invalid -> this // do nothing when action was invalid
            is Action.BuyDice -> {
                // check if move is valid
                val tile = diceGame.supply.find { it.id == action.diceId }
                if (tile == null || diceGame.gold[user]!! < tile.cost) {
                    // invalid
                    sendError("Illegal action $action", Error.ILLEGAL_ACTION, session)
                    this
                } else {
                    // valid! do it
                    diceGame.giveDice(user!!, action.diceId)
                    nbBuys--
                    if (nbBuys == 0) {
                        // next
                        nextPlayer(user)
                    } else {
                        // else player can still buy
                        this
                    }
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
        diceGame.turn++
        if (diceGame.turn > NB_TURNS * diceGame.players.size) {
            // END GAME!!
            return Ended(diceGame)
        }

        // Next Player new roll
        val nextIndex = (diceGame.players.indexOf(user) + 1) % diceGame.players.size
        val nextUser = diceGame.players[nextIndex]


        diceGame.newRoll(nextUser)
        return AwatingBuyDice(diceGame, nextUser) // no tokens whatever
    }


}
