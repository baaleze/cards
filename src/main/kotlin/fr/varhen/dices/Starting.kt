package fr.varhen.dices

import fr.varhen.Error
import fr.varhen.User
import fr.varhen.sendError
import io.javalin.websocket.WsSession

class Starting(diceGame: DiceGame) : GameState(diceGame, null) {

    override fun nextState(action: Action, session: WsSession): GameState {
        return when(action) {
            is Action.Invalid -> this // do nothing when action was invalid
            is Action.Start -> {
                // build the base dices
                diceGame.players.forEach {
                    diceGame.playerDices[it] = mutableListOf(
                        Dice(arrayOf( // TODO base dice
                            Face.ResourceFace(1,0),
                            Face.ResourceFace(1,0),
                            Face.ResourceFace(1,0),
                            Face.ResourceFace(1,0)),
                            Group.GREEN, 0))
                }

                // random first player
                AwatingBuyDice(diceGame, diceGame.players.random())
            }
            else -> {
                sendError("Illegal action $action from $this", Error.ILLEGAL_ACTION, session)
                this
            }
        }
    }

}
