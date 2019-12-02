package fr.varhen.oriflame

import fr.varhen.Error
import fr.varhen.User
import fr.varhen.sendError
import io.javalin.websocket.WsSession

class Starting(game: OriflameGame) : GameState(game, null) {

    override fun nextState(action: Action, session: WsSession): GameState {
        return when(action) {
            is Action.Invalid -> this // do nothing when action was invalid
            is Action.Start -> {
                game.players.forEach { buildHand(it) }
                AwaitingPlay(game, game.players.random())
            }
            else -> {
                sendError("Illegal action $action from $this", Error.ILLEGAL_ACTION, session)
                this
            }
        }
    }

    private fun buildHand(user: User) {
        // create all cards
        game.hands[user] = mutableListOf(
            Card("archer", ARCHER, 0, false, user),
            Card("soldat", SOLDAT, 0, false, user),
            Card("héritier", HERITIER, 0, false, user),
            Card("espion", ESPION, 0, false, user),
            Card("assassin", ASSASSIN, 0, false, user),
            Card("décret royal", DECRET_ROYAL, 0, false, user),
            Card("seigneur", SEIGNEUR, 0, false, user),
            Card("changeforme", CHANGEFORME, 0, false, user),
            Card("ambuscade", AMBUSCADE, 0, false, user),
            Card("complot", COMPLOT, 0, false, user)
        ).also {
            hand -> repeat(3) { hand.remove(hand.random()) }
        }
    }

}
