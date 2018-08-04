package wlceligue.admin.webapp.model.search

import com.querydsl.jpa.JPQLQuery
import wlceligue.admin.webapp.model.jpa.QTeamSeason
import wlceligue.admin.webapp.model.jpa.TeamSeason

class TeamSeasonSearchBean(var teamId: Int? = null,
                           var seasonId: Int? = null) : AbstractSearchBean<TeamSeason, QTeamSeason>(QTeamSeason.teamSeason) {

    override fun doContribute(query: JPQLQuery<TeamSeason>, teamSeason: QTeamSeason) {
        teamId?.let { query.where(teamSeason.team.id.eq(it)) }
        seasonId?.let { query.where(teamSeason.season.id.eq(it)) }
    }

}
