package wlceligue.admin.webapp.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import wlceligue.admin.webapp.enums.Compet
import wlceligue.admin.webapp.enums.MatchStatus
import wlceligue.admin.webapp.model.jpa.Match
import wlceligue.admin.webapp.model.jpa.Season
import wlceligue.admin.webapp.model.repository.MatchRepo
import wlceligue.admin.webapp.model.repository.UserSeasonRepo
import java.util.*

@Service
open class MatchGenerationService(val matchRepo: MatchRepo,
                                  val userSeasonRepo: UserSeasonRepo) {

    @Transactional
    open fun generate(season: Season, division: Int) {

        val already = matchRepo.findBySeasonAndCompetAndRound(season, Compet.LEAGUE, division)

        require(already.isEmpty()) { "Error: ${already.size} games found" }

        val userSeasons = userSeasonRepo.findBySeasonAndDivision(season, division)

        require(userSeasons.size >= 3) { "Not enough player (${userSeasons.size}, expecting at least 3)" }

        var nbTeams = userSeasons.size
        var exempt = -1

        if (nbTeams % 2 == 1) {
            nbTeams++
            exempt = nbTeams
        }

        val matches = ArrayList<Match>()

        for (t1 in nbTeams downTo 1) {
            for (t2 in nbTeams downTo t1 + 1) {

                if (t1 != exempt && t2 != exempt) {

                    val match = Match()
                    match.season = season
                    match.round = division
                    match.compet = Compet.LEAGUE
                    match.status = MatchStatus.INIT
                    match.day = getDay(nbTeams, t1, t2)

                    var local = userSeasons[t1 - 1]
                    var away = userSeasons[t2 - 1]

                    if ((t1 + t2) % 2 == 0) {
                        // change local/away
                        val tmp = local
                        local = away
                        away = tmp
                    }

                    match.user1 = local.user
                    match.team1 = local.team
                    match.user2 = away.user
                    match.team2 = away.team

                    matches.add(match)
                }

            }
        }

        matchRepo.saveAll(matches)
    }

    private fun getDay(nbTeams: Int, t1: Int, t2: Int): Int {

        if (t2 == nbTeams) {
            if (2 * t1 <= nbTeams) {
                return 2 * t1 - 1
            }
            return 2 * t1 - nbTeams

        }

        if (t1 + t2 - 1 < nbTeams) {
            return t1 + t2 - 1
        }

        if (t1 + t2 - 1 >= nbTeams) {
            return t1 + t2 - nbTeams
        }

        return -1
    }


    @Transactional
    open fun generateCup(season: Season) {

        require(season.cup == 0) { "Cup already generated with (${season.cup} rounds" }

        season.cup = getNbRounds(season)

        require(season.cup > 3) { "Not enough rounds (${season.cup}, at least 3" }

        val matches = ArrayList<Match>()

        for (round in 1..season.cup) {

            val nbMatchs = Math.pow(2.toDouble(), (season.cup - round).toDouble())
            for (day in 1..nbMatchs.toInt()) {
                val match = Match().apply {
                    this.season = season
                    this.round = round
                    this.compet = Compet.CUP
                    this.status = MatchStatus.EMPTY
                    this.day = day
                }
                matches.add(match)
            }
        }
        matchRepo.saveAll(matches)
    }

    open fun getNbRounds(season: Season): Int {

        val users = userSeasonRepo.findBySeason(season)

        require(users.isNotEmpty()) { "Error: no users for the season" }

        var nbRounds = 0
        var teamsLeft = users.size.toDouble()

        while (teamsLeft > 1) {
            teamsLeft /= 2
            nbRounds++
        }
        return nbRounds
    }


}
