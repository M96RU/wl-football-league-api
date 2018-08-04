package wlceligue.admin.webapp.model

import wlceligue.admin.webapp.enums.Compet
import wlceligue.admin.webapp.enums.GameOutcome
import wlceligue.admin.webapp.enums.MatchStatus
import wlceligue.admin.webapp.model.jpa.GameUserResult
import wlceligue.admin.webapp.model.jpa.Match
import wlceligue.admin.webapp.model.jpa.User

/**
 * Created by a131199 on 1/19/17.
 */
class GameUserResultBuilder(val game: Match, val user: User) {

    fun build(): GameUserResult {
        val result = GameUserResult()
        result.season = game.season
        result.compet = game.compet
        when (game.compet) {
            Compet.CUP -> result.cupRound = game.round
            Compet.LEAGUE -> {
                result.leagueDay = game.day
                result.division = game.round
            }
        }
        result.status = game.status
        result.date = game.date
        result.game = game
        result.user = user
        result.userTeam = if (user.id == game.user1.id) game.team1 else game.team2
        result.against = if (user.id == game.user1.id) game.user2 else game.user1
        result.againstTeam = if (user.id == game.user1.id) game.team2 else game.team1
        
        if (game.status == MatchStatus.PLAYED && (is1() || is2())) {
            result.regulationTimeFor = if (is1()) game.score1?:0 else game.score2?:0
            result.overtimeFor = if (is1()) game.prolongation1?:0 else game.prolongation2?:0
            result.tabFor = if (is1()) game.tab1?:0 else game.tab2?:0
            result.goalsFor = result.regulationTimeFor + result.overtimeFor

            result.regulationTimeAgainst = if (is2()) game.score1?:0 else game.score2?:0
            result.overtimeAgainst = if (is2()) game.prolongation1?:0 else game.prolongation2?:0
            result.tabAgainst = if (is2()) game.tab1?:0 else game.tab2?:0
            result.goalsAgainst = result.regulationTimeAgainst + result.overtimeAgainst

            result.goalAverage = result.goalsFor - result.goalsAgainst

            result.win = result.goalsFor + result.tabFor > result.goalsAgainst + result.tabAgainst
            result.draw = result.goalsFor + result.tabFor == result.goalsAgainst + result.tabAgainst
            result.lose = result.goalsFor + result.tabFor < result.goalsAgainst + result.tabAgainst

            result.outcome = if (result.win) GameOutcome.WIN else if (result.draw) GameOutcome.DRAW else if (result.lose) GameOutcome.LOSE else null

            result.winRegulationTime = result.regulationTimeFor > result.regulationTimeAgainst
            result.winOvertime = result.regulationTimeFor == result.regulationTimeAgainst && result.overtimeFor > result.overtimeAgainst
            result.winTab = result.regulationTimeFor == result.regulationTimeAgainst && result.overtimeFor == result.overtimeAgainst && result.tabFor > result.tabAgainst

            result.loseRegulationTime = result.regulationTimeFor < result.regulationTimeAgainst
            result.loseOvertime = result.regulationTimeFor == result.regulationTimeAgainst && result.overtimeFor < result.overtimeAgainst
            result.loseTab = result.regulationTimeFor == result.regulationTimeAgainst && result.overtimeFor == result.overtimeAgainst && result.tabFor < result.tabAgainst
        }
        return result
    }

    private fun is1() = game.user1?.id == user.id
    private fun is2() = game.user2?.id == user.id
}