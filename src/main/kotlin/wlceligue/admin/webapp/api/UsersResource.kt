package wlceligue.admin.webapp.api

import com.querydsl.jpa.JPQLQueryFactory
import org.jasig.cas.client.util.CommonUtils
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.security.cas.ServiceProperties
import org.springframework.security.cas.web.CasAuthenticationEntryPoint
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import wlceligue.admin.webapp.model.jpa.QUser
import wlceligue.admin.webapp.model.jpa.Season
import wlceligue.admin.webapp.model.jpa.User
import wlceligue.admin.webapp.model.json.SeasonCreate
import wlceligue.admin.webapp.model.json.UserCreate
import wlceligue.admin.webapp.model.json.UserSerie
import wlceligue.admin.webapp.model.repository.UserRepo
import wlceligue.admin.webapp.model.search.GameSearchBean
import wlceligue.admin.webapp.model.search.GameUserResultSearchBean
import wlceligue.admin.webapp.model.search.UserSearchBean
import wlceligue.admin.webapp.security.web.CurrentUser
import wlceligue.admin.webapp.service.UserSerieService
import wlceligue.admin.webapp.service.stats.UserStats
import wlceligue.admin.webapp.service.stats.UserStatsService
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/api/users")
class UsersResource(val jpqlQueryFactory: JPQLQueryFactory, val userSerieService: UserSerieService,
                    val userRepo: UserRepo, val userStatsService: UserStatsService,
                    val serviceProperties: ServiceProperties,
                    val casAuthenticationEntryPoint: CasAuthenticationEntryPoint) {

    @GetMapping
    @Secured("ROLE_USER")
    fun find(userSearchBean: UserSearchBean, page: Pageable?): Page<User> {
        return userSearchBean.find(jpqlQueryFactory, page)
    }

    @PostMapping
    @Secured("ROLE_USER")
    fun create(@RequestBody userCreate: UserCreate): User {
        val user = User().apply {
            das = userCreate.das ?: throw IllegalStateException("Das is missing!")
            firstname = userCreate.firstname ?: throw IllegalStateException("Firstname is missing!")
            lastname = userCreate.lastname ?: throw IllegalStateException("Lastname is missing!")
            email = userCreate.email ?: throw IllegalStateException("Email is missing!")
        }
        return userRepo.save(user)
    }

    @GetMapping("{id}")
    @Secured("ROLE_USER")
    fun get(@PathVariable("id") id: Int): User {
        return jpqlQueryFactory.selectFrom(QUser.user).where(QUser.user.id.eq(id)).fetchOne()
    }

    @GetMapping("{id}/stats")
    @Secured("ROLE_USER")
    fun getStatistics(@PathVariable("id") id: Int, gameSearchBean: GameUserResultSearchBean): UserStats {
        return userStatsService.getStatistics(id, gameSearchBean)
    }

    @GetMapping("/current")
    fun current(@CurrentUser userDetails: UserDetails?): ResponseEntity<User> {
        if (userDetails != null) {
            val user = jpqlQueryFactory.selectFrom(QUser.user).where(QUser.user.das.equalsIgnoreCase(userDetails.username)).fetchOne()
            if (user != null) {
                return ResponseEntity.ok(user)
            }
        }
        return ResponseEntity.noContent().build<User>()
    }

    @GetMapping("loginUrl")
    fun loginUrl(response: HttpServletResponse): String {
        val serviceUrl = CommonUtils.constructServiceUrl(null, response,
                this.serviceProperties.service, null,
                this.serviceProperties.artifactParameter,
                false)
        return CommonUtils.constructRedirectUrl(casAuthenticationEntryPoint.loginUrl,
                this.serviceProperties.serviceParameter, serviceUrl,
                this.serviceProperties.isSendRenew, false)
    }

    @GetMapping("/series")
    @Secured("ROLE_USER")
    fun getUserSeries(gameSearchBean: GameSearchBean): List<UserSerie> {
        return userSerieService.getUserSerieResponse(gameSearchBean)
    }

    @GetMapping("/series/{id}")
    @Secured("ROLE_USER")
    fun getUserSeries(@PathVariable("id") id: Int, gameSearchBean: GameSearchBean): UserSerie {
        return userSerieService.getUserSerie(userRepo.findById(id).orElseThrow { IllegalArgumentException("No user found with id $id") }, gameSearchBean)
    }

}
