package wlceligue.admin.webapp.model.repository

import org.springframework.data.repository.CrudRepository
import wlceligue.admin.webapp.model.jpa.Season
import wlceligue.admin.webapp.model.jpa.User
import wlceligue.admin.webapp.model.jpa.UserSeason

interface UserSeasonRepo : CrudRepository<UserSeason, Int> {

    fun findBySeason(season: Season): List<UserSeason>

    fun findBySeasonAndDivision(season: Season, division: Int): List<UserSeason>

    fun findBySeasonAndUser(season: Season, user: User): UserSeason?

}
