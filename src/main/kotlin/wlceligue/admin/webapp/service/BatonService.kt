package wlceligue.admin.webapp.service

import com.querydsl.jpa.JPQLQueryFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import wlceligue.admin.webapp.model.jpa.Baton
import wlceligue.admin.webapp.model.jpa.QBaton
import wlceligue.admin.webapp.model.jpa.QGameUserResult
import wlceligue.admin.webapp.model.repository.BatonRepo
import java.time.Instant

/**
 * Created by a131199 on 1/30/17.
 */
@Service
open class BatonService(val jpqlQueryFactory: JPQLQueryFactory, val batonRepo: BatonRepo) {

    open fun getCurrentBaton(): Baton? {
        return jpqlQueryFactory.selectFrom(QBaton.baton).orderBy(QBaton.baton.wonDate.desc()).fetchFirst()
    }

    @Transactional
    open fun rebuildBaton(since: Instant? = null) {
        // Save the first baton date
        val firstForcedBatonDate = jpqlQueryFactory.from(QBaton.baton).where(QBaton.baton.forced.isTrue).select(QBaton.baton.wonDate.min()).fetchFirst()

        // Compute the actual date to use (never before the first baton)
        val actualSince = if (since == null) firstForcedBatonDate else if (since.isAfter(firstForcedBatonDate)) since else firstForcedBatonDate

        // Delete any baton won after provided date (excepted the forced one)
        jpqlQueryFactory.delete(QBaton.baton).where(QBaton.baton.wonDate.goe(actualSince).and(QBaton.baton.forced.isFalse)).execute()

        // Look for the first baton before actual since date
        var currentBaton = jpqlQueryFactory.selectFrom(QBaton.baton).where(QBaton.baton.wonDate.loe(actualSince)).orderBy(QBaton.baton.wonDate.desc()).fetchFirst()

        while (currentBaton != null) {

            val nextForcedBaton = jpqlQueryFactory.selectFrom(QBaton.baton).where(QBaton.baton.wonDate.gt(currentBaton.wonDate).and(QBaton.baton.forced.isTrue)).orderBy(QBaton.baton.wonDate.asc()).fetchFirst()

            // Look for the first lost game for this baton
            val nextLoseQuery = jpqlQueryFactory.selectFrom(QGameUserResult.gameUserResult)
                    .where(QGameUserResult.gameUserResult.user.eq(currentBaton.user))
                    .where(QGameUserResult.gameUserResult.date.goe(currentBaton.wonDate))
            if (nextForcedBaton != null) {
                nextLoseQuery.where(QGameUserResult.gameUserResult.date.lt(nextForcedBaton.wonDate))
            }
            nextLoseQuery.where(QGameUserResult.gameUserResult.loseRegulationTime.isTrue)
            nextLoseQuery.orderBy(QGameUserResult.gameUserResult.date.asc())

            val nextLoseGame = nextLoseQuery.fetchFirst()

            val serieCountQuery = jpqlQueryFactory.selectFrom(QGameUserResult.gameUserResult)
                    .where(QGameUserResult.gameUserResult.user.eq(currentBaton.user))
                    .where(QGameUserResult.gameUserResult.date.goe(currentBaton.wonDate))

            val serieCountLimitDate = arrayOf(nextForcedBaton?.wonDate, nextLoseGame?.date).filterNotNull().min()

            if (serieCountLimitDate != null) {
                serieCountQuery.where(QGameUserResult.gameUserResult.date.lt(serieCountLimitDate))
            }

            if (nextLoseGame != null) {
                currentBaton.lostDate = nextLoseGame.date
                currentBaton.lostGame = nextLoseGame.game
                currentBaton.lostAgainst = nextLoseGame.against
            } else if (nextForcedBaton != null) {
                currentBaton.lostDate = nextForcedBaton.wonDate
                currentBaton.lostGame = null
                currentBaton.lostAgainst = null
            } else {
                currentBaton.lostDate = null
                currentBaton.lostGame = null
                currentBaton.lostAgainst = null
            }

            currentBaton.sinceGames = serieCountQuery.fetchCount()
            batonRepo.save(currentBaton)

            if (nextLoseGame != null) {
                // create a new baton and let the loop fill it and save it
                currentBaton = Baton()
                currentBaton.wonDate = nextLoseGame.date
                currentBaton.wonGame = nextLoseGame.game
                currentBaton.user = nextLoseGame.against
            } else if (nextForcedBaton != null){
                currentBaton = nextForcedBaton
            } else {
                currentBaton = null
            }
        }
    }

}
