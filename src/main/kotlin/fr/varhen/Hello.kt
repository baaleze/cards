package fr.varhen

import io.javalin.Javalin
import io.javalin.websocket.WsSession
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

val users = mutableListOf<User>()
val loggedSession = ConcurrentHashMap<WsSession, User>()
val games = mutableListOf<Game>()

fun main(args: Array<String>) {
    Javalin.create().server {
        val server = Server()
        server.apply {
            connectors = arrayOf(ServerConnector(this).apply {
                host = "localhost"
                port = 8080
            })
        }
        server
    }.apply {
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
                try {
                    handleMessage(JSONObject(message), session)
                } catch (e: JSONException) {
                    sendError("Incorrect JSON: $message", Error.JSON_INVALID, session)
                } catch (e: Exception) {
                    sendError("Server error ${e.message}", Error.SERVER_ERROR, session)
                    e.printStackTrace()
                }
            }
        }
    }.start()
}

fun handleMessage(message: JSONObject, session: WsSession) {
    when(message.getString("type") ?: "NULL") {
        "NULL" -> sendError("Missing type in message! $message", Error.MISSING_MESSAGE_TYPE, session) // missing type!
        "LOGIN" -> login(message.getString("message"), session)
        else -> {
            // check if logged in for all other actions
            if (!loggedSession.containsKey(session)) {
                sendError("Can't do that, the session did not login", Error.NOT_LOGGED_IN, session)
                return
            } else {
                val user = loggedSession[session]!!
                when (message.getString("type")) {
                    "LOGOUT" -> logout(session)
                    "CREATE_GAME" -> createGame(message.getString("name"), session, user)
                    "GAME_LIST" -> broadcastGameList(session)
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

fun createGame(name: String?, session: WsSession, user: User) {
    if (name == null) {
        sendError("Missing message", Error.MISSING_CHAT_MESSAGE, session)
    } else {
        val game = Game(name)
        game.players.add(user)
        games.add(game)
        broadcastGameList()
    }
}

fun broadcastGameList(session: WsSession? = null) {
    if (session != null) {
        session.send(JSONObject().put("type", "GAME_LIST").put("data", JSONArray(games)).toString())
    } else {
        broadcastJson(JSONObject().put("type", "GAME_LIST").put("data", JSONArray(games)))
    }
}

fun chat(string: String?, session: WsSession, user: User) {
    if (string == null) {
        sendError("Missing message", Error.MISSING_CHAT_MESSAGE, session)
    } else {
        broadcastChat(string, user.name)
    }
}

fun join(name: String?, session: WsSession, user: User) {
    // find a game the user is in
    val game = games.find { it.players.contains(user) }
    if (game != null) {
        if(!game.join(user)) {
            sendError("Game ${game.name} is full", Error.GAME_FULL, session)
        } else {
            broadcastChat("${user.name} has joined game ${game.name}")
        }
    } else {
        sendError("Game $name does not exist", Error.GAME_NOT_FOUND, session)
    }
}

fun login(name: String, session: WsSession) {
    // create if the user doesn't exist
    val user = users.find { name == it.name && (session.host() ?: "") == it.ip } ?: User(session.host() ?: "", name)
    // link to session
    loggedSession[session] = user
    broadcastChat("$name has logged in")
}

fun logout(session: WsSession) {
    broadcastChat("${loggedSession[session]?.name} has logged out")
    loggedSession.remove(session)
}

fun sendToGame(message: JSONObject, session: WsSession) {
    val user = loggedSession[session]!!
    // find a game the user is in
    val game = games.find { it.players.contains(user) }
    if (game != null) {
        game.handleMessage(message, session)
    } else {
        sendError("Player ${user.name} is in no game", Error.PLAYER_NOT_IN_GAME, session)
    }
}

fun sendError(error: String, errorCode: Error, session: WsSession) {
    session.send(JSONObject().put("type", "ERROR").put("message", error).put("errorCode", errorCode.toString()).toString())
}

fun broadcastChat(message: String, sender: String = "Admin") {
    loggedSession.forEach {(s, u) -> s.send(JSONObject().put("type", "CHAT").put("message", "[ $sender ] : $message").toString())}
}

fun broadcastJson(json: JSONObject) {
    loggedSession.forEach {(s, u) -> s.send(json.toString())}
}

