package fr.varhen.immortal

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import fr.varhen.Game
import fr.varhen.User
import fr.varhen.broadcastJson

import io.javalin.websocket.WsSession
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileInputStream
import java.io.InputStreamReader

class ImmortalGame(n: String, val set: String? = "default") : Game(n) {

    val gameLog = arrayListOf<String>()
    var gameState: GameState = Starting(this)
    val supply = mutableListOf<Card>()
    val allCards = Card.buildAllCards()
    val commerce = buildCommerce()
    val gold = mutableMapOf<User, Int>()
    val points = mutableMapOf<User, Array<Int>>()
    val chaos = mutableMapOf<User, Int>()
    val science = mutableMapOf<User, Int>()
    val war = mutableMapOf<User, Int>()
    val wonder = mutableMapOf<User, Int>()
    val supremacy = mutableMapOf<User, Int>()
    val pointTokens = mutableMapOf<User, Int>()

    // current turn variables
    val currentUserWonderUses = 0;
    val currentUserCardChoices = mutableListOf<Card>();
    val currentUserTokenChoice = mutableListOf<Commerce>();

    var turn = 0

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
                    "PASS" -> Action.Pass
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
                .put("currentPlayer", gameState.user?.let { JSONObject(gameState.user) })
                .put("state", gameState.name())
                .put("supply", JSONArray(supply))
                .put("turn", 1 + turn / players.size)
                .put("diceRoll", diceRoll)
                .put("pathUsed", JSONArray(pathUsed))
        )
    }

    private fun playerInfo(it: User): JSONObject {
        return JSONObject().put("user", JSONObject(it))
                .put("gold", gold[it])
                .put("points", points[it])
                .put("minusTokens", minusTokens[it])
                .put("plusTokens", plusTokens[it])
    }

    fun broadcastGameLog() {
        broadcastJson(JSONObject().put("type", "GAME_LOG").put("data", JSONArray(gameLog)), players)
    }

    override fun join(player: User): Boolean {
        return super.join(player) && gameState is Starting // only can join if not started yet
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
            allCards.remove(it)
            supply.add(allCards.random())
            boards.getValue(user)[x][y] = it
            gold[user] = gold.getValue(user) - it.cost
        }
    }

    /**
     * Applies the dice roll to get resources.
     * The main gameplay mechanics lies HERE.
     */
    fun applyRoll(user: User) {
        val board = boards[user]!!
        // clear path
        pathUsed.clear()
        val reward = advanceOnBoard(boardSize / 2, boardSize / 2, 0,0,0,0, diceRoll, mutableListOf(), board)
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
        pathUsed.add(currentTile.id)
        // get bonus on current tile
        var newGold = gold + currentTile.gold
        var newPoints = points + currentTile.points
        var newMinusTokens = minusTokens + currentTile.minusTokens
        var newPlusTokens = plusTokens + currentTile.plusTokens
        val newX: Int
        val newY: Int

        // get next tile
        when((currentTile.directions.indexOf(diceRoll-1) + currentTile.direction) % 6) {
            1 -> {
                newX = x-1
                newY = y + (x+1)%2
            }
            2 -> {
                newX = x
                newY = y + 1
            }
            3 -> {
                newX = x+1
                newY = y + (x+1)%2
            }
            4 -> {
                newX = x+1
                newY = y - x%2
            }
            5 -> {
                newX = x
                newY = y-1
            }
            0 -> {
                newX = x-1
                newY = y - x%2
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