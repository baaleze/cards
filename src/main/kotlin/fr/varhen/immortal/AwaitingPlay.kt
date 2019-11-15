package fr.varhen.immortal

import fr.varhen.Error
import fr.varhen.User
import fr.varhen.sendError
import io.javalin.websocket.WsSession

class AwaitingPlay(game: ImmortalGame, user: User) : GameState(game, user) {

    override fun nextState(action: Action, session: WsSession): GameState {
        return when(action) {
            is Action.Invalid -> this // do nothing when action was invalid
            is Action.UseAction -> {
                val card = game.findCard(action.cardId)!!
                // check if move is valid
                if (card.canDoAction(card, game, user!!, action.additionalArgs)) {
                    // ok do it
                    card.action(card, game, user, action.additionalArgs)
                    // the card is used!
                    card.tapped = true
                }
                this // stay in play card state
            }
            is Action.Pass -> {
                // end turn need check for Equilibrium bonus
                if (game.wonders.any { it.name == "Equilibrium" } && game.getNumberOfTokenTriplets(user!!) > 0) {
                    game.addPoints(user, 3)
                }
                nextPlayer(game, user!!)
            }
            else -> {
                sendError("Illegal action $action from $this", Error.ILLEGAL_ACTION, session)
                this
            }
        }
    }

    fun nextPlayer(game: ImmortalGame, user: User): GameState {
        val index = game.players.indexOf(user)
        return if (game.round == 0) {
            // 1st round going up list
            if (index == game.players.count()+1) {
                // end round
                endRound(game)
            } else {
                AwaitingPlay(game, game.players[index+1])
            }
        } else {
            // 2nd round going down the list
            if (index == 0) {
                // end round
                endRound(game)
            } else {
                AwaitingPlay(game, game.players[index-1])
            }
        }
    }

    private fun endRound(game: ImmortalGame): GameState {
        // handle supremacies
        for (p in game.players) {
            if (game.hasSupremacy(p, Commerce.WAR)) {
                game.addSupremacy(p)
            }
            if (game.hasSupremacy(p, Commerce.SCIENCE)) {
                game.addSupremacy(p)
            }
            if (game.wonders.any { it.name == "Rituel du Chaos" } && game.hasSupremacy(p, Commerce.CHAOS)) {
                game.addSupremacy(p)
            }
        }
        return if (game.round == 0) {
            // next draft!
            game.round++
            game.dealHands()
            AwaitingDraft(game)
        } else {
            // end the game!
            if (game.players.count() == 3) {
                AwaitingImmortalChoice(game)
            } else {
                game.reveal()
            }
        }
    }

}
