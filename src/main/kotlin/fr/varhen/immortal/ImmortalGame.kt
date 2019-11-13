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
    val allImmortals = createImmortals()
    val discard = mutableListOf<Card>()
    val commerce = buildCommerce()
    val discardCommerce = mutableListOf<Commerce>()
    val coins = arrayOf(
        arrayOf(5,3,2,3,2), // round 1
        arrayOf(4,3,2,1) // round 2
    )

    val wonders = mutableListOf<Card>()
    var remainingDiamonds = 5

    val buildings = mutableMapOf<User, MutableList<Card>>()
    val heroes = mutableMapOf<User, MutableList<Card>>()
    val hands = mutableMapOf<User, MutableList<Card>>()
    val immortals = mutableMapOf<User, MutableList<Immortal>>()

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
    var currentUserWonderUses = 0

    var round = -1
    var draftTurn = 0
    var drafting = true
    var justiceRevealed = false
    var tomorrowGuessPoints = 0
    var narashimaCulture = 0
    val hasDrafted = mutableMapOf<User, Boolean>()
    val commerceChoice = mutableListOf<Commerce>()
    var immortalRevealCounter = 0

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
                        Action.UseAction(
                            data.getInt("cardId"),
                            data.getString("additionalArgs")
                        )
                    }
                    // TODO add other actions
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

    fun addToken(user: User, type: Commerce, nb: Int) {
        when(type) {
            Commerce.WAR -> war[user]?.plus(nb)
            Commerce.SCIENCE -> science[user]?.plus(nb)
            Commerce.CHAOS -> chaos[user]?.plus(nb)
            Commerce.COIN -> gold[user]?.plus(nb*2)
            Commerce.DIAMOND ->
                if (gold[user]!! >= 5 && remainingDiamonds > 0) {
                    gold[user]?.minus(5)
                    diamonds[user]?.plus(1)
                }
        }
    }

    fun drawCommerce(user: User, nb: Int) {
        // draw
        repeat(nb) { commerceChoice.add(commerce.removeAt(0)) }
        // change state
        gameState = AwaitingCommerceChoice(this, user)
    }

    fun destroy(id: Int, user: User) {
        // get card
        val card = findCard(id)
        if (card != null) {
            destroy(card, user)
        }
    }

    fun destroy(card: Card, user: User) {
        // remove from board
        buildings[user]?.removeIf { it.id == card.id }
        heroes[user]?.removeIf { it.id == card.id }
        // put in discard
        discard.add(card)
    }

    fun swapHands() {
        // TODO
    }

    fun play(id: Int, user: User, playForGold: Boolean, additionalArgs: String) {
        // get card
        val card = findCard(id)!!
        // remove from hand
        hands[user]?.removeIf { it.id == id }
        if (playForGold) {
            // return to supply and shuffle
            allCards.add(card)
            allCards.shuffle()
            // gain coin depending on draft turn
            gold[user]?.plus(coins[round][draftTurn])
        } else {
            // play to board
            when (card.type) {
                CardType.HERO -> heroes[user]!!.add(card)
                CardType.BUILDING -> buildings[user]!!.add(card)
                CardType.WONDER -> wonders.add(card)
            }
            // gain bonus
            card.bonus(card, this, user, additionalArgs)
            // if draft turn 4 round 1 gain twice!
            if (draftTurn == 3 && round == 0) {
                // potentially remove first part of args (like for EZ)
                card.bonus(card, this, user, additionalArgs.substringAfter('|'))
            }
        }
    }

    fun addSupremacy(user: User) {
        supremacy[user]?.plus(1)
    }

    fun hasSupremacy(user: User, commerce: Commerce): Boolean {
        return when(commerce) {
            Commerce.WAR -> hasSupremacy(user, war)
            Commerce.CHAOS -> hasSupremacy(user, chaos)
            Commerce.SCIENCE -> hasSupremacy(user, science)
            else -> false
        }
    }

    fun hasSupremacy(user: User, tokenCounts: MutableMap<User, Int>): Boolean {
        // get user's
        val own = tokenCounts[user]!!
        for ((u, c) in tokenCounts) {
            if (c > own) {
                return false // someone has more!
            } else if (c == own && u != user) {
                // same count with another !! are we justice ?
                return if (justiceRevealed && immortals[user]?.any { it.name == "Justice" } == true) {
                    true
                } else if (!justiceRevealed && immortals[user]?.any { it.name == "Justice" } == true) {
                    // reveal!
                    justiceRevealed = true
                    addSupremacy(user)
                    true
                } else {
                    false
                }
            }
        }
        return true // no one has same or more
    }

    fun addDiamond(user: User) {
        if (remainingDiamonds > 0) {
            diamonds[user]?.plus(1)
            remainingDiamonds--
        }
    }

    fun reveal(game: ImmortalGame): GameState {
        return when(immortalRevealCounter) {
            0 -> // justice
            {
                if (!justiceRevealed) {
                    val u = players.find { immortals[it]!![0].name == "Justice" }
                    if (u != null) { // one player has justice ?
                        allImmortals[immortalRevealCounter].onEnd(game, u)
                    }
                }
                immortalRevealCounter++
                reveal(game) // next
            }
            1 -> // tomorrow
            {
                val u = players.find { immortals[it]!![0].name == "Tomorrow" }
                immortalRevealCounter++
                if (u != null) { // one player has tomorrow ?
                    AwaitingTomorrowGuess(game, u)
                } else {
                    reveal(game)
                }
            }
            4 -> // narashima
            {
                val u = players.find { immortals[it]!![0].name == "Tomorrow" }
                immortalRevealCounter++
                if (u != null) { // one player has tomorrow ?
                    AwaitingNarashimaDestroy(game, u)
                } else {
                    reveal(game)
                }
            }
            2,3,5,6,7 -> // all else
            {
                immortalRevealCounter++
                reveal(game)
            }
            else -> Ended(game) // revealed everyone
        }
    }

    fun hasCard(id: Int, user: User): Boolean {
        return (buildings[user]?.any { it.id == id } == true) || (heroes[user]?.any { it.id == id } == true)
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