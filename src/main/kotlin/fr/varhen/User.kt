package fr.varhen

var nextId = 0

class User(val ip: String, val name: String) {
    val id = nextId++
}