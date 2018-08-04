package wlceligue.admin.webapp.model.repository

import org.springframework.data.repository.CrudRepository
import wlceligue.admin.webapp.model.jpa.Season
import wlceligue.admin.webapp.model.jpa.TeamSeason

interface TeamSeasonRepo : CrudRepository<TeamSeason, Int> {

    fun findBySeason(season: Season): List<TeamSeason>

}
