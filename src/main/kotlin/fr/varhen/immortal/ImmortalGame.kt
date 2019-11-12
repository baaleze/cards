package fr.varhen.immortal

import fr.varhen.Game
import fr.varhen.User
import fr.varhen.broadcastJson
import fr.varhen.sendError
import fr.varhen.Error

import io.javalin.websocket.WsSession
import org.json.JSONArray
import org.json.JSONObject

class ImmortalGame(n: String, val set: String? = "default") : Game(n) {

    val gameLog = arrayListOf<String>()
    var gameState: GameState = Starting(this)
    val allCards = buildAllCards(2)
    val discard = mutableListOf<Card>()
    val commerce = buildCommerce()
    val discardCommerce = mutableListOf<Commerce>()

    val wonders = mutableListOf<Card>()
    var remainingDiamonds = 5

    val buildings = mutableMapOf<User, MutableList<Card>>()
    val heroes = mutableMapOf<User, MutableList<Card>>()
    val hands = mutableMapOf<User, MutableList<Card>>()

    val gold = mutableMapOf<User, Int>()
    val points = mutableMapOf<User, Array<Int>>()
    val chaos = mutableMapOf<User, Int>()
    val science = mutableMapOf<User, Int>()
    val war = mutableMapOf<User, Int>()
    val wonder = mutableMapOf<User, Int>()
    val supremacy = mutableMapOf<User, Int>()
    val pointTokens = mutableMapOf<User, Int>()
    val diamonds = mutableMapOf<User, Int>()

    // current turn variables
    val currentUserWonderUses = 0

    val round = 0
    val drafting = true
    val hasDrafted = mutableMapOf<User, Boolean>()

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
                            data.getInt("cardId"),
                            data.getBoolean("useForGold"),
                            data.getString("additionalArgs")
                        )
                    }
                    "USE_ACTION" -> {
                        val data = message.getJSONObject("data")
                        // TODO add additional info like target and other things
                        Action.UseAction(
                            data.getInt("cardId"),
                            data.getString("additionalArgs")
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

    fun hasBuildings(user: User): Boolean {
        return buildings[user]?.isNotEmpty() ?: false
    }

    fun hasOnlyThisCard(user: User, name: String, hero: Boolean): Boolean {
        return if (hero) {
            heroes[user]?.all { it.name == name } ?: false
        } else {
            buildings[user]?.all { it.name == name } ?: false
        }
    }

    fun getCulture(id: Int): Int {
        return findCard(id)?.culture ?: 0
    }

    fun destroy(id: Int, user: User) {
        // get card
        var card = findCard(id)
        if (card != null) {
            // remove from board
            buildings[user]?.removeIf { it.id == id }
            heroes[user]?.removeIf { it.id == id }
            // put in discard
            discard.add(card)
        }
    }

    fun addSupremacy(user: User) {
        supremacy[user]?.plus(1)
    }

    fun addDiamond(user: User) {
        if (remainingDiamonds > 0) {
            diamonds[user]?.plus(1)
            remainingDiamonds--
        }
    }

    fun findCard(id: Int): Card? {
        for ((u,cards) in buildings) {
            for (c in cards) {
                if (c.id == id) {
                    return c
                }
            }
        }
        for ((u,cards) in heroes) {
            for (c in cards) {
                if (c.id == id) {
                    return c
                }
            }
        }
        for (c in wonders) {
            if (c.id == id) {
                return c
            }
        }
        for (c in discard) {
            if (c.id == id) {
                return c
            }
        }
        return null
    }

    override fun generateInfo(): JSONObject {
        return JSONObject().put("type", "GAME_INFO").put("data", JSONObject()
            .put("players", JSONArray(players.map{ playerInfo(it)}))
            .put("currentPlayer", gameState.user?.let { JSONObject(gameState.user) })
            .put("state", gameState.name())
            .put("allCards", cardListInfo(allCards))
            .put("discard", cardListInfo(discard))
            .put("commerce", JSONArray(commerce))
            .put("discardCommerce", JSONArray(discardCommerce))
            .put("round", round)
            .put("currentWonderUse", currentUserWonderUses)
            .put("drafting", drafting)
        )
    }

    private fun mapInfo(map: MutableMap<User, MutableList<Card>>): JSONObject {
        with(JSONObject()) {
            for ((user, cards) in map) {
                this.put(user.id.toString(), cardListInfo(cards))
            }
            return this
        }
    }

    private fun cardListInfo(cardList: MutableList<Card>?): JSONArray {
        return if (cardList == null) {
            JSONArray()
        } else {
            JSONArray(cardList.map { it.id })
        }
    }

    private fun playerInfo(it: User): JSONObject {
        return JSONObject().put("user", JSONObject(it))
            .put("gold", gold[it])
            .put("hand", cardListInfo(hands[it]))
            .put("buildings", cardListInfo(buildings[it]))
            .put("heroes", cardListInfo(heroes[it]))
            .put("chaos", chaos[it])
            .put("science", science[it])
            .put("war", war[it])
            .put("wonder", wonder[it])
            .put("supremacy", supremacy[it])
            .put("pointTokens", pointTokens[it])
            .put("diamonds", diamonds[it])
    }

    fun broadcastGameLog() {
        broadcastJson(JSONObject().put("type", "GAME_LOG").put("data", JSONArray(gameLog)), players)
    }

    override fun join(player: User): Boolean {
        return super.join(player) && gameState is Starting // only can join if not started yet
    }

    /**
     * Orders player depending on their lowest number on their cards
     */
    fun orderPlayers(): User {
        // TODO
        return players[0]
    }
}