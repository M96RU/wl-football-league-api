package wlceligue.admin.webapp.model.search

import com.querydsl.jpa.JPQLQuery
import wlceligue.admin.webapp.enums.Compet
import wlceligue.admin.webapp.enums.MatchStatus
import wlceligue.admin.webapp.model.jpa.GameUserResult
import wlceligue.admin.webapp.model.jpa.QGameUserResult
import java.time.Instant

class GameUserResultSearchBean(var season: Int? = null,
                               var division: Int? = null,
                               var user: Int? = null,
                               var against: Int? = null,
                               var compet: Compet? = null,
                               var status: MatchStatus? = null,
                               var startDate: Instant? = null,
                               var endDate: Instant? = null,
                               var hasDate: Boolean? = null) : AbstractSearchBean<GameUserResult, QGameUserResult>(QGameUserResult.gameUserResult) {

    override fun doContribute(query: JPQLQuery<GameUserResult>, gameResult: QGameUserResult) {
        season?.let { query.where(gameResult.season.id.eq(it)) }
        division?.let { query.where(gameResult.division.eq(it)) }
        user?.let { query.where(gameResult.user.id.eq(it)) }
        against?.let { query.where(gameResult.against.id.eq(it)) }
        compet?.let { query.where(gameResult.compet.eq(it)) }
        status?.let { query.where(gameResult.status.eq(it)) }
        startDate?.let { query.where(gameResult.date.goe(it)) }
        endDate?.let { query.where(gameResult.date.loe(it)) }
        hasDate?.let {
            if (it) {
                query.where(gameResult.date.isNotNull)
            } else {
                query.where(gameResult.date.isNull)
            }
        }
    }
}

