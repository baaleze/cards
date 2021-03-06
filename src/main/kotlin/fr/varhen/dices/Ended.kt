package fr.varhen.dices

import fr.varhen.Error
import fr.varhen.sendError
import io.javalin.websocket.WsSession

class Ended(diceGame: DiceGame): GameState(diceGame, null) {
    override fun nextState(action: Action, session: WsSession): GameState {
        sendError("Game is finished", Error.ILLEGAL_ACTION, session)
        return this
    }

}
