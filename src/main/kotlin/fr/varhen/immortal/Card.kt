package fr.varhen.immortal

import fr.varhen.User

var nextId = 0

fun buildAllCards(): MutableList<Card> {
    with(mutableListOf<Card>()) {
        addCard(this, 1,"Lion", arrayOf(Faction.NARASHIMA), ::noop, ::cantDo, ::noop, ::cantDo, ::noop)
        // TODO do the other cards lul
        return this
    }
}

fun noop(g: ImmortalGame, u: User): Unit {}
fun cantDo(g: ImmortalGame, u: User): Boolean { return false }

fun addCard(list: MutableList<Card>, nb: Int, name: String, factions: Array<Faction>,
            bonus: (ImmortalGame, User) -> Unit,
            canDoBonus: (ImmortalGame, User) -> Boolean,
            action: (ImmortalGame, User) -> Unit,
            canDoAction: (ImmortalGame, User) -> Boolean,
            onEnd: (ImmortalGame, User) -> Unit): Unit {
    repeat(nb) {
        list.add(Card(name, factions, bonus, canDoBonus, action, canDoAction, onEnd))
    }
}

class Card(val name: String, val factions: Array<Faction>,
           val bonus: (ImmortalGame, User) -> Unit,
           val canDoBonus: (ImmortalGame, User) -> Boolean,
           val action: (ImmortalGame, User) -> Unit,
           val canDoAction: (ImmortalGame, User) -> Boolean,
           val onEnd: (ImmortalGame, User) -> Unit) {
    val id = nextId++
}
