package wlceligue.admin.webapp.model.repository

import org.springframework.data.repository.CrudRepository
import wlceligue.admin.webapp.model.jpa.Season

interface SeasonRepo : CrudRepository<Season, Int>
