package wlceligue.admin.webapp.model.search

import com.querydsl.jpa.JPQLQuery
import lombok.Data
import wlceligue.admin.webapp.model.jpa.QSeason
import wlceligue.admin.webapp.model.jpa.Season

@Data
class SeasonSearchBean : AbstractSearchBean<Season, QSeason>(QSeason.season) {

    override fun doContribute(query: JPQLQuery<Season>, queryEntity: QSeason) {

    }

}
