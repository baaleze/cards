package fr.varhen.dices

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import fr.varhen.Error
import fr.varhen.Game
import fr.varhen.User
import fr.varhen.sendError
import io.javalin.websocket.WsSession
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStreamReader

const val NB_TURNS = 15

class DiceGame(val n: String) : Game(n) {

    var gameState: GameState = Starting(this)
    val supply = mutableListOf<Dice>()
    val allDices = buildAllDices()
    val playerDices = mutableMapOf<User, MutableList<Dice>>()
    val gold = mutableMapOf<User, Int>()
    val points = mutableMapOf<User, Int>()
    val currentRoll = mutableListOf<Pair<Dice, Int>>()
    var turn = 0

    override fun handleMessage(message: JSONObject, session: WsSession, user: User) {
        if (gameState.user != null && gameState.user != user ) {
            // wrong user sent command
            sendError("Illegal action, wrong user $user", Error.ILLEGAL_ACTION, session)
        } else {
            gameState = gameState.nextState(
                when (message.getString("type")) {
                    "START_GAME" -> Action.Start
                    "BUY_DICE" -> {
                        val data = message.getJSONObject("data")
                        Action.BuyDice(
                            data.getInt("diceId")
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
            .put("playerDices", JSONArray(playerDices.map { (u, b) -> boardInfo(b, u) }))
            .put("players", JSONArray(players.map{ playerInfo(it)}))
            .put("currentPlayer", gameState.user?.let { JSONObject(gameState.user) })
            .put("state", gameState.name())
            .put("supply", JSONArray(supply))
            .put("turn", 1 + turn / players.size)
        )
    }

    private fun playerInfo(it: User): JSONObject {
        return JSONObject().put("user", JSONObject(it))
            .put("gold", gold[it])
            .put("points", points[it])
    }

    private fun boardInfo(b: List<Dice>, u: User): JSONObject {
        return JSONObject().put("user", JSONObject(u)).put("dices", JSONArray(b))
    }

    private fun buildAllDices(): MutableList<Dice> {

        val csvReader = CSVReaderBuilder(InputStreamReader(DiceGame::class.java.getResourceAsStream("/default.csv")))
            .withCSVParser(CSVParserBuilder().build())
            .build()
        csvReader.readNext() // header skip

        // Read the rest
        val tiles = mutableListOf<Dice>()
        var line: Array<String>? = csvReader.readNext()
        while (line != null) {
            tiles.add(
                diceFromCsvLine(line)
            )
            line = csvReader.readNext()
        }
        tiles.shuffle()

        return tiles
    }

    private fun diceFromCsvLine(line: Array<String>): Dice {
        TODO("not implemented")
    }

    fun giveDice(user: User, diceId: Int) {
        supply.find { it.id == diceId }!!.let {
            supply.remove(it)
            gold[user]!!.minus(it.cost)
            playerDices[user]!!.add(it)
        }
    }

    fun newRoll(user: User) {
        // clear old roll
        currentRoll.clear()
        // for every player dice
        playerDices[user]?.forEach {
            // get random index between 1 and number of faces
            val index = (1..it.faces.size).random()
            currentRoll.add(Pair(it, index))
            // apply the face
            it.faces[index].apply(user, this)
        }
    }


}