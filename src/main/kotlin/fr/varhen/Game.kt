package fr.varhen

import io.javalin.websocket.WsSession
import org.json.JSONObject

class Game(val name: String) {
    val playerLimit = 4
    val players = mutableListOf<User>()

    fun join(player: User): Boolean {
        return if (players.size < playerLimit) {
            players.add(player)
            true
        } else {
            false
        }
    }

    fun handleMessage(message: JSONObject, session: WsSession) {
        TODO("not implemented")
    }
}