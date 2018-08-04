package wlceligue.admin.webapp.api

import com.querydsl.jpa.JPQLQueryFactory
import net.fortuna.ical4j.data.CalendarOutputter
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.DateTime
import net.fortuna.ical4j.model.Dur
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.property.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import wlceligue.admin.webapp.enums.Compet
import wlceligue.admin.webapp.enums.MatchStatus
import wlceligue.admin.webapp.model.jpa.Match
import wlceligue.admin.webapp.model.jpa.QMatch
import wlceligue.admin.webapp.model.repository.SeasonRepo
import wlceligue.admin.webapp.model.repository.UserRepo
import wlceligue.admin.webapp.model.search.GameSearchBean
import wlceligue.admin.webapp.security.web.CurrentUser
import wlceligue.admin.webapp.service.CalendarService
import wlceligue.admin.webapp.service.MatchGenerationService
import wlceligue.admin.webapp.service.MatchResult
import wlceligue.admin.webapp.service.MatchService
import java.time.Duration
import java.time.Instant
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@RestController
@RequestMapping("/api/games")
class GamesResource(val jpqlQueryFactory: JPQLQueryFactory,
                         val matchService: MatchService,
                         val matchGenerationService: MatchGenerationService,
                         val userRepo: UserRepo,
                         val seasonRepo: SeasonRepo,
                         val calendarService: CalendarService) {

    @GetMapping
    @Secured("ROLE_USER")
    fun findGames(gameSearchBean: GameSearchBean, page: Pageable?): Page<Match> {
        return gameSearchBean.find(jpqlQueryFactory, page)
    }

    @GetMapping("{id}")
    @Secured("ROLE_USER")
    fun get(@PathVariable("id") id: Int): Match {
        return jpqlQueryFactory.selectFrom(QMatch.match).where(QMatch.match.id.eq(id)).fetchOne()
    }

    @PostMapping("{id}/result")
    @Secured("ROLE_USER")
    fun updateMatchResult(@PathVariable("id") id: Int, @RequestBody matchResult: MatchResult, @CurrentUser user: UserDetails, httpServletRequest: HttpServletRequest): Match {
        val match = get(id)

        if (!httpServletRequest.isUserInRole("ROLE_ADMIN")) {
            require(user.username.equals(match.user1?.das, true) || user.username.equals(match.user2?.das, true),
                    { "Vous devez être l'un des joueurs pour mettre à jour le match" })
            require(match.date == null || match.date.isAfter(Instant.now().minusMillis(Duration.ofDays(7).toMillis())),
                    { "Vous devez être administrateur pour modifier un match de plus de 7 jours" })
        }

        matchService.updateGame(match, matchResult)
        return match
    }

    @PostMapping("{id}/cancel")
    @Secured("ROLE_ADMIN")
    fun updateMatchResult(@PathVariable("id") id: Int): Match {
        val match = get(id)
        val matchResult = MatchResult(cancel = true)
        matchService.updateGame(match, matchResult)
        return match
    }

    @PostMapping("/generate")
    @Secured("ROLE_ADMIN")
    fun generate(@RequestParam division: Int, @RequestParam season: Int) {
        matchGenerationService.generate(seasonRepo.findById(season).orElseThrow { IllegalArgumentException("No season found with id $season") }, division)
    }

    @PostMapping("/cupgenerate")
    @Secured("ROLE_USER")
    fun generateCup(@RequestParam season: Int) {
        val dbSeason = seasonRepo.findById(season).orElseThrow { IllegalArgumentException("No season found with id $season") }
        matchGenerationService.generateCup(dbSeason)
        seasonRepo.save(dbSeason)
    }

    @GetMapping("/ical", produces = arrayOf("text/calendar"))
    fun getCalendar(gameSearchBean: GameSearchBean, httpServletResponse: HttpServletResponse) {
        gameSearchBean.hasDate = true
        val games = gameSearchBean.find(jpqlQueryFactory)
        val calendar = calendarService.generateCalendar(games, calendarService.calendarName(gameSearchBean))
        CalendarOutputter().output(calendar, httpServletResponse.outputStream)
    }

}
