package wlceligue.admin.webapp.api

import com.querydsl.jpa.JPQLQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import wlceligue.admin.webapp.enums.MatchStatus
import wlceligue.admin.webapp.model.jpa.Baton
import wlceligue.admin.webapp.model.jpa.Match
import wlceligue.admin.webapp.model.jpa.QBaton
import wlceligue.admin.webapp.model.jpa.QMatch
import wlceligue.admin.webapp.model.search.BatonSearchBean
import wlceligue.admin.webapp.service.BatonService

@RestController
@RequestMapping("/api/batons")
class BatonsResource(val jpqlQueryFactory: JPQLQueryFactory, val batonService: BatonService) {

    @GetMapping
    fun find(batonSearchBean: BatonSearchBean, page: Pageable?): Page<Baton> {
        return batonSearchBean.find(jpqlQueryFactory, page)
    }

    @GetMapping("/current")
    fun current(): Baton? {
        return batonService.getCurrentBaton()
    }

    @GetMapping("/{id}/games")
    fun batonGames(@PathVariable id: Long): List<Match> {

        val baton = jpqlQueryFactory.selectFrom(QBaton.baton).where(QBaton.baton.id.eq(id)).fetchOne()

        val query = jpqlQueryFactory.selectFrom(QMatch.match)
                .where(QMatch.match.user1.eq(baton.user).or(QMatch.match.user2.eq(baton.user)))
                .where(QMatch.match.date.goe(baton.wonDate))
                .where(QMatch.match.status.eq(MatchStatus.PLAYED))
        if (baton.lostDate != null) {
            query.where(QMatch.match.date.lt(baton.lostDate))
        }

        query.orderBy(QMatch.match.date.asc())

        return query.fetch()
    }

}
