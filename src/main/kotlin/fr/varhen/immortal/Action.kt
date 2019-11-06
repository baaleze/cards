package fr.varhen.immortal

sealed class Action {
    object Invalid : Action()
    object Start : Action()
    data class UseAction(val cardId: Int): Action()
    data class PlayCard(val cardId: Int, useForGold: Boolean) : Action()
    data class ChooseCard(val cardId: Int?): Action()
    object Pass : Action()
}