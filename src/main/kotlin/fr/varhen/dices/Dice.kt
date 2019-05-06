package fr.varhen.dices

var nextId = 0

class Dice(val faces: Array<Face>, val group: Group, val cost: Int) {
    val id = nextId++
}