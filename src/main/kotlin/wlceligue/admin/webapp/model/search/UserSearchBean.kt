package wlceligue.admin.webapp.model.search

import com.querydsl.jpa.JPQLQuery
import lombok.Data
import wlceligue.admin.webapp.model.jpa.QUser
import wlceligue.admin.webapp.model.jpa.QUserAvailability
import wlceligue.admin.webapp.model.jpa.User
import java.time.Instant

@Data
class UserSearchBean(var availableAt: Instant? = null) : AbstractSearchBean<User, QUser>(QUser.user) {

    override fun doContribute(query: JPQLQuery<User>, queryEntity: QUser) {
        availableAt?.let { query.from(QUserAvailability.userAvailability).where(QUser.user.eq(QUserAvailability.userAvailability.user).and(QUserAvailability.userAvailability.date.eq(availableAt))) }
    }

}
