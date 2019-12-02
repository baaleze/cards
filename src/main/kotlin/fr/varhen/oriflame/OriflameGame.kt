package fr.varhen.oriflame

import fr.varhen.Game
import fr.varhen.User
import fr.varhen.broadcastJson
import fr.varhen.sendError
import fr.varhen.Error

import io.javalin.websocket.WsSession
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.min

class OriflameGame(n: String, val set: String? = "default") : Game(n) {

    val gameLog = arrayListOf<String>()
    var gameState: GameState = Starting(this)

    val board = mutableListOf<MutableList<Card>>()
    val hands = mutableMapOf<User, MutableList<Card>>()
    val discards = mutableMapOf<User, MutableList<Card>>()

    val points = mutableMapOf<User, Int>()

    var currentPile: MutableList<Card>? = mutableListOf()
    var remainingCardsToPlayThisRound = 0


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
                    "PLAY_CARD" -> {
                        val data = message.getJSONObject("data")
                        Action.PlayCard(
                            data.getString("name"),
                            data.getInt("pos")
                        )
                    }
                    "CHOOSE_REVEAL" -> {
                        val data = message.getJSONObject("data")
                        Action.ChooseReveal(
                            data.getBoolean("reveal")
                        )
                    }
                    "CHOOSE_TARGET" -> {
                        val data = message.getJSONObject("data")
                        Action.ChooseTarget(
                            players.find { it.id == data.getInt("owner") }!!,
                            data.getInt("pos"),
                            data.getInt("destPos")
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

    fun getCurrentCard(): Card {
        return currentPile!!.first()
    }

    override fun generateInfo(): JSONObject {
        return JSONObject().put("type", "GAME_INFO").put("data", JSONObject()
            .put("players", JSONArray(players.map{ playerInfo(it)}))
            .put("currentPlayer", gameState.user?.let { JSONObject(gameState.user) })
            .put("state", gameState.name())
            .put("board", cardListInfo(board))
        )
    }

    private fun cardListInfo(cardList: MutableList<MutableList<Card>>): JSONArray {
        return JSONArray(cardList.map { pile -> pile.map { cardInfo(it) } })
    }

    private fun cardInfo(card: Card): JSONObject {
        return JSONObject()
            .put("name", card.name)
            .put("owner", card.owner)
            .put("points", card.points)
            .put("revealed", card.revealed)
    }

    private fun playerInfo(it: User): JSONObject {
        return JSONObject()
            .put("user", JSONObject(it))
            .put("hand", JSONArray(hands[it]!!.map { c -> cardInfo(c) }))
            .put("discard", JSONArray(discards[it]!!.map { c -> cardInfo(c) }))
            .put("points", points[it]!!)
    }

    fun broadcastGameLog() {
        broadcastJson(JSONObject().put("type", "GAME_LOG").put("data", JSONArray(gameLog)), players)
    }

    override fun join(player: User): Boolean {
        return super.join(player) && gameState is Starting // only can join if not started yet
    }

    fun destroy(pos: Int, user: User) {
        val card = board[pos].removeAt(0)
        if (board[pos].isEmpty()) {
            board.removeAt(pos)
        }
        points[user]!!.plus(1)
        // is it ambush ?? and no assassin as assassin destroys itself
        if (card.name == "ambuscade" && currentPile!!.first().name != "assassin") {
            points[card.owner]!!.plus(4)
            destroyCurrentCard()
        }
    }

    fun destroyCurrentCard() {
        // destroy current card
        val i = board.indexOf(currentPile!!)
        currentPile!!.removeAt(0)
        if (currentPile!!.isEmpty()) {
            board.removeAt(i)
            // current is set to previous
            currentPile = if (i > 0) board[i - 1] else null
        }
    }

    fun stealFrom(userTo: User?, userFrom: User) {
        if (points[userFrom]!! > 0) {
            points[userFrom]!!.minus(1)
            points[userTo]!!.plus(1)
        }
    }

    fun moveCard(pos: Int, destPos: Int) {
        val card = board[pos].first()
        if (destPos > 0) { // 1 2 3 4..
            // put the card left
            board.add(destPos-1, mutableListOf(card))
        } else { // 0 -1 -2 -3...
            // put the card ON the pile
            board[-destPos].add(0, card)
        }
        // remove it from where it was
        board[pos].removeAt(0)
        if (board[pos].isEmpty()) {
            board.removeAt(pos)
        }
    }


}