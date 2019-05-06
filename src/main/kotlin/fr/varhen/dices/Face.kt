package fr.varhen.dices

import fr.varhen.User

sealed class Face {

    abstract fun apply(user: User, diceGame: DiceGame)

    class ResourceFace(val gold: Int, val points: Int): Face() {
        override fun apply(user: User, diceGame: DiceGame) {
            diceGame.gold[user]!!.plus(gold)
            diceGame.points[user]!!.plus(points)
        }
    }

    // TODO other type of faces go here
}
