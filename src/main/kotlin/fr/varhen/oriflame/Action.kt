package fr.varhen.oriflame

import fr.varhen.User

sealed class Action {
    object Invalid : Action()
    object Start : Action()
    data class PlayCard(val name: String, val pos: Int) : Action()
    data class ChooseReveal(val reveal: Boolean): Action()
    data class ChooseTarget(val owner: User, val pos: Int, val destPos: Int): Action()
}