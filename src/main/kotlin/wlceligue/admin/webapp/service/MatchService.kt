package wlceligue.admin.webapp.service

import com.querydsl.jpa.JPQLQueryFactory
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newFixedThreadPoolContext
import mu.KLoggable
import net.fortuna.ical4j.data.CalendarOutputter
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import wlceligue.admin.webapp.config.AppConfiguration
import wlceligue.admin.webapp.enums.Compet
import wlceligue.admin.webapp.enums.GameOutcome
import wlceligue.admin.webapp.enums.MatchStatus
import wlceligue.admin.webapp.model.GameUserResultBuilder
import wlceligue.admin.webapp.model.jpa.*
import wlceligue.admin.webapp.model.repository.GameUserResultRepo
import wlceligue.admin.webapp.model.repository.MatchRepo
import wlceligue.admin.webapp.model.search.AbstractSearchBean
import java.io.ByteArrayOutputStream
import javax.activation.DataHandler
import javax.annotation.PostConstruct
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMultipart
import javax.mail.util.ByteArrayDataSource
import kotlin.coroutines.experimental.CoroutineContext


@Service
open class MatchService(val matchRepo: MatchRepo,
                        val jpqlQueryFactory: JPQLQueryFactory,
                        val userSeasonService: UserSeasonService,
                        val gameUserResultRepo: GameUserResultRepo,
                        val batonService: BatonService,
                        val javaMailSender: JavaMailSender,
                        val calendarService: CalendarService,
                        val appConfiguration: AppConfiguration,
                        val template: SimpMessagingTemplate) {

    companion object : Any(), KLoggable {
        override val logger = AbstractSearchBean.logger()
    }

    private lateinit var mailPool: CoroutineContext

    @PostConstruct
    open fun init() {
        logger.info { appConfiguration }
        this.mailPool = newFixedThreadPoolContext(4, "MailPool")
    }

    @Transactional
    open fun updateGame(game: Match, gameResult: MatchResult) {
        val initialDate = if (gameResult.date == null) game.date else null
        val initialStatus = game.status

        if (gameResult.score1 != null || gameResult.score2 != null) {
            closeMatch(game, gameResult)
            updateNextRound(game)

            userSeasonService.updateUserSeason(game)
        } else {
            game.status = if (gameResult.date != null) MatchStatus.PLANNED else if (gameResult.cancel) MatchStatus.CANCELLED else MatchStatus.INIT
            game.score1 = null
            game.score2 = null
            game.prolongation1 = null
            game.prolongation2 = null
            game.tab1 = null
            game.tab2 = null
            game.date = gameResult.date
            matchRepo.save(game)
            updateGameUserResults(game, true)
            if (initialStatus == MatchStatus.PLAYED) {
                userSeasonService.updateUserSeason(game)
            }
        }

        if (game.date != null || initialDate != null) {

            val emails = jpqlQueryFactory.select(QUser.user.email).from(QUser.user).where(QUser.user.`in`(game.user1, game.user2).and(QUser.user.email.isNotNull)).fetch()

            if (appConfiguration.emailDestOverride.isNotBlank()) {
                logger.info { "Overriding emails address $emails to ${appConfiguration.emailDestOverride}" }
                emails.clear()
                emails.add(appConfiguration.emailDestOverride)
            }

            launch(mailPool) {
                logger.info { "Preparing invitation email for game ${game.id}" }
                val calendar = calendarService.generateCalendar(arrayListOf(game), originDate = initialDate)
                val message = javaMailSender.createMimeMessage()
                val helper = MimeMessageHelper(message)
                helper.setFrom(appConfiguration.emailFrom)
                helper.setTo(emails.toTypedArray())
                helper.setSubject(calendarService.gameSummary(game))

                message.setHeader("Content-class", "urn:content-classes:calendarmessage")

                // Create a Multipart
                val multipart = MimeMultipart("alternative")
                message.setContent(multipart)

                // Create the content part
                val contentPart = MimeBodyPart()
                multipart.addBodyPart(contentPart)
                contentPart.setText(calendarService.gameDescription(game), "utf-8")

                // Create the invite part
                val invitePart = MimeBodyPart()
                multipart.addBodyPart(invitePart)

                // Fill the message
                val baos = ByteArrayOutputStream()
                CalendarOutputter(false).output(calendar, baos)
                invitePart.dataHandler = DataHandler(ByteArrayDataSource(baos.toByteArray(), "text/calendar;name=\"meeting.ics\";method=${calendar.method.value};charset=utf-8"))
                invitePart.setHeader("Content-Transfer-Encoding", "8bit")

                logger.info { "Sending emails to $emails with subject ${message.subject}" }

                // Send email
                logger.debug {
                    val mailDumpBuffer = ByteArrayOutputStream()
                    message.writeTo(mailDumpBuffer)
                    String(mailDumpBuffer.toByteArray())
                }

                javaMailSender.send(message)
            }
        }
        template.convertAndSend("/topic/game", game)
    }

    @Transactional
    open fun updateGameUserResults(game: Match, clearFirst: Boolean = true) {
        if (clearFirst) {
            // Delete old results
            jpqlQueryFactory.delete(QGameUserResult.gameUserResult).where(QGameUserResult.gameUserResult.game.eq(game)).execute()
        }
        if (game.status == MatchStatus.PLAYED) {
            for (user in arrayOf(game.user1, game.user2)) {
                val result = GameUserResultBuilder(game, user).build()
                gameUserResultRepo.save(result)
            }
        }
    }

    private fun closeMatch(match: Match, matchResult: MatchResult): Match {

        requireNotNull(matchResult.date, { "Date is mandatory if a score is provided" })
        require(matchResult.score1 != null && matchResult.score2 != null, { "score1 and score2 are mandatory" })

        val firstDate = arrayOf(match.date, matchResult.date).filterNotNull().min()

        if (matchResult.score1 != matchResult.score2) {
            require(matchResult.prolongation1 == null && matchResult.prolongation2 == null, { "No prolongation score allowed if score1 != score2" })
        }

        if (match.compet == Compet.LEAGUE) {
            require(matchResult.prolongation1 == null && matchResult.prolongation2 == null
                    && matchResult.tab1 == null && matchResult.tab2 == null,
                    { "Only score1 and score2 can be provided for LEAGUE matchs" })
        }

        if (match.compet == Compet.CUP && matchResult.score1 == matchResult.score2) {
            require(matchResult.prolongation1 != null && matchResult.prolongation2 != null, { "Prolongation score are mandatory for cup in case of a draw" })
            if (matchResult.prolongation1 == matchResult.prolongation2) {
                require(matchResult.tab1 != null && matchResult.tab2 != null, { "Tab score are mandatory for if prolongation is a draw" })
                require(matchResult.tab1 != matchResult.tab2, { "Tab score must be different" })
            }
        }

        match.status = MatchStatus.PLAYED
        match.score1 = matchResult.score1
        match.score2 = matchResult.score2
        match.date = matchResult.date
        match.prolongation1 = matchResult.prolongation1
        match.prolongation2 = matchResult.prolongation2
        match.tab1 = matchResult.tab1
        match.tab2 = matchResult.tab2

        val updated = matchRepo.save(match)
        updateGameUserResults(match)

        batonService.rebuildBaton(firstDate)

        return updated

    }

    private fun updateNextRound(match: Match) {

        if (Compet.CUP == match.compet) {

            // next round
            val nextRound = 1 + match.round
            // next game in the round
            val nextDay = (1 + match.day) / 2

            // retrieve the next match
            val matches = matchRepo.findBySeasonAndCompetAndRoundAndDay(match.season, match.compet, nextRound, nextDay)

            if (matches.isEmpty()) {
                return
            }

            if (matches.size > 1) {
                throw RuntimeException("Ambigous matches, too many result for " + match.compet + ", round " + nextRound + ", day " + nextDay)
            }

            // 1 is the local team, 0 is the away team
            val reste = match.day % 2

            val nextMatch = matches[0]

            val result = GameUserResultBuilder(match, match.user1).build()

            if (result.outcome == GameOutcome.DRAW) {
                throw RuntimeException("Cannot determine the winner for game ${match.id}")
            }

            val winnerUser = if (result.outcome == GameOutcome.WIN) match.user1 else match.user2
            val winnerTeam = if (result.outcome == GameOutcome.WIN) match.team1 else match.team2

            if (reste > 0) {
                // local team
                nextMatch.user1 = winnerUser
                nextMatch.team1 = winnerTeam
            } else {
                // away team
                nextMatch.user2 = winnerUser
                nextMatch.team2 = winnerTeam
            }

            if (nextMatch.user1 != null && nextMatch.user2 != null) {
                nextMatch.status = MatchStatus.INIT
            }

            matchRepo.save(nextMatch)
        }

    }

    @Transactional
    open fun updateUserTeam(userSeason: UserSeason) {
        val matches = jpqlQueryFactory.selectFrom(QMatch.match)
                .where(QMatch.match.season.id.eq(userSeason.season.id).and(
                        QMatch.match.user1.id.eq(userSeason.user.id)
                                .or(QMatch.match.user2.id.eq(userSeason.user.id))
                )).fetch()

        matches?.forEach {
            if (it.user1.equals(userSeason.user)) {
                it.team1 = userSeason.team
            } else if (it.user2.equals(userSeason.user)) {
                it.team2 = userSeason.team
            }
        }

        matchRepo.saveAll(matches)
    }

}
