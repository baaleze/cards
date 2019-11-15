package fr.varhen.immortal

import org.json.JSONArray

sealed class Action {
    object Invalid : Action()
    object Start : Action()
    data class UseAction(val cardId: Int, val additionalArgs: String): Action()
    data class PlayCard(val cardId: Int, val useForGold: Boolean, val additionalArgs: String) : Action()
    data class ChooseCommerce(val commerce: Commerce?): Action()
    data class Guess(val guesses: Map<Int, String>, val usedJoker: Boolean): Action()
    data class ChooseImmortal(val name: String): Action()
    data class ChooseWhatToDestroy(val cards: JSONArray): Action()
    data class PlayOrNot(val play: Boolean, val additionalArgs: String): Action()

    object Pass : Action()
}