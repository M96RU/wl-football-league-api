package wlceligue.admin.webapp.model.json

data class SeasonCreate(

        var previous: Int? = null,

        var label: String = "",

        var users: List<SeasonUserEntry> = emptyList()
)