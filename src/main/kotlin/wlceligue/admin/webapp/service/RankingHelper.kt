package wlceligue.admin.webapp.service

import wlceligue.admin.webapp.enums.MatchStatus
import wlceligue.admin.webapp.model.jpa.Match
import wlceligue.admin.webapp.model.jpa.User
import wlceligue.admin.webapp.model.json.Ranking
import java.util.*

class RankingHelper(matches: List<Match>) {

    private val points: MutableMap<User, Ranking> = HashMap()

    init {
        for (match in matches) {
            calculPoints(match)
        }
    }

    private fun calculPoints(match: Match) {

        if (MatchStatus.PLAYED == match.status) {

            val ranking1 = getUserRanking(match.user1)
            val ranking2 = getUserRanking(match.user2)

            ranking1.goalFor += match.score1
            ranking1.goalAgainst += match.score2
            ranking2.goalFor += match.score2
            ranking2.goalAgainst += match.score1

            ranking1.team = match.team1
            ranking2.team = match.team2

            if (match.score1 > match.score2) {
                ranking1.won++
                ranking2.lose++
            } else if (match.score2 > match.score1) {
                ranking1.lose++
                ranking2.won++
            } else {
                ranking1.draw++
                ranking2.draw++
            }

            setUserRanking(match.user1, ranking1)
            setUserRanking(match.user2, ranking2)
        }
    }

    private fun setUserRanking(user: User, ranking: Ranking) {
        points.put(user, ranking)
    }

    private fun getUserRanking(user: User): Ranking {
        return points.getOrPut(user) { Ranking(user) }
    }

    // more points
    // same points, better goal average
    // same goal average, better goal scored
    val ranking: List<Ranking>
        get() {

            val listRanking = ArrayList<Ranking>()

            for (first in points.values) {
                for (second in points.values) {
                    if (second.points > first.points) {
                        first.rank++
                    } else if (second.points == first.points) {
                        if (second.goalAverage > first.goalAverage) {
                            first.rank++
                        } else if (second.goalAverage == first.goalAverage && second.goalFor > first.goalFor) {
                            first.rank++
                        }
                    }
                }
                listRanking.add(first)
            }

            listRanking.sortBy { it.rank }

            return listRanking
        }

}
