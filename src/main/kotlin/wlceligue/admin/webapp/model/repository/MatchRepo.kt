package wlceligue.admin.webapp.model.repository

import org.springframework.data.repository.CrudRepository
import wlceligue.admin.webapp.enums.Compet
import wlceligue.admin.webapp.enums.MatchStatus
import wlceligue.admin.webapp.model.jpa.Match
import wlceligue.admin.webapp.model.jpa.Season
import wlceligue.admin.webapp.model.jpa.User

interface MatchRepo : CrudRepository<Match, Int> {

    fun findBySeasonAndCompetAndRoundAndDay(season: Season, compet: Compet, round: Int?, day: Int?): List<Match>

    fun findBySeasonAndCompetAndRound(season: Season, compet: Compet, round: Int?): List<Match>

}
