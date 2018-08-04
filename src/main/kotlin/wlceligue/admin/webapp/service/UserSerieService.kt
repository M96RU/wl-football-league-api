package wlceligue.admin.webapp.service

import com.querydsl.jpa.JPQLQueryFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import wlceligue.admin.webapp.enums.MatchStatus
import wlceligue.admin.webapp.model.jpa.User
import wlceligue.admin.webapp.model.json.UserMatchSerie
import wlceligue.admin.webapp.model.json.UserSerie
import wlceligue.admin.webapp.model.repository.UserRepo
import wlceligue.admin.webapp.model.search.GameSearchBean

@Service
class UserSerieService(val userRepo: UserRepo,
                       val jpqlQueryFactory: JPQLQueryFactory) {

    fun getUserSerieResponse(gameSearchBean: GameSearchBean): List<UserSerie> = userRepo.findAll().map { getUserSerie(it, gameSearchBean) }.filter { it.matches.isNotEmpty() }

    fun getUserSerie(user: User, gameSearchBean: GameSearchBean): UserSerie {

        val userSerie = UserSerie()
        userSerie.user = user

        gameSearchBean.user = user.id
        gameSearchBean.status = MatchStatus.PLAYED

        val matches = gameSearchBean.find(jpqlQueryFactory, PageRequest.of(0, 5, Sort.Direction.DESC, "date"))

        for (match in matches) {
            val userMatchSerie = UserMatchSerie(user, match)
            userSerie.points += userMatchSerie.points
            userSerie.goalAverage += userMatchSerie.goalAverage
            userSerie.matches.add(userMatchSerie)
        }

        // Reverse the matchs (older first, recent last)
        userSerie.matches.reverse()

        if (userSerie.matches.isNotEmpty()) {
            val lastMatch = userSerie.matches.last().match
            userSerie.team = if (lastMatch.user1.id == user.id) lastMatch.team1 else lastMatch.team2
        }

        return userSerie
    }

}
