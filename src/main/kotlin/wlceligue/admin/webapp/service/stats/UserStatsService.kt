package wlceligue.admin.webapp.service.stats

import com.querydsl.jpa.JPQLQueryFactory
import wlceligue.admin.webapp.model.jpa.GameUserResult
import wlceligue.admin.webapp.model.search.GameUserResultSearchBean
import javax.inject.Named

@Named
class UserStatsService(val jpqlQueryFactory: JPQLQueryFactory) {

    fun getStatistics(userId: Int, resultSearchBean: GameUserResultSearchBean): UserStats {
        resultSearchBean.user = userId
        val gameResults = resultSearchBean.find(jpqlQueryFactory)
        return buildUserStats(userId, gameResults)
    }

    fun getStatistics(resultSearchBean: GameUserResultSearchBean): List<UserStats> {
        val gameResults = resultSearchBean.find(jpqlQueryFactory)
        return gameResults.groupBy { it.user.id }.map { buildUserStats(it.key, it.value) }
    }

    private fun buildUserStats(userId: Int, gameResults: Iterable<GameUserResult>): UserStats {
        val response = UserStats()
        response.userId = userId

        response.scoreFor = gameResults.sumBy { it.goalsFor }
        response.regulationTimeFor = gameResults.sumBy { it.regulationTimeFor }
        response.overtimeFor = gameResults.sumBy { it.overtimeFor }
        response.tabFor = gameResults.sumBy { it.tabFor }

        response.scoreAgainst = gameResults.sumBy { it.goalsAgainst }
        response.regulationTimeAgainst = gameResults.sumBy { it.regulationTimeAgainst }
        response.overtimeAgainst = gameResults.sumBy { it.overtimeAgainst }
        response.tabAgainst = gameResults.sumBy { it.tabAgainst }

        response.win = gameResults.count { it.win }
        response.draw = gameResults.count { it.draw }
        response.lose = gameResults.count { it.lose }

        response.winRegulationTime = gameResults.count { it.winRegulationTime }
        response.winOvertime = gameResults.count { it.winOvertime }
        response.winTab = gameResults.count { it.winTab }

        response.loseRegulationTime = gameResults.count { it.loseRegulationTime }
        response.loseOvertime = gameResults.count { it.loseOvertime }
        response.loseTab = gameResults.count { it.loseTab }
        response.played = gameResults.count()
        return response
    }

}
