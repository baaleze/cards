package fr.varhen.immortal

sealed class Action {
    object Invalid : Action()
    object Start : Action()
    data class UseAction(val cardId: Int, val additionalArgs: String): Action()
    data class PlayCard(val cardId: Int, val useForGold: Boolean, val additionalArgs: String) : Action()
    object Pass : Action()
}