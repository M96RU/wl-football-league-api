package wlceligue.admin.webapp.api

import com.querydsl.jpa.JPQLQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*
import wlceligue.admin.webapp.model.jpa.QTeamSeason
import wlceligue.admin.webapp.model.jpa.TeamSeason
import wlceligue.admin.webapp.model.search.TeamSeasonSearchBean
import wlceligue.admin.webapp.service.TeamSeasonService
import wlceligue.admin.webapp.service.team.TeamSeasonCreate
import wlceligue.admin.webapp.service.team.TeamSeasonUpdate

/**
 * Created by A171090 on 13/09/2017.
 */
@RestController
@RequestMapping("/api/teamSeasons")
open class TeamSeasonsResource(val jpqlQueryFactory: JPQLQueryFactory, val teamSeasonService: TeamSeasonService) {

    @GetMapping
    open fun find(teamSeasonSearchBean: TeamSeasonSearchBean, page: Pageable?): Page<TeamSeason> {
        return teamSeasonSearchBean.find(jpqlQueryFactory, page)
    }

    @PostMapping
    @Secured("ROLE_ADMIN")
    open fun create(@RequestBody teamSeasonCreate: TeamSeasonCreate): TeamSeason {
        return teamSeasonService.createTeamSeason(teamSeasonCreate);
    }

    @GetMapping("{id}")
    fun get(@PathVariable("id") id: Int): TeamSeason {
        return jpqlQueryFactory.selectFrom(QTeamSeason.teamSeason).where(QTeamSeason.teamSeason.id.eq(id)).fetchOne()
    }

    @PostMapping("{id}")
    @Secured("ROLE_ADMIN")
    open fun updateTeamSeason(@PathVariable("id") id: Int, @RequestBody teamSeasonUpdate: TeamSeasonUpdate): TeamSeason {
        val teamSeason = get(id)
        return teamSeasonService.updateTeamSeason(teamSeason, teamSeasonUpdate);
    }

}
