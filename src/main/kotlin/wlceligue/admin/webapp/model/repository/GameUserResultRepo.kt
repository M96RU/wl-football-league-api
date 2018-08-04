package wlceligue.admin.webapp.model.repository

import org.springframework.data.repository.CrudRepository
import wlceligue.admin.webapp.enums.Compet
import wlceligue.admin.webapp.enums.MatchStatus
import wlceligue.admin.webapp.model.jpa.GameUserResult
import wlceligue.admin.webapp.model.jpa.Match
import wlceligue.admin.webapp.model.jpa.Season
import wlceligue.admin.webapp.model.jpa.User

interface GameUserResultRepo : CrudRepository<GameUserResult, Int>
