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
    var cylakTopCard = emptyCard()


    val hasDrafted = mutableMapOf<User, Boolean>()
    val commerceChoice = mutableListOf<Commerce>()
    val chaosPortalCards = mutableListOf<Card>()
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
                    "PLAY_OR_NOT" -> {
                        val data = message.getJSONObject("data")
                        Action.PlayOrNot(
                            data.getBoolean("play"),
                            data.getString("additionalArgs")
                        )
                    }
                    "CHOOSE_COMMERCE" -> {
                        val data = message.getJSONObject("data")
                        Action.ChooseCommerce(Commerce.valueOf(data.getString("commerce")))
                    }
                    "GUESS" -> {
                        val data = message.getJSONObject("data")
                        Action.Guess(parseGuess(data.getJSONObject("guess")), data.getBoolean("usedJoker"))
                    }
                    "CHOOSE_IMMORTAL" -> {
                        val data = message.getJSONObject("data")
                        Action.ChooseImmortal(data.getString("name"))
                    }
                    "CHOOSE_WHAT_TO_DESTROY" -> {
                        val data = message.getJSONObject("data")
                        Action.ChooseWhatToDestroy(data.getJSONArray("cardIds"))
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

    fun parseGuess(jsonObject: JSONObject): Map<Int, String> {
        return mutableMapOf<Int, String>().also {
            for(i in 0..players.count()) {
                it[i] = jsonObject[i.toString()] as String
            }
        }
    }

    fun getCulture(id: Int): Int {
        return findCard(id)?.culture ?: 0
    }

    fun addToken(user: User, type: Commerce, nb: Int) {
        val amount =
            if (totalTokens(user) + nb > 10 && type != Commerce.COIN && type != Commerce.DIAMOND)
                10 - totalTokens(user)
            else if (type == Commerce.COIN && gold[user]!! + nb > 10)
                10 - gold[user]!!
            else
                nb
        when(type) {
            Commerce.WAR -> war[user]?.plus(amount)
            Commerce.SCIENCE -> science[user]?.plus(amount)
            Commerce.CHAOS -> chaos[user]?.plus(amount)
            Commerce.COIN -> gold[user]?.plus(amount)
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
        if (round == 0) {
            val firstHand = hands[players[0]]!!
            for (i in 0..players.count()-2) {
                hands[players[i]] = hands[players[i+1]]!!
            }
            hands[players[players.count()-1]] = firstHand
        } else {
            val lastHand = hands[players[players.count()-1]]!!
            for (i in players.count()-1 downTo 1) {
                hands[players[i]] = hands[players[i-1]]!!
            }
            hands[players[0]] = lastHand
        }
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

    fun getNumberOfTokenTriplets(user: User): Int {
        return Math.min(Math.min(
            war[user] ?: 0, science[user] ?: 0
        ), chaos[user] ?: 0)
    }

    fun addPoints(user: User, nb: Int) {
        pointTokens[user]?.plus(nb)
    }

    fun addSupremacy(user: User) {
        supremacy[user]?.plus(1)
        // ecole d'élite!! pour chaque instance du batiment gagner 2pv
        repeat(buildings[user]!!.count { it.name == "Ecole d'Elite de Justice " }) {
            addPoints(user, 2)
        }
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
        // get user's (nezha gives 1 more)
        val own = tokenCounts[user]!! + if (heroes[user]!!.any { it.name == "Nezha" }) 1 else 0
        for ((u, count) in tokenCounts) {
            val c = count + if (heroes[u]!!.any { it.name == "Nezha" }) 1 else 0
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

    fun reveal(): GameState {
        return when(immortalRevealCounter) {
            0 -> // justice
            {
                if (!justiceRevealed) {
                    val u = players.find { immortals[it]!![0].name == "Justice" }
                    if (u != null) { // one player has justice ?
                        allImmortals[immortalRevealCounter].onEnd(this, u)
                    }
                }
                immortalRevealCounter++
                reveal() // next
            }
            1 -> // tomorrow
            {
                val u = players.find { immortals[it]!![0].name == "Tomorrow" }
                immortalRevealCounter++
                if (u != null) { // one player has tomorrow ?
                    AwaitingTomorrowGuess(this, u)
                } else {
                    reveal()
                }
            }
            4 -> // narashima
            {
                val u = players.find { immortals[it]!![0].name == "Tomorrow" }
                immortalRevealCounter++
                if (u != null) { // one player has tomorrow ?
                    AwaitingNarashimaDestroy(this, u)
                } else {
                    reveal()
                }
            }
            2,3,5,6,7 -> // all else
            {
                immortalRevealCounter++
                reveal()
            }
            else -> { // revealed everyone
                // apply on end effect
                for ((p, cards) in buildings) {
                    for (c in cards) {
                        c.onEnd(c, this, p)
                    }
                }
                for ((p, cards) in heroes) {
                    for (c in cards) {
                        c.onEnd(c, this, p)
                    }
                }
                Ended(this)
            }
        }
    }

    fun dealHands() {
        // clear to be safe
        hands.clear()
        // shuffle supply
        allCards.shuffle()
        val handSize = if (round == 0) 5 else 4
        for(p in players) {
            hands[p] = mutableListOf()
            repeat(handSize) {
                hands[p]!!.add(dealCard())
            }
        }
    }

    private fun dealCard(): Card {
        return allCards.removeAt(0)
    }

    fun hasCard(name: String, user: User): Boolean {
        return (buildings[user]?.any { it.name == name } == true) || (heroes[user]?.any { it.name == name } == true)
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
     * Orders player depending on their lowest number on their cards and returns first player
     */
    fun orderPlayers() {
        val lowestCardNumbers = mutableMapOf<User, Int>()
        for(p in players) {
            lowestCardNumbers[p] = lowerCardNumber(p)
        }
        players.sortBy { lowestCardNumbers[it] }
    }

    fun lowerCardNumber(user: User): Int {
        return Math.min(
            buildings[user]!!.minBy { it.number }?.number ?: 50,
            heroes[user]!!.minBy { it.number }?.number ?: 50
        )
    }

    fun putCulture(user: User, cardId: Int, nb: Int) {
        findCard(cardId)?.culture?.plus(nb)
    }

    fun payMoney(user: User, amount: Int) {
        gold[user]?.minus(amount)
    }

    fun totalTokens(other: User): Int {
        return chaos[other]!! + science[other]!! + war[other]!!
    }

    fun addWonder(user: User) {
        wonder[user]?.plus(1)
    }

    fun neighborHasCard(user: User, card: Card): Boolean {
        val left = players[(players.indexOf(user) - 1) % players.count()]
        val right = players[(players.indexOf(user) - 1) % players.count()]
        return hasCard(card.id, left) && hasCard(card.id, right)
    }

    fun destroyHero(user: User, cardId: Int) {
        val card = findCard(cardId)!!
        // remove it from board
        heroes[user]?.removeIf { it.id == cardId }
        // special effects
        if (card.name == "Fenghuang") {
            // put culture on every building
            buildings[user]!!.forEach { it.culture++ }
            // put it back in supply
            allCards.add(card)
            allCards.shuffle()
        } else if (card.factions.contains(Faction.CHAOS)) {
            discard.add(card)
            // if not already there put Rituel des Ombres in play
            if (wonders.none { it.name == "Rituel des Ombres" }) {
                val ritual = allCards.find { it.name == "Rituel des Ombres" }!!
                allCards.remove(ritual)
                wonders.add(ritual)
                addWonder(user)
            }
        } else {
            discard.add(card)
        }
    }
}