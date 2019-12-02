package fr.varhen.oriflame

import fr.varhen.User

val ARCHER = CardMechanic {u,g ->
    AwaitingTarget(g, u, "ARCHER")
}
val SOLDAT = CardMechanic {u,g -> AwaitingTarget(g, u, "SOLDAT")}
val HERITIER = CardMechanic {u,g ->
    if (g.board.any { it.first().revealed && it.first().name == "héritier" })
        g.points[u]!!.plus(2)
    null
}
val ESPION = CardMechanic {u,g -> AwaitingTarget(g, u, "ESPION")}
val ASSASSIN = CardMechanic {u,g -> AwaitingTarget(g, u, "ASSASSIN")}
val DECRET_ROYAL = CardMechanic {u,g -> AwaitingTarget(g, u, "DECRET_ROYAL")}
val SEIGNEUR = CardMechanic {u,g ->
    val i = g.board.indexOf(g.currentPile)
    g.points[u]!!.plus(1
            + if (i > 0 && g.board[i - 1].first().owner == u) 1 else 0
            + if (i < g.board.count() - 1 && g.board[i + 1].first().owner == u) 1 else 0
    )
    null
}
val CHANGEFORME = CardMechanic {u,g -> AwaitingTarget(g, u, "CHANGEFORME")}
val AMBUSCADE = CardMechanic {u,g ->
    g.points[u]!!.plus(1)
    null
}
val COMPLOT = CardMechanic {u,g ->
    g.points[u]!!.plus(g.currentPile!!.first().points)
    null
}

class CardMechanic(val onPlay: (User, OriflameGame) -> GameState?)