package fr.varhen.immortal

import fr.varhen.Error
import fr.varhen.oriflame.GameState
import fr.varhen.sendError
import io.javalin.websocket.WsSession

class AwaitingDraft(game: ImmortalGame) : GameState(game, null) {

    override fun nextState(action: Action, session: WsSession): GameState {
        return when(action) {
            is Action.Invalid -> this // do nothing when action was invalid
            is Action.PlayCard -> {
                // check if player has card and can play it
                if (game.hands[user]?.any {it.id == action.cardId} == true) {
                    // Play for real
                    game.play(action.cardId, user!!, action.useForGold, action.additionalArgs)

                    // check if every one has played
                    return if (game.hasDrafted.all { it.value }) {
                        // next draft turn
                        if (game.round == 0 && game.draftTurn == 4) {
                            // END DRAFT
                            game.draftTurn = 0
                            // sort players
                            game.orderPlayers()
                            AwaitingPlay(game, game.players[0])
                        } else if (game.round == 1 && game.draftTurn == 3) {
                            // END DRAFT 2
                            game.draftTurn = 0
                            // next player is LAST one
                            AwaitingPlay(game, game.players[game.players.count() - 1])
                        } else {
                            game.draftTurn++
                            // swap hands
                            game.swapHands()
                            // draft again!
                            AwaitingDraft(game)
                        }

                    } else {
                        this
                    }
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
