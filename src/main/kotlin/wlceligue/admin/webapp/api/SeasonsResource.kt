package wlceligue.admin.webapp.api

import com.querydsl.jpa.JPQLQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*
import wlceligue.admin.webapp.model.jpa.*
import wlceligue.admin.webapp.model.json.SeasonCreate
import wlceligue.admin.webapp.model.repository.SeasonRepo
import wlceligue.admin.webapp.model.search.SeasonSearchBean
import wlceligue.admin.webapp.service.CupDraw
import wlceligue.admin.webapp.service.CupService
import wlceligue.admin.webapp.service.SeasonService

@RestController
@RequestMapping("/api/seasons")
class SeasonsResource(val jpqlQueryFactory: JPQLQueryFactory, val seasonRepo: SeasonRepo, val seasonService: SeasonService, val cupService: CupService) {

    @GetMapping
    @Secured("ROLE_USER")
    fun find(seasonSearchBean: SeasonSearchBean, page: Pageable?): Page<Season> {
        return seasonSearchBean.find(jpqlQueryFactory, page)
    }

    @PostMapping
    @Secured("ROLE_ADMIN")
    fun create(@RequestBody seasonCreate: SeasonCreate): Season {
        return seasonService.create(seasonCreate)
    }

    @GetMapping("{id}")
    @Secured("ROLE_USER")
    fun get(@PathVariable("id") id: Int): Season {
        return jpqlQueryFactory.selectFrom(QSeason.season).where(QSeason.season.id.eq(id)).fetchOne()
    }

    @GetMapping("{id}/league/{division}/ranking")
    @Secured("ROLE_USER")
    fun ranking(@PathVariable("id") id: Int, @PathVariable division: Int): List<UserSeason> {
        return jpqlQueryFactory.selectFrom(QUserSeason.userSeason)
                .where(QUserSeason.userSeason.season.id.eq(id))
                .where(QUserSeason.userSeason.division.eq(division))
                .orderBy(QUserSeason.userSeason.rank.asc())
                .fetch()
    }

    @PostMapping("{id}/cup/draw")
    @Secured("ROLE_USER")
    fun cupDraw(@PathVariable("id") id: Int, @RequestBody draw: CupDraw): Match {
        val season = get(id)
        return cupService.cupDraw(season, draw)
    }

}
