package wlceligue.admin.webapp.service

import com.querydsl.jpa.JPQLQueryFactory
import org.springframework.transaction.support.TransactionTemplate
import wlceligue.admin.webapp.enums.MatchStatus
import wlceligue.admin.webapp.model.jpa.*
import wlceligue.admin.webapp.model.repository.BatonRepo
import wlceligue.admin.webapp.model.repository.UserSeasonRepo
import javax.annotation.PostConstruct
import javax.inject.Named

@Named
open class MigrationService(val jpqlQueryFactory: JPQLQueryFactory, val matchService: MatchService, val userSeasonService: UserSeasonService,
                            val transactionTemplate: TransactionTemplate, val userSeasonRepo: UserSeasonRepo, val batonRepo: BatonRepo,
                            val batonService: BatonService) {

    @PostConstruct
    open fun init() {
        if (jpqlQueryFactory.selectFrom(QGameUserResult.gameUserResult).fetchCount() == 0L) {

            transactionTemplate.execute {
                // Populate GameUserResult table
                jpqlQueryFactory.selectFrom(QMatch.match).where(QMatch.match.status.eq(MatchStatus.PLAYED)).fetch().forEach {
                    matchService.updateGameUserResults(it, false)
                }

                // Update userSeason new fields
                jpqlQueryFactory.selectFrom(QSeason.season).fetch().forEach {
                    for (division in 1..it.division) {
                        val userSeasons = userSeasonService.computeDivisionRanking(it, division)
                        userSeasons.forEach { userSeasonService.computeCup(it) }
                        userSeasonRepo.saveAll(userSeasons)
                    }
                }

                // Rebuild baton since first games
                batonService.rebuildBaton()
            }
        }

    }

}