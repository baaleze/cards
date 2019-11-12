package fr.varhen.immortal


fun buildCommerce(): List<Commerce> {
    return listOf(
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
    ).shuffled()
}

enum class Commerce {
    CHAOS, SCIENCE, WAR, COIN, DIAMOND
}