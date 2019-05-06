package fr.varhen.dices

sealed class Action {
    object Invalid : Action()
    object Start : Action()
    data class BuyDice(val diceId: Int): Action()
    object Pass : Action()
}