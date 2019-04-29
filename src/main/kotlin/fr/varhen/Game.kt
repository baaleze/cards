package fr.varhen

import io.javalin.websocket.WsSession
import org.json.JSONObject

abstract class Game(val name: String) {
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

    abstract fun handleMessage(
        message: JSONObject,
        session: WsSession,
        user: User
    )

    abstract fun generateInfo(): JSONObject
}