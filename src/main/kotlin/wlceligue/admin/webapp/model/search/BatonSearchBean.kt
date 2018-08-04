package wlceligue.admin.webapp.model.search

import com.querydsl.jpa.JPQLQuery
import wlceligue.admin.webapp.enums.Compet
import wlceligue.admin.webapp.enums.MatchStatus
import wlceligue.admin.webapp.model.jpa.Baton
import wlceligue.admin.webapp.model.jpa.Match
import wlceligue.admin.webapp.model.jpa.QBaton
import wlceligue.admin.webapp.model.jpa.QMatch

class BatonSearchBean(var userId: Int? = null) : AbstractSearchBean<Baton, QBaton>(QBaton.baton) {

    override fun doContribute(query: JPQLQuery<Baton>, baton: QBaton) {
        userId?.let { query.where(baton.user.id.eq(it)) }
    }
}

