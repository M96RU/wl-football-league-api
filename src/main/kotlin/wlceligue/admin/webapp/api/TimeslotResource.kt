package wlceligue.admin.webapp.api

import com.querydsl.jpa.JPQLQueryFactory
import org.springframework.data.domain.Page
import org.springframework.security.access.annotation.Secured
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import wlceligue.admin.webapp.model.jpa.Timeslot
import wlceligue.admin.webapp.model.repository.TimeslotRepo
import wlceligue.admin.webapp.model.search.TimeslotSearchBean

/**
 * Created by A131199 on 23/09/2016.
 */
@RestController
@RequestMapping("/api/timeslots")
class TimeslotResource(val jpqlQueryFactory: JPQLQueryFactory, val timeslotRepo: TimeslotRepo) {

    @GetMapping
    @Secured("ROLE_USER")
    fun search(searchBean: TimeslotSearchBean): Page<Timeslot> {
        return searchBean.find(jpqlQueryFactory)
    }

    @PostMapping
    @Transactional
    @Secured("ROLE_USER")
    fun save(@RequestBody timeslot: Timeslot): Timeslot {
        return timeslotRepo.save(timeslot)
    }

    @DeleteMapping("{id}")
    @Transactional
    @Secured("ROLE_USER")
    fun delete(@PathVariable id: Long) {
        timeslotRepo.deleteById(id)
    }

}
