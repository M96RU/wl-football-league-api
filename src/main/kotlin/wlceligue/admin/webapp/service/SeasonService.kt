package wlceligue.admin.webapp.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import wlceligue.admin.webapp.model.jpa.Season
import wlceligue.admin.webapp.model.json.SeasonCreate
import wlceligue.admin.webapp.model.repository.SeasonRepo

@Service
open class SeasonService(val userSeasonService: UserSeasonService,
                         val teamSeasonService: TeamSeasonService,
                         val matchGenerationService: MatchGenerationService,
                         val seasonRepo: SeasonRepo) {

    @Transactional
    open fun create(seasonCreate: SeasonCreate): Season {

        require(!seasonCreate.label.isNullOrBlank()) { "Season label missing" }

        require(seasonCreate.users.size > 0) { "Empty user list" }

        val previousSeason = seasonRepo.findById(seasonCreate.previous).orElseThrow { IllegalArgumentException("No previous season found with id ${seasonCreate.previous}") }

        val season = createSeason(seasonCreate.label)

        teamSeasonService.initTeamSeason(season, previousSeason)
        userSeasonService.createUserSeasons(season, seasonCreate.users)

        for (division in 1..season.division) {
            matchGenerationService.generate(season, division)
        }

        matchGenerationService.generateCup(season)

        return seasonRepo.save(season)
    }

    private fun createSeason(label: String): Season {
        val season = Season()
        season.label = label
        season.division = 0
        season.cup = 0
        return seasonRepo.save(season)
    }


}
