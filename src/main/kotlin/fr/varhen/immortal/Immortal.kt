package fr.varhen.immortal

import fr.varhen.User

fun createImmortals(): List<Immortal> {
    return listOf(
        Immortal("Justice",
            { g, u ->
                if (!g.justiceRevealed) {
                    g.justiceRevealed = true
                    g.addSupremacy(u)
                }
            },
            {
                g, u -> g.supremacy[u]!! * 4 // count supremacies again
            }
        ),
        Immortal("Tomorrow",
            { g, u -> {} },
            { g, u -> g.tomorrowGuessPoints + g.chaos.map { it.value }.sum() }
        ),
        Immortal("Justice",
            { g, u ->
                if (!g.justiceRevealed) {
                    g.justiceRevealed = true
                    g.addSupremacy(u)
                }
            },
            {
                    g, u -> g.supremacy[u]!! * 4 // count supremacies again
            }
        ),
        Immortal("Justice",
            { g, u ->
                if (!g.justiceRevealed) {
                    g.justiceRevealed = true
                    g.addSupremacy(u)
                }
            },
            {
                    g, u -> g.supremacy[u]!! * 4 // count supremacies again
            }
        ),
        Immortal("Justice",
            { g, u ->
                if (!g.justiceRevealed) {
                    g.justiceRevealed = true
                    g.addSupremacy(u)
                }
            },
            {
                    g, u -> g.supremacy[u]!! * 4 // count supremacies again
            }
        ),
        Immortal("Justice",
            { g, u ->
                if (!g.justiceRevealed) {
                    g.justiceRevealed = true
                    g.addSupremacy(u)
                }
            },
            {
                    g, u -> g.supremacy[u]!! * 4 // count supremacies again
            }
        ),
        Immortal("Justice",
            { g, u ->
                if (!g.justiceRevealed) {
                    g.justiceRevealed = true
                    g.addSupremacy(u)
                }
            },
            {
                    g, u -> g.supremacy[u]!! * 4 // count supremacies again
            }
        ),
        Immortal("Justice",
            { g, u ->
                if (!g.justiceRevealed) {
                    g.justiceRevealed = true
                    g.addSupremacy(u)
                }
            },
            {
                    g, u -> g.supremacy[u]!! * 4 // count supremacies again
            }
        )
    )
}

class Immortal(val name: String, val onEnd: (ImmortalGame, User) -> Unit, val points: (ImmortalGame, User) -> Int) {}
