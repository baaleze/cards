package fr.varhen.immortal

import fr.varhen.User

var nextId = 0


fun emptyCard(): Card {
    return Card("", 0, arrayOf(), CardType.HERO, ::noop, ::noop, ::cantDo, ::noopEnd)
}

fun buildAllCards(nbBuildings: Int): MutableList<Card> {
    with(mutableListOf<Card>()) {
        addCard(this, 1, "Alpha", 1, arrayOf(Faction.WAR), CardType.HERO,
            { c,g,u,s -> g.addToken(u, Commerce.WAR, 1) },
            { c,g,u,s -> g.drawCommerce(u, 1)},
            { c,g,u,s -> true },
            { c,g,u ->
                if (g.hasSupremacy(u, Commerce.WAR)) {
                    g.addSupremacy(u)
                }
            })
        addCard(this, 1, "Abunakashi", 2, arrayOf(Faction.NARASHIMA), CardType.HERO,
            ::noop,
            { c,g,u,s ->
                // pay 3 coins
                g.payMoney(u,3)
                val cardName = s.substringBefore('|')
                // get card
                val card = g.allCards.find { it.name == cardName } ?: g.discard.find { it.name == cardName }!!
                // remove it
                g.allCards.removeIf { it.name == cardName }
                g.discard.removeIf { it.name == cardName }
                // gain bonus
                card.bonus(card, g, u, s.substringAfter('|'))
                // put it on board
                g.heroes[u]?.add(card)
                // remove abunakashi
                g.heroes[u]?.remove(c)
                // put it in supply and shuffle
                g.allCards.add(c)
                g.allCards.shuffle()

            },
            // need 3 gold and hero must be in supply OR discard
            { c,g,u,s -> (g.gold[u] ?: 0) >= 3 &&
                    (g.allCards.any { it.name == s && it.type == CardType.HERO } || g.discard.any { it.name == s && it.type == CardType.HERO }) },
            ::noopEnd)
        addCard(this, 1, "Eliana", 3, arrayOf(Faction.SCIENCE), CardType.HERO,
            { c,g,u,s -> g.addToken(u, Commerce.SCIENCE, 1) },
            { c,g,u,s -> g.putCulture(u, s.toInt(), 2)},
            { c,g,u,s -> g.hasBuildings(u) },
            { c,g,u ->
                if (g.hasCard("Observatoire de Phoenix", u)) {
                    g.addSupremacy(u)
                }
            })
        addCard(this, 1, "Nezha", 4, arrayOf(Faction.SCIENCE, Faction.WAR), CardType.HERO,
            ::noop,
            { c,g,u,s ->
                // find target
                val other = g.players.find { it.id == s.toInt() }!!
                // give your chaos
                g.chaos[u]?.minus(1)
                g.addToken(other, Commerce.CHAOS, 1)
                // gain 1 point
                g.addPoints(u,1)
            },
            { c,g,u,s ->
                // find target
                val other = g.players.find { it.id == s.toInt() }!!
                g.chaos[u]!! > 0 && g.totalTokens(other) < 10
            },
            ::noopEnd)
        addCard(this, 1, "EZ", 5, arrayOf(Faction.CHAOS), CardType.HERO,
            { c,g,u,s ->
                // find target
                val playerId = s.substringBefore('|').toInt()
                val other = g.players.find { it.id == playerId }!!
                // both gain chaos
                g.addToken(u, Commerce.CHAOS, 1)
                g.addToken(other, Commerce.CHAOS, 1)
            },
            { c,g,u,s ->
                // get choice
                val getCoin = s == "GET_COIN"
                if (getCoin) {
                    g.addToken(u, Commerce.COIN, 2)
                } else {
                    g.addToken(u, Commerce.CHAOS, 1)
                }
            },
            { c,g,u,s -> true },
            ::noopEnd)
        addCard(this, 1, "Shadow", 6, arrayOf(Faction.CHAOS), CardType.HERO,
            { c,g,u,s ->
                g.addToken(u, Commerce.CHAOS, 1)
            },
            { c,g,u,s ->
                // get choice
                if (s != "") {
                    if (s.contains('|')) { // 2 targets
                        for (playerId in s.split('|')) {
                            val player = g.players.find { it.id == playerId.toInt() }!!
                            g.addToken(player, Commerce.CHAOS, 1)
                        }
                    } else { // 1 target
                        val player = g.players.find { it.id == s.toInt() }!!
                        g.addToken(player, Commerce.CHAOS, 1)
                    }
                }// no target
            },
            { c,g,u,s ->
                if (s != "") {
                    if (s.contains('|')) { // 2 targets
                        s.split('|')
                            .map { g.players.find { p -> p.id == it.toInt() }!! }
                            .all { g.totalTokens(it) < 10 }
                    } else { // 1 target
                        g.totalTokens(g.players.find { p -> p.id == s.toInt() }!!) < 10
                    }
                } else {
                    true // no target
                }
            },
            ::noopEnd)
        addCard(this, 1, "Cylak", 7, arrayOf(Faction.CHAOS), CardType.HERO,
            { c,g,u,s ->
                g.addToken(u, Commerce.CHAOS, 1)
            },
            { c,g,u,s ->
                if (s == "TOP_CARD") {
                    g.cylakTopCard = g.allCards[0]
                    g.gameState = AwatingCylakPlay(g, u)
                } else {
                    // play from discard
                    val cardName = s.substringBefore('|')
                    val card = g.discard.find { it.name == cardName }!!
                    card.action(card, g, u, s.substringAfter('|'))
                }
            },
            { c,g,u,s ->
                if (s == "TOP_CARD") {
                    true
                } else {
                    val cardName = s.substringBefore('|')
                    val card = g.discard.find { it.name == cardName }
                    card != null && card.canDoAction(card, g, u, s.substringAfter('|'))
                }
            },
            ::noopEnd)
        addCard(this, 1,"Lion", 11, arrayOf(Faction.NARASHIMA), CardType.HERO, ::noop,
            { c,g,u,s ->
                c.culture += g.getCulture(s.toInt())
                // destroy building
                g.destroy(s.toInt(), u)
                // gain diamond
                g.addDiamond(u)
            }, // apply effect
            { c,g,u,s -> g.hasBuildings(u) && s.toIntOrNull() != null },
            { c,g,u ->
                if (g.hasOnlyThisCard(u, "Lion", true)) {
                    g.addSupremacy(u)
                }
            })

        addCard(this, nbBuildings,"Observatoire de Phoenix", 0, arrayOf(Faction.SCIENCE), CardType.BUILDING,
            ::noop, ::noop, ::cantDo, ::noopEnd)
        // TODO do the other cards lul
        this.shuffle()
        return this
    }
}

fun noop(c: Card, g: ImmortalGame, u: User, additionalArgs: String): Unit {}
fun noopEnd(c: Card, g: ImmortalGame, u: User): Unit {}
fun cantDo(c: Card, g: ImmortalGame, u: User, additionalArgs: String): Boolean { return false }

fun addCard(list: MutableList<Card>, nb: Int, name: String, number: Int,
            factions: Array<Faction>,type: CardType,
            bonus: (Card, ImmortalGame, User, String) -> Unit,
            action: (Card, ImmortalGame, User, String) -> Unit,
            canDoAction: (Card, ImmortalGame, User, String) -> Boolean,
            onEnd: (Card, ImmortalGame, User) -> Unit): Unit {
    repeat(nb) {
        list.add(Card(name, number, factions, type, bonus, action, canDoAction, onEnd))
    }
}

class Card(val name: String, val number: Int, val factions: Array<Faction>, val type: CardType,
           val bonus: (Card, ImmortalGame, User, String) -> Unit,
           val action: (Card, ImmortalGame, User, String) -> Unit,
           val canDoAction: (Card, ImmortalGame, User, String) -> Boolean,
           val onEnd: (Card, ImmortalGame, User) -> Unit) {
    val id = nextId++
    var culture = 0
    var tapped = false
}
