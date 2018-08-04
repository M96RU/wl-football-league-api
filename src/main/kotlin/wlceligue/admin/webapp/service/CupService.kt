package wlceligue.admin.webapp.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import wlceligue.admin.webapp.enums.Compet
import wlceligue.admin.webapp.enums.MatchStatus
import wlceligue.admin.webapp.model.jpa.Match
import wlceligue.admin.webapp.model.jpa.Season
import wlceligue.admin.webapp.model.repository.MatchRepo
import wlceligue.admin.webapp.model.repository.TeamRepo
import wlceligue.admin.webapp.model.repository.UserRepo

@Service
open class CupService(val matchRepo: MatchRepo,
                      val userRepo: UserRepo,
                      val teamRepo: TeamRepo) {

    @Transactional
    open fun cupDraw(season: Season, draw: CupDraw): Match {

        val round = draw.round ?: throw IllegalArgumentException("round expected")
        val day = draw.day ?: throw IllegalArgumentException("day expected")

        val user = userRepo.findById(draw.user).orElseThrow { IllegalArgumentException("No user found with id ${draw.user}") }
        val team = teamRepo.findById(draw.team).orElseThrow { IllegalArgumentException("No team found with id ${draw.team}") }

        val matches = matchRepo.findBySeasonAndCompetAndRoundAndDay(season, Compet.CUP, draw.round, draw.day)
        require(matches.size == 1, { "should have only one match for season ${season.id} CUP round ${draw.round} day ${draw.day}" })

        // remove previous local match
        val previousRound = round - 1
        var previousDay = (day - 1) * 2 + 1
        if (!draw.local) {
            previousDay++
        }
        val previousMatches = matchRepo.findBySeasonAndCompetAndRoundAndDay(season, Compet.CUP, previousRound, previousDay)
        previousMatches.forEach(matchRepo::delete)

        // update match
        val match = matches[0]

        if (draw.local) {
            match.user1 = user
            match.team1 = team
        } else {
            match.user2 = user
            match.team2 = team
        }

        if (match.status == MatchStatus.EMPTY && match.user1 != null && match.team1 != null && match.user2 != null && match.team2 != null) {
            match.status = MatchStatus.INIT
        }

        return matchRepo.save(match)
    }

}
