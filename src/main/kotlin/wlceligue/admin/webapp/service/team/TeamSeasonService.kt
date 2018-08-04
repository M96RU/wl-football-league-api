package wlceligue.admin.webapp.service

import wlceligue.admin.webapp.model.jpa.Season
import wlceligue.admin.webapp.model.jpa.TeamSeason
import wlceligue.admin.webapp.model.repository.SeasonRepo
import wlceligue.admin.webapp.model.repository.TeamRepo
import wlceligue.admin.webapp.model.repository.TeamSeasonRepo
import wlceligue.admin.webapp.service.team.TeamSeasonCreate
import wlceligue.admin.webapp.service.team.TeamSeasonUpdate
import javax.inject.Named

/**
 * Created by a171090 on 17/09/2017.
 */
@Named
open class TeamSeasonService(val teamSeasonRepo: TeamSeasonRepo, val seasonRepo: SeasonRepo, val teamRepo: TeamRepo) {

    open fun createTeamSeason(teamSeasonCreate: TeamSeasonCreate): TeamSeason {
        return teamSeasonRepo.save(TeamSeason().apply {
            season = seasonRepo.findById(teamSeasonCreate.season).orElseThrow { IllegalArgumentException("No season found with id ${teamSeasonCreate.season}") }
            team = teamRepo.findById(teamSeasonCreate.team).orElseThrow { IllegalArgumentException("No team found with id ${teamSeasonCreate.team}") }
            division = teamSeasonCreate.division
            attack = teamSeasonCreate.attack
            midfield = teamSeasonCreate.midfield
            defence = teamSeasonCreate.defence
        })
    }

    fun updateTeamSeason(teamSeason: TeamSeason, teamSeasonUpdate: TeamSeasonUpdate): TeamSeason {
        teamSeason.division = teamSeasonUpdate.division
        teamSeason.attack = teamSeasonUpdate.attack
        teamSeason.midfield = teamSeasonUpdate.midfield
        teamSeason.defence = teamSeasonUpdate.defence
        return teamSeasonRepo.save(teamSeason);
    }

    fun initTeamSeason(newSeason: Season, previousSeason: Season) {

        for (teamSeason in teamSeasonRepo.findBySeason(previousSeason)) {
            teamSeasonRepo.save(TeamSeason().apply {
                season = newSeason
                team = teamSeason.team
                division = teamSeason.division
                attack = teamSeason.attack
                midfield = teamSeason.midfield
                defence = teamSeason.defence
            })
        }

    }

}