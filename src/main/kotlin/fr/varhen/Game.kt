package fr.varhen

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
        return JSONObject().put("name", name).put("players", JSONArray(players)).put("started", started)
    }
}