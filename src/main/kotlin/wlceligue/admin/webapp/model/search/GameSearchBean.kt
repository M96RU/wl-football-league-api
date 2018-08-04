package wlceligue.admin.webapp.model.search

import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.JPQLQuery
import wlceligue.admin.webapp.enums.Compet
import wlceligue.admin.webapp.enums.MatchStatus
import wlceligue.admin.webapp.model.jpa.*
import java.time.Instant

class GameSearchBean(var season: Int? = null,
                     var division: Int? = null,
                     var user: Int? = null,
                     var against: Int? = null,
                     var compet: Compet? = null,
                     var status: MatchStatus? = null,
                     var date: Instant? = null,
                     var dates: List<Instant>? = null,
                     var hasDate: Boolean? = null,
                     var startDate: Instant? = null,
                     var endDate: Instant? = null,
                     var availableAt: Instant? = null,
                     var availableCount: Int? = 1) : AbstractSearchBean<Match, QMatch>(QMatch.match) {

    override fun doContribute(query: JPQLQuery<Match>, match: QMatch) {
        season?.let { query.where(match.season.id.eq(it)) }
        division?.let { query.where(match.round.eq(it).and(match.compet.eq(Compet.LEAGUE))) }
        if (user != null && against != null) {
            query.where(match.user1.id.eq(user).and(match.user2.id.eq(against))
                    .or(match.user1.id.eq(against).and(match.user2.id.eq(user))))
        } else {
            user?.let { query.where(match.user1.id.eq(it).or(match.user2.id.eq(it))) }
            against?.let { query.where(match.user1.id.eq(it).or(match.user2.id.eq(it))) }
        }
        compet?.let { query.where(match.compet.eq(it)) }
        status?.let { query.where(match.status.eq(it)) }
        date?.let { query.where(match.date.eq(it)) }
        dates?.let { query.where(match.date.`in`(it)) }
        hasDate?.let { if (it) {
            query.where((match.date.isNotNull))
        } else {
            query.where((match.date.isNull))
        }}
        startDate?.let { query.where(match.date.goe(it)) }
        endDate?.let { query.where(match.date.loe(it)) }
        availableAt?.let {
            val availableUsers = JPAExpressions.selectDistinct(QUserAvailability.userAvailability.user).from(QUserAvailability.userAvailability).where(QUserAvailability.userAvailability.date.eq(availableAt))
            if (user != null) {
                availableUsers.where(QUserAvailability.userAvailability.user.id.ne(user))
            }

            when (availableCount) {
                2 -> query.where(QMatch.match.user1.`in`(availableUsers).and(QMatch.match.user2.`in`(availableUsers)))
                else -> query.where(QMatch.match.user1.`in`(availableUsers).and(QMatch.match.user2.notIn(availableUsers))
                        .or(QMatch.match.user1.notIn(availableUsers).and(QMatch.match.user2.`in`(availableUsers))))
            }

        }
    }
}

