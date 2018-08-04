package wlceligue.admin.webapp.service.stats

class UserStats {

    var userId: Int? = null

    var played = 0

    var scoreFor = 0
    var regulationTimeFor = 0
    var overtimeFor = 0
    var tabFor = 0

    var scoreAgainst = 0
    var regulationTimeAgainst = 0
    var overtimeAgainst = 0
    var tabAgainst = 0

    var win = 0
    var draw = 0
    var lose = 0

    var winRegulationTime = 0
    var winOvertime = 0
    var winTab = 0

    var loseRegulationTime = 0
    var loseOvertime = 0
    var loseTab = 0
}
