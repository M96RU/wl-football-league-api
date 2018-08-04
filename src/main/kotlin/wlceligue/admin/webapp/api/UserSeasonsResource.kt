package wlceligue.admin.webapp.api

import com.querydsl.jpa.JPQLQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import wlceligue.admin.webapp.model.jpa.QUserSeason
import wlceligue.admin.webapp.model.jpa.UserSeason
import wlceligue.admin.webapp.model.repository.TeamRepo
import wlceligue.admin.webapp.model.repository.UserSeasonRepo
import wlceligue.admin.webapp.model.search.UserSeasonSearchBean
import wlceligue.admin.webapp.security.web.CurrentUser
import wlceligue.admin.webapp.service.MatchService
import wlceligue.admin.webapp.service.UserSeasonChoice
import wlceligue.admin.webapp.service.UserSeasonService
import javax.servlet.http.HttpServletRequest

/**
 * Created by A131199 on 23/09/2016.
 */
@RestController
@RequestMapping("/api/userSeasons")
class UserSeasonsResource(val jpqlQueryFactory: JPQLQueryFactory, val userSeasonService: UserSeasonService, val userSeasonRepo: UserSeasonRepo, val teamRepo: TeamRepo, val matchService: MatchService) {

    @GetMapping
    @Secured("ROLE_USER")
    fun find(userSeasonSearchBean: UserSeasonSearchBean, page: Pageable?): Page<UserSeason> {
        return userSeasonSearchBean.find(jpqlQueryFactory, page)
    }

    @GetMapping("{id}")
    @Secured("ROLE_USER")
    fun get(@PathVariable("id") id: Int): UserSeason {
        return jpqlQueryFactory.selectFrom(QUserSeason.userSeason).where(QUserSeason.userSeason.id.eq(id)).fetchOne()
    }

    @PostMapping("{id}/team")
    @Secured("ROLE_USER")
    fun choiceTeam(@PathVariable("id") id: Int, @RequestBody choice: UserSeasonChoice, @CurrentUser user: UserDetails, httpServletRequest: HttpServletRequest): UserSeason {
        val userSeason = get(id)

        if (!httpServletRequest.isUserInRole("ROLE_ADMIN")) {
            require(user.username.equals(userSeason.user?.das, true),
                    { "Vous ne pouvez pas choisir pour un autre" })
        }

        userSeasonService.updateUserTeam(userSeason, choice)
        matchService.updateUserTeam(userSeason)
        return userSeason
    }

}
