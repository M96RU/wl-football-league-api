package wlceligue.admin.webapp.service.team

import javax.validation.constraints.Max
import javax.validation.constraints.Min

data class TeamSeasonUpdate(

        var division: Int? = null,

        @Min(1)
        @Max(99)
        var attack: Int? = null,

        @Min(1)
        @Max(99)
        var midfield: Int? = null,

        @Min(1)
        @Max(99)
        var defence: Int? = null
)