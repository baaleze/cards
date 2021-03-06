package fr.varhen

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import fr.varhen.cards.CardGame
import fr.varhen.cards.Tile
import fr.varhen.dices.DiceGame
import io.javalin.Handler
import io.javalin.Javalin
import io.javalin.websocket.WsSession
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap

val users = mutableListOf<User>()
val loggedSession = ConcurrentHashMap<WsSession, User>()
val games = mutableListOf<Game>()
var setList = buildSetList()

fun main(args: Array<String>) {
    val h = args.getOrElse(0){ "0.0.0.0" }
    val p = args.getOrElse(1) { "8080" }.toInt()

    Javalin.create().server {
        val server = Server()
        server.apply {
            connectors = arrayOf(ServerConnector(this).apply {
                host = h
                port = p
            })
        }
        server
    }.apply {
        enableStaticFiles("/ngcards")
        exception(Exception::class.java) { e, ctx ->
            e.printStackTrace()
        }
        ws("/ws") { ws ->
            ws.onConnect { session ->
                println("${session.host()} joined.")
            }
            ws.onClose { session, status, message ->
                println("${session.host()} left. >($status) $message")
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

fun buildSetList(): List<String> {
    return File("./sets").listFiles().map {
        it.nameWithoutExtension
    }
}

fun handleMessage(message: JSONObject, session: WsSession) {
    when(message.getString("type") ?: "NULL") {
        "NULL" -> sendError("Missing type in message! $message", Error.MISSING_MESSAGE_TYPE, session) // missing type!
        "PING" -> session.send(JSONObject().put("type", "PONG").toString())
        "LOGIN" -> login(message.getString("message"), session)
        else -> {
            // check if logged in for all other actions
            if (!loggedSession.containsKey(session)) {
                sendError("Can't do that, the session did not login", Error.NOT_LOGGED_IN, session)
                return
            } else {
                val user = loggedSession[session]!!
                when (message.getString("type")) {
                    "SET_LIST" -> broadcastSetList(session)
                    "CREATE_SET" -> createSet(
                        message.getJSONObject("data").getString("setName"),
                        message.getJSONObject("data").getJSONArray("setList"),
                        session)
                    "LOGOUT" -> logout(session)
                    "CREATE_GAME" -> createGame(
                        message.getString("message"),
                        message.getJSONObject("data").getString("gameType"),
                        session,
                        user,
                        message.getJSONObject("data").getString("set")
                    )
                    "GAME_LIST" -> broadcastGameList(session)
                    "GET_GAME_INFO" -> broadcastGameInfo(message.getString("message"), session)
                    "JOIN_GAME" -> join(message.getString("message"), session, user)
                    "CHAT" -> chat(message.getString("message"), session, user)
                    else -> {
                        sendToGame(message, session, user)
                    }
                }
            }

        }
    }
}

fun createSet(setName: String, jsonArray: JSONArray?, session: WsSession) {
    if (jsonArray != null && jsonArray.length() > 0) {
        // write set into csv file, it is given in the form of an array of array
        File("./sets/$setName.csv").printWriter().use { out ->
            jsonArray.forEach {
                if (it is JSONArray) {
                    out.println(it.toList().map { i -> i.toString() }.joinToString(","))
                }
            }
        }

        // change set list
        if (!setList.contains(setName)) {
            setList = setList.plusElement(setName)
        }

        // broadcast it
        broadcastSetList(session)
        broadcastChat("$setName successfully edited!")
        session.send(JSONObject().put("type", "OK_SAVED").toString())
    } else {
        sendError("Invalid set array", Error.JSON_INVALID, session)
    }
}

fun readSet(setName: String): JSONArray {
    val csvReader = CSVReaderBuilder(InputStreamReader(FileInputStream("./sets/$setName.csv")))
        .withCSVParser(CSVParserBuilder().build())
        .build()
    return JSONArray().also {
        var line: Array<String>? = csvReader.readNext()
        while (line != null) {
            it.put(line)
            line = csvReader.readNext()
        }
    }

}

fun broadcastSetList(session: WsSession) {
    session.send(JSONObject().put("type", "SET_LIST").put("data", JSONArray(setList.map {
        JSONObject().put("setName", it).put("setList", readSet(it))
    })).toString())
}

fun broadcastGameInfo(gameName: String?, session: WsSession? = null) {
    if (gameName == null) {
        if (session != null) {
            sendError("Missing game name", Error.MISSING_GAME_NAME, session)
        } else {
            error("SHOULD NOT HAPPEN WTF")
        }
    } else {
        val game = games.find { it.name == gameName }
        if (game != null) {
            broadcastJson(game.generateInfo(), game.players)
        } else {
            if (session != null) {
                sendError("Game not found", Error.GAME_NOT_FOUND, session)
            } else {
                error("SHOULD NOT HAPPEN WTF")
            }
        }
    }
}

fun createGame(name: String, type: String, session: WsSession, user: User, set: String?) {
    when(type) {
        "CARDS" -> CardGame(name, set)
        "DICES" -> DiceGame(name)
        else -> {
            sendError("Incorrect game type", Error.JSON_INVALID, session)
            null
        }
    }?.let {
        it.players.add(user)
        games.add(it)
        broadcastGameList()
        broadcastChat("${user.name} created new game $name [$set]")
    }
}

fun broadcastUserList(session: WsSession? = null) {
    if (session != null) {
        session.send(JSONObject().put("type", "USER_LIST").put("data", JSONArray(loggedSession.map { (s, u) -> u })).toString())
    } else {
        broadcastJson(JSONObject().put("type", "USER_LIST").put("data", JSONArray(loggedSession.map { (s, u) -> u })))
    }
}

fun broadcastGameList(session: WsSession? = null) {
    val message = JSONObject().put("type", "GAME_LIST").put("data", JSONArray(games.map { it.desc() }))
    if (session != null) {
        session.send(message.toString())
    } else {
        broadcastJson(message)
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
    // find the game from name
    val game = games.find { it.name == name }
    if (game != null) {
        if (game.players.contains(user)){ // already in it do nothing
            return
        } else if(!game.join(user)) {
            sendError("Game ${game.name} is full", Error.GAME_FULL, session)
        } else {
            broadcastChat("${user.name} has joined game ${game.name}")
            broadcastGameList()
        }
    } else {
        sendError("Game $name does not exist", Error.GAME_NOT_FOUND, session)
    }
}

fun login(name: String, session: WsSession) {
    // create if the user doesn't exist
    var user = users.find { name == it.name && (session.host() ?: "") == it.ip };
    if (user == null) {
        user = User(session.host() ?: "", name)
        users.add(user)
    }
    // link to session
    loggedSession[session] = user
    broadcastChat("$name has logged in")
    broadcastUserList()
    broadcastGameList(session)
    broadcastSetList(session)
}

fun logout(session: WsSession) {
    broadcastChat("${loggedSession[session]?.name} has logged out")
    loggedSession.remove(session)
    broadcastUserList()
}

fun sendToGame(message: JSONObject, session: WsSession, user: User) {
    // find a game the user is in
    val game = games.find { it.players.contains(user) }
    if (game != null) {
        game.handleMessage(message, session, user)
        broadcastGameInfo(game.name, session)
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

fun broadcastJson(json: JSONObject, users: MutableList<User>? = null) {
    if (users == null) {
        loggedSession.forEach { (s, u) -> s.send(json.toString()) }
    } else {
        users.forEach {
            loggedSession.forEach { t: WsSession, u: User ->
                if (u == it) {
                    t.send(json.toString())
                }
            }
        }
    }
}

