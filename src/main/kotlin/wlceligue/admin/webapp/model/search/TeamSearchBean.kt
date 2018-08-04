package wlceligue.admin.webapp.model.search

import com.querydsl.jpa.JPQLQuery
import lombok.Data
import wlceligue.admin.webapp.model.jpa.*
import java.time.Instant

@Data
class TeamSearchBean : AbstractSearchBean<Team, QTeam>(QTeam.team) {

    override fun doContribute(query: JPQLQuery<Team>, queryEntity: QTeam) {

    }
}
