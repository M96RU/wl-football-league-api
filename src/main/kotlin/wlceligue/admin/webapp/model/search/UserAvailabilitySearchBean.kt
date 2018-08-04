package wlceligue.admin.webapp.model.search

import com.querydsl.jpa.JPQLQuery
import lombok.Data
import wlceligue.admin.webapp.model.jpa.QUserAvailability
import wlceligue.admin.webapp.model.jpa.UserAvailability
import java.time.Instant

@Data
class UserAvailabilitySearchBean(
        var userId: Int? = null,
        var dates: List<Instant>? = null): AbstractSearchBean<UserAvailability, QUserAvailability>(QUserAvailability.userAvailability) {

    override fun doContribute(query: JPQLQuery<UserAvailability>, queryEntity: QUserAvailability) {
        userId?.let { query.where(queryEntity.user.id.eq(userId)) }
        dates?.let { query.where(queryEntity.date.`in`(it)) }
    }

}
