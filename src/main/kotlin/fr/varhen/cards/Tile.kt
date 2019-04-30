package fr.varhen.cards

var nextId = 0

class Tile(val cost: Int, val directions: Array<Int>, val gold: Int, val points: Int, val plusTokens: Int, val minusTokens: Int) {
    val id = nextId++
    var direction = 0
}
