package wlceligue.admin.webapp.service

import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.DateTime
import net.fortuna.ical4j.model.Dur
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.property.*
import wlceligue.admin.webapp.config.AppConfiguration
import wlceligue.admin.webapp.enums.Compet
import wlceligue.admin.webapp.enums.MatchStatus
import wlceligue.admin.webapp.model.jpa.Match
import wlceligue.admin.webapp.model.repository.UserRepo
import wlceligue.admin.webapp.model.search.GameSearchBean
import java.time.Instant
import javax.inject.Named

/**
 * Created by a131199 on 3/9/17.
 */
@Named
open class CalendarService(val userRepo: UserRepo,
                           val appConfiguration: AppConfiguration) {

    open fun generateCalendar(games: Iterable<Match>, calendarName: String = "", originDate: Instant? = null): Calendar {
        val calendar = Calendar()
        if (calendarName.isNotBlank()) {
            calendar.properties += XProperty("X-WR-CALNAME", calendarName)
            calendar.properties += XProperty("X-PUBLISHED-TTL", "PT1H")
        }
        calendar.properties += ProdId("-//Worldline//Football League//FR")
        calendar.properties += if (originDate != null) Method.CANCEL else Method.PUBLISH
        calendar.properties += Version.VERSION_2_0
        calendar.properties += CalScale.GREGORIAN
        games.forEach {
            val event = if (originDate != null) VEvent(DateTime(originDate.toEpochMilli()), Dur("20M"), gameSummary(it)) else VEvent(DateTime(it.date.toEpochMilli()), Dur("20M"), gameSummary(it))
            if (it.status == MatchStatus.PLAYED) event.properties += Description(gameDescription(it))
            event.properties += Uid("${it.id}@${appConfiguration.calendarDomain}")
            event.properties += Sequence(it.version)
            event.properties += Organizer("MAILTO:${appConfiguration.emailFrom}")
            calendar.components += event
        }
        return calendar
    }

    open fun calendarName(gameSearchBean: GameSearchBean): String {
        var name = "FIFA"
        gameSearchBean.user?.let {
            name += userRepo.findById(it).orElseThrow { IllegalArgumentException("No user found with id $it") }.run { " - $firstname $lastname" }
        }
        return name
    }

    open fun gameSummary(game: Match): String {
        val compet = when (game.compet) {
            Compet.LEAGUE -> "D${game.round}/J${game.day}"
            Compet.CUP -> "Coupe/" + cupRoundName(game)
            null -> "Non défini"
        }
        return "[$compet] ${game.user1.firstname} ${game.user1.lastname} (${game.team1.label}) - ${game.user2.firstname} ${game.user2.lastname} (${game.team2.label})"
    }

    open fun gameDescription(game: Match): String {
        if (game.status == MatchStatus.PLAYED) {
            var description = "Temps réglementaire: ${game.score1} - ${game.score2}"
            if (game.prolongation1 != null) description += "\nProlongation: ${game.prolongation1} - ${game.prolongation2}"
            if (game.tab1 != null) description += "\nTir aux buts: ${game.tab1} - ${game.tab2}"
            return description
        } else {
            return "Ne pas refuser cette invitation, il faut passer par le site pour annuler/reprogrammer.\n${appConfiguration.siteUrl}"
        }
    }

    private fun cupRoundName(game: Match): String {
        val cupRoundFromFinal = game.season.cup - game.round
        return when (cupRoundFromFinal) {
            0 -> "Finale"
            1 -> "Demi-finales"
            2 -> "Quart de finales"
            else -> if (game.round == 1) "1er tour" else "${game.round}ème tour"
        }
    }

}