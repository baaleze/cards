package fr.varhen.immortal

import fr.varhen.Error
import fr.varhen.User
import fr.varhen.oriflame.GameState
import fr.varhen.sendError
import io.javalin.websocket.WsSession

class AwatingChaosPortalPlay(g: ImmortalGame, u: User) : GameState(g,u) {

    override fun nextState(action: Action, session: WsSession): GameState {
        return when(action) {
            is Action.Invalid -> this // do nothing when action was invalid
            is Action.ChooseChaosPortal -> {
                // valid choice ?
                return if (game.chaosPortalCards.any { it.id == action.cardId }) {
                    // play it on board
                    val card = game.chaosPortalCards.find { it.id == action.cardId }!!
                    game.allCards.remove(card)
                    when {
                        card.type == CardType.HERO -> game.heroes[user]!!.add(card)
                        card.type == CardType.BUILDING -> game.buildings[user]!!.add(card)
                        else -> game.wonders.add(card)
                    }
                    // bonus
                    card.bonus(card, game, user!!, action.additionalArgs)
                    // clear choices
                    game.chaosPortalCards.clear()
                    AwaitingPlay(game, user)
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
