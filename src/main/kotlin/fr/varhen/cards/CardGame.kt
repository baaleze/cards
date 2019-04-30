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
    val boards = mutableMapOf<User, Array<Array<Tile?>>>()
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
        return JSONObject().put("type", "GAME_INFO").put("data", JSONObject()
            .put("boards", JSONArray(boards.map { (u, b) -> boardInfo(b, u) }))
            .put("players", JSONArray(players.map{ playerInfo(it)}))
        )// TODO
    }

    private fun playerInfo(it: User): JSONObject {
        return JSONObject().put("","") // TODO
    }

    private fun boardInfo(b: Array<Array<Tile?>>, u: User): JSONObject {
        return JSONObject().put("user", u).put("tiles", JSONArray(b))
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
            Tile(5, arrayOf(), 2, 0, 0, 0)
        ).run {
            shuffle()
            this
        }
    }

    /**
     * Applies the dice roll to get resources.
     * The main gameplay mechanics lies HERE.
     */
    fun applyRoll(user: User) {
        val board = boards[user]!!
        val reward = advanceOnBoard(2, 2, 0,0,0,0, diceRoll, mutableListOf(), board)
        // apply the reward
        gold[user] = gold[user]!! + reward.gold
        points[user] = points[user]!! + reward.points
        minusTokens[user] = minusTokens[user]!! + reward.minusTokens
        plusTokens[user] = plusTokens[user]!! + reward.plusTokens
    }

    private fun advanceOnBoard(
        x: Int, y: Int,
        gold: Int,
        points: Int,
        minusTokens: Int,
        plusTokens: Int,
        diceRoll: Int,
        visitedTiles: MutableList<Tile>,
        board: Array<Array<Tile?>>
    ): Reward {
        val currentTile = board[x][y]!!
        visitedTiles.add(currentTile)
        // get bonus on current tile
        var newGold = gold + currentTile.gold
        var newPoints = points + currentTile.points
        var newMinusTokens = minusTokens + currentTile.minusTokens
        var newPlusTokens = plusTokens + currentTile.plusTokens
        val newX: Int
        val newY: Int

        // get next tile
        when(currentTile.directions[diceRoll]) {
            0 -> {
                newX = x-2
                newY = y
            }
            1 -> {
                newX = x-1
                newY = y + (x+1)%2
            }
            2 -> {
                newX = x+1
                newY = y+ (x+1)%2
            }
            3 -> {
                newX = x+2
                newY = y
            }
            4 -> {
                newX = x+1
                newY = y-(x%2)
            }
            5 -> {
                newX = x-1
                newY = y-(x%2)
            }
            else -> return Reward(newGold, newPoints, newMinusTokens, newPlusTokens)
        }
        val nextTile = board.getOrNull(newX)?.getOrNull(newY)

        when {
            nextTile == null -> // no more tiles
                return Reward(newGold, newPoints, newMinusTokens, newPlusTokens)
            visitedTiles.contains(nextTile) -> {
                // loop! get double the bonus for the tile that looped
                var inLoop = false
                for(t in visitedTiles) {
                    if (t == nextTile) {
                        inLoop = true // we arrived in the loop begin to get bonus
                    }
                    if (inLoop) {
                        newGold += t.gold
                        newPoints += t.points
                        newMinusTokens += t.minusTokens
                        newPlusTokens += t.plusTokens
                    }
                }
                return Reward(newGold, newPoints, newMinusTokens, newPlusTokens)
            }
            else -> // next
                return advanceOnBoard(newX, newY, newGold, newPoints, newMinusTokens, newPlusTokens, diceRoll, visitedTiles, board)
        }
    }
}