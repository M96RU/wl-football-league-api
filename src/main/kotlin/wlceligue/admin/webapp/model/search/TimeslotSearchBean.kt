package wlceligue.admin.webapp.model.search

import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.JPQLQuery
import lombok.Data
import wlceligue.admin.webapp.model.jpa.*
import java.time.Instant

@Data
class TimeslotSearchBean(var startDate: Instant? = null, var endDate: Instant? = null, var available: Boolean? = null) : AbstractSearchBean<Timeslot, QTimeslot>(QTimeslot.timeslot) {

    override fun doContribute(query: JPQLQuery<Timeslot>, queryEntity: QTimeslot) {
        startDate?.let { query.where(queryEntity.date.goe(it)) }
        endDate?.let { query.where(queryEntity.date.loe(it)) }
        available?.let {
            val minTimeslotSubquery = JPAExpressions.select(QTimeslot.timeslot.date.min()).from(QTimeslot.timeslot)
            val matchesDatesQuery = JPAExpressions.select(QMatch.match.date).from(QMatch.match).where(QMatch.match.date.goe(minTimeslotSubquery))
            if (it) {
                query.where(queryEntity.date.notIn(matchesDatesQuery))
            } else {
                query.where(queryEntity.date.`in`(matchesDatesQuery))
            }
        }
    }

}
