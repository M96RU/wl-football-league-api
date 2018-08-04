package wlceligue.admin.webapp.model.json

import wlceligue.admin.webapp.model.jpa.Team
import wlceligue.admin.webapp.model.jpa.User

class Ranking(val user: User) {

    var rank: Int = 1

    var team: Team? = null

    var goalFor: Int = 0

    var goalAgainst: Int = 0

    val goalAverage: Int
        get() = goalFor - goalAgainst

    var won: Int = 0

    var draw: Int = 0

    var lose: Int = 0

    val played: Int
        get() = won + draw + lose

    val points: Int
        get() = won * 3 + draw

}
