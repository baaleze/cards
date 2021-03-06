package fr.varhen

import fr.varhen.cards.CardGame
import fr.varhen.dices.DiceGame
import fr.varhen.immortal.ImmortalGame
import fr.varhen.oriflame.OriflameGame
import io.javalin.websocket.WsSession
import org.json.JSONArray
import org.json.JSONObject

abstract class Game(val name: String) {
    var started = false
    val playerLimit = 4
    val players = mutableListOf<User>()

    open fun join(player: User): Boolean {
        return if (players.size < playerLimit) {
            players.add(player)
            true
        } else {
            false
        }
    }

    abstract fun handleMessage(
        message: JSONObject,
        session: WsSession,
        user: User
    )

    abstract fun generateInfo(): JSONObject
    fun desc(): JSONObject {
        return JSONObject()
            .put("name", name)
            .put("players", JSONArray(players))
            .put("started", started)
            .put("type", when(this) {
                is CardGame -> "CARD"
                is DiceGame -> "DICE"
                is ImmortalGame -> "IMMORTAL"
                is OriflameGame -> "ORIFLAMME"
                else -> "OTHER"
            })
    }
}