package fr.varhen.cards

import fr.varhen.User
import io.javalin.websocket.WsSession

class AwaitingUseTokens(cardGame: CardGame, user: User) : GameState(cardGame, user) {
    override fun nextState(action: Action, session: WsSession): GameState {
        TODO("not implemented")
    }

}
