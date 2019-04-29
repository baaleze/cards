package fr.varhen.cards

sealed class Action {
    object Invalid : Action()
    object Start : Action()
    data class UseTokens(val nbMinusTokens: Int, val nbPlusTokens: Int): Action()
    data class PlayTile(val tileId: Int, val x: Int, val y: Int, val direction: Int) : Action()
    object Pass : Action()
}