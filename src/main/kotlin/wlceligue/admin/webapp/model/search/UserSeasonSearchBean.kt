package wlceligue.admin.webapp.model.search

import com.querydsl.jpa.JPQLQuery
import lombok.Data
import wlceligue.admin.webapp.model.jpa.QUserSeason
import wlceligue.admin.webapp.model.jpa.UserSeason

class UserSeasonSearchBean(var userId: Int? = null,
                           var seasonId: Int? = null,
                           var winSomething: Boolean? = null) : AbstractSearchBean<UserSeason, QUserSeason>(QUserSeason.userSeason) {

    override fun doContribute(query: JPQLQuery<UserSeason>, userSeason: QUserSeason) {
        userId?.let { query.where(userSeason.user.id.eq(it)) }
        seasonId?.let { query.where(userSeason.season.id.eq(it)) }
        winSomething?.let {
            val winSomethingPredicate = userSeason.cupWinner.isTrue.or(userSeason.leagueWinner.isTrue)
            if (it) query.where(winSomethingPredicate) else query.where(winSomethingPredicate.not())
        }
    }

}
