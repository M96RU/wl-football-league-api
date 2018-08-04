package wlceligue.admin.webapp.model.repository

import org.springframework.data.repository.CrudRepository
import wlceligue.admin.webapp.model.jpa.User
import wlceligue.admin.webapp.model.jpa.UserAvailability

interface UserAvailabilityRepo : CrudRepository<UserAvailability, Int>
