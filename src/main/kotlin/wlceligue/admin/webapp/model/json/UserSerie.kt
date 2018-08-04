package wlceligue.admin.webapp.model.json

import java.util.ArrayList

import wlceligue.admin.webapp.model.jpa.Team
import wlceligue.admin.webapp.model.jpa.User
import wlceligue.admin.webapp.model.json.UserMatchSerie

class UserSerie {

    var user: User? = null

    var team: Team? = null

    var points = 0

    var goalAverage = 0

    var matches: MutableList<UserMatchSerie> = ArrayList()

}
