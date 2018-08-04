package wlceligue.admin.webapp.api

import com.querydsl.jpa.JPQLQueryFactory
import org.springframework.data.domain.Page
import org.springframework.security.access.annotation.Secured
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import wlceligue.admin.webapp.model.jpa.QUserAvailability
import wlceligue.admin.webapp.model.jpa.UserAvailability
import wlceligue.admin.webapp.model.repository.UserAvailabilityRepo
import wlceligue.admin.webapp.model.repository.UserRepo
import wlceligue.admin.webapp.model.search.UserAvailabilitySearchBean
import java.time.Instant

/**
 * Created by A131199 on 23/09/2016.
 */
@RestController
@RequestMapping("/api/availability")
class UserAvailabilityResource(val jpqlQueryFactory: JPQLQueryFactory, val userAvailabilityRepo: UserAvailabilityRepo, val userRepo: UserRepo) {

    @GetMapping
    fun search(searchBean: UserAvailabilitySearchBean): Page<UserAvailability> {
        return searchBean.find(jpqlQueryFactory)
    }

    @PostMapping("{date}/{userId}")
    @Transactional
    @Secured("ROLE_USER")
    fun declaredAvailable(@PathVariable userId: Int, @PathVariable date: Instant): UserAvailability {
        val availability = UserAvailability()
        availability.date = date
        availability.user = userRepo.findById(userId).orElseThrow { IllegalArgumentException("No user found with id $userId") }
        return userAvailabilityRepo.save(availability)
    }

    @DeleteMapping("{date}/{userId}")
    @Transactional
    @Secured("ROLE_USER")
    fun removeAvailable(@PathVariable userId: Int, @PathVariable date: Instant): UserAvailability? {
        val availability = jpqlQueryFactory.selectFrom(QUserAvailability.userAvailability)
                .where(QUserAvailability.userAvailability.user.id.eq(userId))
                .where(QUserAvailability.userAvailability.date.eq(date)).fetchOne()
        if (availability != null) {
            userAvailabilityRepo.delete(availability)
        }
        return availability
    }

}
