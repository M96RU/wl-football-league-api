package wlceligue.admin.webapp.api

import com.querydsl.jpa.JPQLQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import wlceligue.admin.webapp.model.jpa.Team
import wlceligue.admin.webapp.model.search.TeamSearchBean

@RestController
@RequestMapping("/api/teams")
class TeamsResource(val jpqlQueryFactory: JPQLQueryFactory) {

    @GetMapping
    fun find(teamSearchBean: TeamSearchBean, page: Pageable?): Page<Team> {
        return teamSearchBean.find(jpqlQueryFactory, page)
    }

}
