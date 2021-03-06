package fr.varhen.immortal


fun buildCommerce(): MutableList<Commerce> {
    return mutableListOf(
        Commerce.CHAOS,
        Commerce.CHAOS,
        Commerce.CHAOS,
        Commerce.SCIENCE,
        Commerce.SCIENCE,
        Commerce.SCIENCE,
        Commerce.WAR,
        Commerce.WAR,
        Commerce.WAR,
        Commerce.COIN,
        Commerce.COIN,
        Commerce.COIN,
        Commerce.DIAMOND,
        Commerce.DIAMOND
    ).also {
        it.shuffle()
    }
}

enum class Commerce {
    CHAOS, SCIENCE, WAR, COIN, DIAMOND
}