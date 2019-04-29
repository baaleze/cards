package fr.varhen.cards

import fr.varhen.*

import io.javalin.websocket.WsSession
import org.json.JSONArray
import org.json.JSONObject

class CardGame(val n: String) : Game(n) {

    val gameLog = arrayListOf<String>()
    var gameState: GameState = Starting(this)
    val supply = mutableListOf<Tile>()
    val allTiles = buildAllTiles()
    val boards = mutableMapOf<User, Array<Array<Tile>>>()
    val gold = mutableMapOf<User, Int>()
    val points = mutableMapOf<User, Int>()
    val minusTokens = mutableMapOf<User, Int>()
    val plusTokens = mutableMapOf<User, Int>()

    var diceRoll = 0

    override fun handleMessage(
        message: JSONObject,
        session: WsSession,
        user: User
    ) {
        if (gameState.user != null && gameState.user != user ) {
            // wrong user sent command
            sendError("Illegal action, wrong user $user", Error.ILLEGAL_ACTION, session)
        } else {
            gameState = gameState.nextState(
                when (message.getString("type")) {
                    "START_GAME" -> Action.Start
                    "PLAY_TILE" -> {
                        val data = message.getJSONObject("data")
                        Action.PlayTile(
                            data.getInt("tileId"),
                            data.getInt("x"),
                            data.getInt("y"),
                            data.getInt("direction")
                        )
                    }
                    "USE_TOKENS" -> {
                        val data = message.getJSONObject("data")
                        Action.UseTokens(
                            data.getInt("nbMinusTokens"),
                            data.getInt("nbPlusTokens")
                        )
                    }
                    else -> {
                        sendError(
                            "Illegal message for game ${message.getString("type")}",
                            Error.ILLEGAL_ACTION,
                            session
                        )
                        Action.Invalid
                    }
                }
                , session
            )
        }
    }

    override fun generateInfo(): JSONObject {
        TODO("not implemented")
    }

    fun roll() {
        diceRoll = (1..6).shuffled().first()
    }

    fun broadcastGameLog() {
        broadcastJson(JSONObject().put("type", "GAME_LOG").put("data", JSONArray(gameLog)), players)
    }

    fun putTileOnPlayerBoard(
        user: User,
        tileId: Int,
        x: Int,
        y: Int,
        direction: Int
    ) {
        val tile = supply.find { it.id == tileId }?.let {
            it.direction = direction
            supply.remove(it)
            allTiles.remove(it)
            supply.add(allTiles.random())
            boards.getValue(user)[x][y] = it
            gold[user] = gold.getValue(user) - it.cost
        }
    }

    private fun buildAllTiles(): MutableList<Tile> {
        return mutableListOf(
            Tile(5, arrayOf(arrayOf(1)), 2, 0, 0, 0)
        ).run {
            shuffle()
            this
        }
    }
}