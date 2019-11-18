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
            { c,g,u,s -> g.putCulture(s.toInt(), 2)},
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
        addCard(this, 1,"Avatar de Galmi", 8, arrayOf(Faction.NARASHIMA), CardType.HERO,
            { c,g,u,s -> g.addWonder(u)},
            { c,g,u,s ->
                // copy own building
                val card = g.findCard(s.substringBefore('|').toInt())!!
                card.action(card, g, u, s.substringAfter('|'))
            },
            { c,g,u,s -> g.hasBuildings(u) && s.substringBefore('|').toIntOrNull() != null },
            { c,g,u ->
                g.addPoints(u, (g.diamonds[u] ?: 0) * 4) // gain 4 points for each diamond
            })
        addCard(this, 1,"Fenghuang", 9, arrayOf(Faction.SCIENCE), CardType.HERO,
            { c,g,u,s -> g.addToken(u, Commerce.SCIENCE, 1)},
            { c,g,u,s ->
                // copy blue card action anywhere
                val card = g.findCard(s.substringBefore('|').toInt())!!
                card.action(card, g, u, s.substringAfter('|'))
            },
            { c,g,u,s ->
                val card = g.findCard(s.substringBefore('|').toInt())
                card != null && card.factions.contains(Faction.SCIENCE) && !g.allCards.contains(card)
            },
            ::noopEnd)
        addCard(this, 1,"Valeen", 10, arrayOf(Faction.SCIENCE, Faction.WAR), CardType.HERO,
            { c,g,u,s ->
                g.addToken(u, Commerce.SCIENCE, 1)
                g.addToken(u, Commerce.WAR, 1)
            },
            { c,g,u,s ->
                // replace blue and red token with chaos
                g.science[u]?.minus(1)
                g.war[u]?.minus(1)
                g.chaos[u]?.plus(1)
                g.addPoints(u, 1)
            },
            { c,g,u,s -> g.science[u]!! > 0 && g.war[u]!! > 0},
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
        addCard(this, 1,"Orgah", 12, arrayOf(Faction.WAR), CardType.HERO,
            { c,g,u,s -> g.addToken(u, Commerce.WAR, 1)},
            { c,g,u,s ->
                // copy neightbor's building
                val card = g.findCard(s.substringBefore('|').toInt())!!
                card.action(card, g, u, s.substringAfter('|'))
            },
            { c,g,u,s ->
                val card = g.findCard(s.substringBefore('|').toInt())!!
                card.type == CardType.BUILDING && card.canDoAction(card, g, u, s.substringAfter('|'))
                        && g.neighborHasCard(u, card)
            },
            ::noopEnd)
        addCard(this, nbBuildings,"Armurerie de Goan-Sul", 25, arrayOf(Faction.WAR), CardType.BUILDING,
            { c,g,u,s -> g.addToken(u, Commerce.WAR, 1) },
            { c,g,u,s -> g.addToken(u, Commerce.WAR, 1)},
            { c,g,u,s -> g.totalTokens(u) < 10}, ::noopEnd)
        addCard(this, nbBuildings,"Observatoire de Phoenix", 27, arrayOf(Faction.SCIENCE), CardType.BUILDING,
            { c,g,u,s -> g.addToken(u, Commerce.SCIENCE, 1) },
            { c,g,u,s -> g.addToken(u, Commerce.SCIENCE, 1)},
            { c,g,u,s -> g.totalTokens(u) < 10}, ::noopEnd)
        addCard(this, nbBuildings,"Caravane de Xi'an", 29, arrayOf(), CardType.BUILDING,
            ::noop,
            { c,g,u,s ->
                val hasWar = g.heroes[u]!!.any { it.factions.contains(Faction.WAR) }
                val hasScience = g.heroes[u]!!.any { it.factions.contains(Faction.SCIENCE) }
                if (hasWar && hasScience && g.totalTokens(u) == 9) {
                    // must make a choice
                    g.commerceChoice.clear()
                    g.commerceChoice.add(Commerce.SCIENCE)
                    g.commerceChoice.add(Commerce.WAR)
                    g.gameState = AwaitingCommerceChoice(g, u)
                } else {
                    if (hasWar) {
                        g.addToken(u, Commerce.WAR, 1)
                    }
                    if (hasScience) {
                        g.addToken(u, Commerce.SCIENCE, 1)
                    }
                }
            },
            { c,g,u,s -> g.totalTokens(u) < 10},
            { c,g,u -> g.addPoints(u, g.getNumberOfTokenTriplets(u)) })
        addCard(this, nbBuildings,"Temple de Galmi", 31, arrayOf(Faction.GALMI), CardType.BUILDING,
            ::noop,
            { c,g,u,s ->
                g.payMoney(u, 2)
                g.addPoints(u, g.wonder[u]!! * 2)
            },
            { c,g,u,s -> g.gold[u]!! >= 2}, ::noopEnd)
        addCard(this, nbBuildings,"Ecole d'Elite de Justice", 33, arrayOf(Faction.WAR, Faction.SCIENCE), CardType.BUILDING,
            ::noop,
            { c,g,u,s ->
                g.payMoney(u, 2)
                g.addToken(u, if (s == "WAR") Commerce.WAR else Commerce.SCIENCE, 1)
            },
            { c,g,u,s -> g.gold[u]!! >= 2}, ::noopEnd)

        addCard(this, nbBuildings,"Autel de Narashima", 35, arrayOf(Faction.NARASHIMA), CardType.BUILDING,
            ::noop,
            { c,g,u,s ->
                // destroy hero
                g.destroyHero(u, s.toInt())
                g.addPoints(u, 3)
            },
            { c,g,u,s -> g.heroes[u]!!.isNotEmpty() && s.toIntOrNull() != null},
            { c,g,u -> if (g.hasOnlyThisCard(u, "Autel de Narashima", false)) {
                g.addSupremacy(u)
            }})
        addCard(this, nbBuildings,"Engins de Guerre de Goan-Sul", 37, arrayOf(Faction.WAR), CardType.BUILDING,
            ::noop,
            { c,g,u,s ->
                g.payMoney(u, 2)
                g.drawCommerce(u, 1)
                g.addToken(u, Commerce.WAR, 1)
            },
            { c,g,u,s -> g.gold[u]!! >= 2}, ::noopEnd)
        addCard(this, nbBuildings,"Bibliothèque de Phoenix", 39, arrayOf(Faction.SCIENCE), CardType.BUILDING,
            ::noop,
            { c,g,u,s ->
                g.payMoney(u, 2)
                g.findCard(s.toInt())!!.culture++
                g.addToken(u, Commerce.SCIENCE, 1)
            },
            { c,g,u,s -> g.gold[u]!! >= 2 && g.buildings.any { it.value.isNotEmpty() } && s.toIntOrNull() != null}, ::noopEnd)
        addCard(this, nbBuildings,"Portail du Chaos", 41, arrayOf(Faction.CHAOS), CardType.BUILDING,
            { c,g,u,s -> g.addToken(u, Commerce.CHAOS, 1) },
            { c,g,u,s ->
                // destroy the card
                g.destroy(c, u)
                // draw 3 cards
                g.chaosPortalCards.clear()
                repeat(3) {
                    g.chaosPortalCards.add(g.allCards[it])
                }
                g.gameState = AwatingChaosPortalPlay(g, u)
            },
            { c,g,u,s -> true }, ::noopEnd)
        addCard(this, nbBuildings,"Forteresse de Galmi", 43, arrayOf(Faction.GALMI), CardType.BUILDING,
            ::noop,
            { c,g,u,s ->
                g.payMoney(u, 2)
                val card = g.findCard(s.substringBefore('|').toInt())!!
                card.action(card, g, u, s.substringAfter('|'))
            },
            { c,g,u,s ->
                val card = g.findCard(s.substringBefore('|').toInt())
                g.gold[u]!! >= 2 && card != null &&
                        (card.type == CardType.BUILDING ||
                                (card.type == CardType.HERO && g.heroes[u]!!.any { it.id == card.id }))
            },
            { c,g,u -> g.addPoints(u, g.wonder[u] ?: 0)})
        addCard(this, nbBuildings,"Incroyable Marché de Xi'an", 45, arrayOf(), CardType.BUILDING,
            ::noop,
            { c,g,u,s ->
                g.drawCommerce(u, 2)
            },
            { c,g,u,s -> true }, ::noopEnd)
        addCard(this, nbBuildings,"Trésor de Byun Hyung Ja", 47, arrayOf(), CardType.BUILDING,
            ::noop,
            { c,g,u,s ->
                g.addToken(u, Commerce.COIN, 3)
            },
            { c,g,u,s -> true }, ::noopEnd)
        // WONDERS
        addCard(this, 1,"Hall de Guerre de Goan-Sul", 0, arrayOf(Faction.WAR), CardType.WONDER,
            { c,g,u,s -> g.addWonder(u)},
            { c,g,u,s ->
                val cardId = s.substringBefore('|').toInt()
                var card: Card? = null
                for ((user, buildings) in g.buildings) {
                    if (user != u && buildings.any { it.id == cardId }) {
                        card = buildings.find { it.id == cardId }!!
                    }
                }
                card?.action(card, g, u, s.substringAfter('|'))
            },
            { c,g,u,s ->
                g.buildings.any { entry -> entry.key != u && entry.value.any { it.id == s.substringBefore('|').toInt() } }
            }, ::noopEnd)
        addCard(this, 1,"Cité Volante de Phoenix", 0, arrayOf(Faction.SCIENCE), CardType.WONDER,
            { c,g,u,s -> g.addWonder(u)},
            { c,g,u,s ->
                for(b in g.buildings[u]!!) {
                    b.culture++
                }
            },
            { c,g,u,s -> true }, ::noopEnd)
        addCard(this, 1,"Fabuloserie de Xi'an", 0, arrayOf(), CardType.WONDER,
            { c,g,u,s -> g.addWonder(u)},
            { c,g,u,s ->
                if (s == "WAR") {
                    g.science[u]?.minus(1)
                    g.addToken(u, Commerce.WAR, 2)
                } else {
                    g.war[u]?.minus(1)
                    g.addToken(u, Commerce.SCIENCE, 2)
                }
            },
            { c,g,u,s -> true }, ::noopEnd)
        addCard(this, 1,"Statues Jumelles", 0, arrayOf(), CardType.WONDER,
            { c,g,u,s -> g.addWonder(u)},
            { c,g,u,s ->
                if (g.totalTokens(u) < 10) {
                    if (s == "WAR") {
                        if (g.war[u]!! == 1) {
                            g.war[u] = 2
                        } else if (g.war[u]!! == 0) {
                            g.war[u] = if (g.totalTokens(u) == 9) 1 else 2
                        }
                    } else {
                        if (g.science[u]!! == 1) {
                            g.science[u] = 2
                        } else if (g.science[u]!! == 0) {
                            g.science[u] = if (g.totalTokens(u) == 9) 1 else 2
                        }
                    }
                } // else do nothing
            },
            { c,g,u,s -> true }, ::noopEnd)
        addCard(this, 1,"Terres de Feu de Narashima", 0, arrayOf(), CardType.WONDER,
            { c,g,u,s -> g.addWonder(u)},
            { c,g,u,s ->
                val args = s.split(",")
                for (i in 1..2) {
                    g.addToken(u,
                        when(args[i]) {
                            "WAR" -> Commerce.WAR
                            "SCIENCE" -> Commerce.SCIENCE
                            else -> Commerce.CHAOS
                        }
                        , 1)
                }
                g.destroy(args[0].toInt(), u)
            },
            { c,g,u,s -> g.heroes[u]!!.isNotEmpty() || g.buildings[u]!!.isNotEmpty() }, ::noopEnd)
        addCard(this, 1,"Sanctuaire de Galmi", 0, arrayOf(Faction.GALMI), CardType.WONDER,
            { c,g,u,s -> g.addWonder(u)},
            { c,g,u,s ->
                g.payMoney(u, 5 - g.buildings[u]!!.count { it.factions.contains(Faction.GALMI) })
                g.addWonder(u)
            },
            { c,g,u,s -> g.gold[u]!! >= 5 - g.buildings[u]!!.count { it.factions.contains(Faction.GALMI) } }, ::noopEnd)
        addCard(this, 1,"Orbe du Chaos", 0, arrayOf(Faction.CHAOS), CardType.WONDER,
            { c,g,u,s -> g.addWonder(u)},
            { c,g,u,s ->
                val cardId = s.substringBefore('|').toInt()
                var card: Card? = null
                for ((user, heroes) in g.heroes) {
                    if (user != u && heroes.any { it.id == cardId }) {
                        card = heroes.find { it.id == cardId }!!
                    }
                }
                card?.action(card!!, g, u, s.substringAfter('|'))
            },
            { c,g,u,s ->
                g.heroes.any { entry -> entry.key != u && entry.value.any { it.id == s.substringBefore('|').toInt() } }
            }, ::noopEnd)
        addCard(this, 1,"Epées de Justice", 0, arrayOf(), CardType.WONDER,
            { c,g,u,s -> g.addWonder(u)},
            { c,g,u,s ->
                val args = s.split(",")
                for (i in 0..4) {
                    when(args[i]) {
                        "WAR" -> g.war[u]?.minus(1)
                        "SCIENCE" -> g.science[u]?.minus(1)
                        else -> g.chaos[u]?.minus(1)
                    }
                }
                g.addSupremacy(u)
            },
            { c,g,u,s -> g.totalTokens(u) >= 5 && s.split(",").count() == 5 }, ::noopEnd)
        addCard(this, 1,"Mine de Diamant", 0, arrayOf(), CardType.WONDER,
            { c,g,u,s -> g.addWonder(u)},
            { c,g,u,s ->
                g.payMoney(u, 5)
                g.addDiamond(u)
            },
            { c,g,u,s -> g.gold[u]!! >= 5 && g.remainingDiamonds > 0 }, ::noopEnd)
        addCard(this, 1,"Rituel des Ombres", 0, arrayOf(Faction.CHAOS), CardType.WONDER,
            { c,g,u,s -> g.addWonder(u)},
            ::noop, ::cantDo, ::noopEnd)
        addCard(this, 1,"Equilibrium", 0, arrayOf(), CardType.WONDER,
            { c,g,u,s -> g.addWonder(u)},
            ::noop, ::cantDo, ::noopEnd)
        addCard(this, 1,"Sablier d'Ambre", 0, arrayOf(Faction.GALMI), CardType.WONDER,
            { c,g,u,s -> g.addWonder(u)},
            ::noop, ::cantDo, ::noopEnd)
        this.shuffle()
        return this
    }
}

fun noop(c: Card, g: ImmortalGame, u: User, additionalArgs: String) {}
fun noopEnd(c: Card, g: ImmortalGame, u: User) {}
fun cantDo(c: Card, g: ImmortalGame, u: User, additionalArgs: String): Boolean { return false }

fun addCard(list: MutableList<Card>, nb: Int, name: String, number: Int,
            factions: Array<Faction>,type: CardType,
            bonus: (Card, ImmortalGame, User, String) -> Unit,
            action: (Card, ImmortalGame, User, String) -> Unit,
            canDoAction: (Card, ImmortalGame, User, String) -> Boolean,
            onEnd: (Card, ImmortalGame, User) -> Unit) {
    for (i in 0 until nb) {
        list.add(Card(name, number + i, factions, type, bonus, action, canDoAction, onEnd))
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
