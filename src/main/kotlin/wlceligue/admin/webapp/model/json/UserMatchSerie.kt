package wlceligue.admin.webapp.model.json

import wlceligue.admin.webapp.model.jpa.Match
import wlceligue.admin.webapp.model.jpa.User

class UserMatchSerie(val user: User, val match: Match) {

    var points = 0
        private set

    var goalAverage = 0
        private set

    init {
        if (match.user1 == user) {
            goalAverage = match.score1 - match.score2
        } else {
            goalAverage = match.score2 - match.score1
        }
        if (goalAverage > 0) {
            points = 3
        } else if (goalAverage < 0) {
            points = 0
        } else {
            points = 1
        }
    }

}
