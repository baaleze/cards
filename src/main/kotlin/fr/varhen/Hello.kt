package fr.varhen

import io.javalin.Javalin
import io.javalin.websocket.WsSession
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

val users = mutableListOf<User>()
val loggedSession = ConcurrentHashMap<WsSession, User>()
val games = mutableListOf<Game>()

fun main() {
    Javalin.create().apply {
        enableStaticFiles("/ngcards")
        ws("/ws") { ws ->
            ws.onConnect { session ->
                println("${session.host()} joined.")
            }
            ws.onClose { session, status, message ->
                println("${session.host()} left.")
                logout(session)
            }
            ws.onMessage { session, message ->
                // handle message
                handleMessage(JSONObject(message), session)
            }
        }
    }.start(7070)
}

fun handleMessage(message: JSONObject, session: WsSession) {
    when(message.getString("type") ?: "NULL") {
        "NULL" -> sendError("Missing type in message! $message", session) // missing type!
        "LOGIN" -> login(message.getString("name"), session)
        else -> {
            // check if logged in for all other actions
            if (!loggedSession.contains(session)) {
                sendError("Can't do that, the session did not login", session)
                return
            } else {
                val user = loggedSession[session]!!
                when (message.getString("type")) {
                    "JOIN" -> join(message.getString("name"), session, user)
                    "CHAT" -> chat(message.getString("message"), session, user)
                    else -> {
                        sendToGame(message, session)
                    }
                }
            }

        }
    }
}

fun chat(string: String?, session: WsSession, user: User) {
    if (string == null) {
        sendError("Missing message", session)
    } else {
        broadcast(string, session, user.name)
    }
}

fun join(name: String?, session: WsSession, user: User) {
    // find a game the user is in
    val game = games.find { it.players.contains(user) }
    if (game != null) {
        if(!game.join(user)) {
            sendError("Game ${game.name} is full", session)
        } else {
            broadcast("${user.name} has joined game ${game.name}", session)
        }
    } else {
        sendError("Game $name does not exist", session)
    }
}

fun login(name: String, session: WsSession) {
    // create if the user doesn't exist
    val user = users.find { name == it.name && (session.host() ?: "") == it.ip } ?: User(session.host() ?: "", name)
    // link to session
    loggedSession[session] = user
    broadcast("$name has logged in", session)
}

fun logout(session: WsSession) {
    broadcast("${loggedSession[session]?.name} has logged out", session)
    loggedSession.remove(session)
}

fun sendToGame(message: JSONObject, session: WsSession) {
    val user = loggedSession[session]!!
    // find a game the user is in
    val game = games.find { it.players.contains(user) }
    if (game != null) {
        game.handleMessage(message, session)
    } else {
        sendError("Player ${user.name} is in no game", session)
    }
}

fun sendError(error: String, session: WsSession) {
    session.send(JSONObject().put("type", "ERROR").put("message", error).toString())
}

fun broadcast(message: String, session: WsSession, sender: String = "Admin") {
    session.send(JSONObject().put("type", "CHAT").put("message", "[ $sender ] : $message").toString())
}

