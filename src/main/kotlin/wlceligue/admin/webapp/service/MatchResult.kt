package wlceligue.admin.webapp.service

import java.time.Instant
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

data class MatchResult(
        var cancel: Boolean = false,

        var date: Instant? = null,

        @Min(0)
        @Max(20)
        var score1: Int? = null,

        @Min(0)
        @Max(20)
        var score2: Int? = null,

        var prolongation1: Int? = null,

        var prolongation2: Int? = null,

        var tab1: Int? = null,

        var tab2: Int? = null

)