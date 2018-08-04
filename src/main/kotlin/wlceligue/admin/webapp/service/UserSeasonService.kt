package wlceligue.admin.webapp.service

import com.querydsl.jpa.JPQLQueryFactory
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import wlceligue.admin.webapp.enums.Compet
import wlceligue.admin.webapp.enums.MatchStatus
import wlceligue.admin.webapp.model.jpa.Match
import wlceligue.admin.webapp.model.jpa.Season
import wlceligue.admin.webapp.model.jpa.User
import wlceligue.admin.webapp.model.jpa.UserSeason
import wlceligue.admin.webapp.model.json.SeasonUserEntry
import wlceligue.admin.webapp.model.repository.MatchRepo
import wlceligue.admin.webapp.model.repository.TeamRepo
import wlceligue.admin.webapp.model.repository.UserRepo
import wlceligue.admin.webapp.model.repository.UserSeasonRepo
import wlceligue.admin.webapp.model.search.GameUserResultSearchBean
import java.util.*

@Service
open class UserSeasonService(val userSeasonRepo: UserSeasonRepo,
                             val matchRepo: MatchRepo,
                             val userRepo: UserRepo,
                             val teamRepo: TeamRepo,
                             val jpqlQueryFactory: JPQLQueryFactory,
                             val template: SimpMessagingTemplate) {

    @Transactional
    open fun createUserSeasons(season: Season, seasonUserEntries: List<SeasonUserEntry>) {

        val users = ArrayList<UserSeason>()
        seasonUserEntries.forEach {
            val userSeason = UserSeason()
            userSeason.season = season
            userSeason.division = it.division
            userSeason.rank = 1
            userSeason.choice = it.choice
            userSeason.user = userRepo.findById(it.user?.id).orElseThrow { IllegalArgumentException("No user found with id $it.user.id") }

            if (userSeason.division > season.division) {
                season.division = userSeason.division
            }

            users.add(userSeason)
        }
        userSeasonRepo.saveAll(users)
    }

    @Transactional
    open fun updateUserTeam(userSeason: UserSeason, choice: UserSeasonChoice): UserSeason {

        userSeason.team = teamRepo.findById(choice.team).orElseThrow { IllegalArgumentException("No team found with id ${choice.team}") }
        userSeasonRepo.save(userSeason)

        template.convertAndSend("/topic/userSeason", userSeason)

        return userSeason;
    }

    @Transactional
    open fun updateUserSeason(match: Match) {

        if (Compet.LEAGUE == match.compet) {
            updateUserLeagueRanking(match.season, match.user1)
            updateUserLeagueRanking(match.season, match.user2)
            val userSeasons = computeDivisionRanking(match.season, match.round)
            userSeasonRepo.saveAll(userSeasons)
        }

        if (Compet.CUP == match.compet) {
            updateCup(match.season, match.user1)
            updateCup(match.season, match.user2)
        }

    }

    open fun computeDivisionRanking(season: Season, division: Int): List<UserSeason> {

        val userSeasons = userSeasonRepo.findBySeasonAndDivision(season, division)
        val matches = matchRepo.findBySeasonAndCompetAndRound(season, Compet.LEAGUE, division)

        updateRankPerPoints(userSeasons)
        updateRankPerDirectMatches(userSeasons, matches.filter { it.status == MatchStatus.PLAYED })
        updateRankPerGoals(userSeasons)

        for (userSeason in userSeasons) {
            // Compute total potential points
            val pointsToWin = matches.filter { it.user1.id == userSeason.user.id || it.user2.id == userSeason.user.id }.count { it.status != MatchStatus.PLAYED } * 3
            userSeason.potentialPoints = userSeason.points + pointsToWin
            userSeason.leagueWinner = false
        }

        for (userSeason in userSeasons.filter { it.rank == 1 }) {
            val otherUserSeasons = userSeasons.filter { it.user.id != userSeason.user.id }
            if (otherUserSeasons.all { it.potentialPoints < userSeason.points }) {
                // Nobody can have as many points as the current leader
                userSeason.leagueWinner = true
            } else if (otherUserSeasons.any { it.potentialPoints == userSeason.points }) {
                // Lets find users that can have as many points as the leader
                val potentialSamePointsUserSeasons = otherUserSeasons.filter { it.potentialPoints == userSeason.points }

                // If the leader won all his matchs against potential leaders, he is sure to be winner
                var wonAgainstAllSamePoints = true
                for (potentialSamePointsUserSeason in potentialSamePointsUserSeasons) {
                    // If the current user has already won against potential same points, he is sure to be best
                    wonAgainstAllSamePoints = wonAgainstAllSamePoints && GameUserResultSearchBean(user = userSeason.user.id,
                            against = potentialSamePointsUserSeason.user.id,
                            division = division,
                            season = season.id)
                            .find(jpqlQueryFactory).any { it.win }
                }
                if (wonAgainstAllSamePoints) {
                    userSeason.leagueWinner = true
                }

                // If everyone has played his matchs, let's trust the current ranking.
                if (userSeason.points == userSeason.potentialPoints && potentialSamePointsUserSeasons.all { it.points == it.potentialPoints }) {
                    userSeason.leagueWinner = true
                }

                // Else we must wait for other games to be played (goal average may change the winner)
            }

        }

        return userSeasons

    }

    private fun updateRankPerGoals(userSeasons: List<UserSeason>) {

        for (first in userSeasons) {
            for (other in userSeasons) {
                if (other.rank == first.rank) {
                    if (other.goalAverage > first.goalAverage) {
                        // same rank, better goal average
                        first.rank++
                    } else if (other.goalAverage == first.goalAverage && other.goalFor > first.goalFor) {
                        // same rank, same goal average, better goal for
                        first.rank++
                    }
                }
            }
        }
    }

    private fun updateRankPerDirectMatches(userSeasons: List<UserSeason>, matches: List<Match>) {

        val done = ArrayList<UserSeason>()

        for (first in userSeasons) {

            if (!done.contains(first)) {

                // not done atm
                done.add(first)

                val sameRank = HashMap<User, UserSeason>()
                sameRank.put(first.user, first)

                for (other in userSeasons) {

                    if (!done.contains(other) && other.rank == first.rank) {
                        done.add(other)
                        sameRank.put(other.user, other)
                    }
                }

                if (sameRank.size > 1) {
                    val directMatches = getDirectMatches(sameRank, matches)
                    val rankingHelper = RankingHelper(directMatches)
                    val rankings = rankingHelper.ranking
                    for (ranking in rankings) {
                        val userRanking = sameRank[ranking.user]
                        if (userRanking != null) {
                            userRanking.rank += ranking.rank - 1
                        }
                    }
                }

            }
        }
    }

    private fun getDirectMatches(sameRank: Map<User, UserSeason>, matches: List<Match>): List<Match> {

        val directMatches = ArrayList<Match>()

        for (m in matches) {
            val userSeason1 = sameRank[m.user1]
            val userSeason2 = sameRank[m.user2]
            if (userSeason1 != null && userSeason2 != null) {
                directMatches.add(m)
            }
        }

        return directMatches
    }

    private fun updateRankPerPoints(userSeasons: List<UserSeason>) {

        for (first in userSeasons) {
            first.rank = 1
            for (other in userSeasons) {
                if (other.points > first.points) {
                    first.rank++
                }
            }
        }

    }

    @Transactional
    open fun updateCup(season: Season, user: User) {
        val userSeason = userSeasonRepo.findBySeasonAndUser(season, user)

        if (userSeason != null) {
            computeCup(userSeason)
            userSeasonRepo.save(userSeason)
        }

    }

    open fun computeCup(userSeason: UserSeason) {
        val results = GameUserResultSearchBean(season = userSeason.season.id, compet = Compet.CUP, user = userSeason.user.id, status = MatchStatus.PLAYED)
                .find(jpqlQueryFactory)

        userSeason.cupRound = if (results.totalElements == 0L) 0 else results.maxBy { it.cupRound }!!.cupRound
        userSeason.cupInProgress = if (results.totalElements == 0L) true else results.maxBy { it.cupRound }!!.run { cupRound < season.cup && win }
        if (userSeason.cupInProgress) {
            // Where are still in progress, so either winner of the last match, or no match played for now. Anyway we can say we are at next round that last match result
            userSeason.cupRound++
        }
        userSeason.cupWinner = results.any { it.cupRound == userSeason.season.cup && it.win }
        userSeason.cupGoalFor = results.sumBy { it.goalsFor }
        userSeason.cupGoalAgainst = results.sumBy { it.goalsAgainst }
        userSeason.cupWon = results.count { it.win }
        userSeason.cupLose = results.count { it.lose }
        userSeason.cupWonProlongation = results.count { it.winOvertime }
        userSeason.cupLoseProlongation = results.count { it.loseOvertime }
        userSeason.cupWonTab = results.count { it.winTab }
        userSeason.cupLoseTab = results.count { it.loseTab }
    }

    private fun updateUserLeagueRanking(season: Season, user: User) {

        val userSeason = userSeasonRepo.findBySeasonAndUser(season, user)

        if (userSeason != null) {
            val results = GameUserResultSearchBean(season = season.id, compet = Compet.LEAGUE, user = user.id, status = MatchStatus.PLAYED)
                    .find(jpqlQueryFactory)
            userSeason.draw = results.count { it.draw }
            userSeason.won = results.count { it.win }
            userSeason.lose = results.count { it.lose }
            userSeason.goalFor = results.sumBy { it.goalsFor }
            userSeason.goalAgainst = results.sumBy { it.goalsAgainst }
            userSeason.goalAverage = results.sumBy { it.goalAverage }
            userSeason.points = userSeason.won * 3 + userSeason.draw
            userSeasonRepo.save(userSeason)

        }

    }

}
