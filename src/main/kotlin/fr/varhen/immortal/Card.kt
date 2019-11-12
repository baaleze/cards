package fr.varhen.immortal

import fr.varhen.User

var nextId = 0

fun buildAllCards(nbBuildings: Int): MutableList<Card> {
    with(mutableListOf<Card>()) {
        addCard(this, 1,"Lion", 0, arrayOf(Faction.NARASHIMA), CardType.HERO, ::noop, false,
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
            ::noop, false, ::noop, ::cantDo, ::noopEnd)
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
            costsTwoCoin: Boolean,
            action: (Card, ImmortalGame, User, String) -> Unit,
            canDoAction: (Card, ImmortalGame, User, String) -> Boolean,
            onEnd: (Card, ImmortalGame, User) -> Unit): Unit {
    repeat(nb) {
        list.add(Card(name, number, factions, type, bonus, costsTwoCoin, action, canDoAction, onEnd))
    }
}

class Card(val name: String, val number: Int, val factions: Array<Faction>, val type: CardType,
           val bonus: (Card, ImmortalGame, User, String) -> Unit,
           val costsTwoCoin: Boolean,
           val action: (Card, ImmortalGame, User, String) -> Unit,
           val canDoAction: (Card, ImmortalGame, User, String) -> Boolean,
           val onEnd: (Card, ImmortalGame, User) -> Unit) {
    val id = nextId++
    var culture = 0
}
