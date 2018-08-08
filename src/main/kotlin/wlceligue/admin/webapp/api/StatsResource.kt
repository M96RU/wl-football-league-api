package wlceligue.admin.webapp.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import wlceligue.admin.webapp.model.search.GameUserResultSearchBean
import wlceligue.admin.webapp.service.stats.UserStats
import wlceligue.admin.webapp.service.stats.UserStatsService

@RestController
@RequestMapping("/api/stats")
class StatsResource(val userStatsService: UserStatsService) {

    @GetMapping()
    fun getStatistics(gameSearchBean: GameUserResultSearchBean): List<UserStats> {
        return userStatsService.getStatistics(gameSearchBean)
    }

}
