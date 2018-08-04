package wlceligue.admin.webapp.model.search

import com.querydsl.core.QueryResults
import com.querydsl.core.types.EntityPath
import com.querydsl.core.types.Expression
import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.PathBuilderFactory
import com.querydsl.jpa.JPQLQuery
import com.querydsl.jpa.JPQLQueryFactory
import mu.KLoggable
import mu.KLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import kotlin.system.measureTimeMillis

abstract class AbstractSearchBean<E, in Q : EntityPath<E>> protected constructor(private val defaultQueryEntity: Q) {

    fun prepare(queryFactory: JPQLQueryFactory, pageable: Pageable? = null, queryEntity: Q = defaultQueryEntity): JPQLQuery<E> {
        val query = queryFactory.select(queryEntity).from(queryEntity)
        contribute(query, queryEntity, pageable)
        return query
    }

    fun find(queryFactory: JPQLQueryFactory, pageable: Pageable? = null, queryEntity: Q = defaultQueryEntity, modifier: ((JPQLQuery<E>) -> Unit)? = {}): Page<E> {
        val query = prepare(queryFactory, pageable, queryEntity)
        modifier?.invoke(query)
        var queryResults: QueryResults<E>? = null
        val time = measureTimeMillis { queryResults = query.fetchResults() }
        logger.debug { "Query execution time : $time, ${toString()}" }
        if (pageable != null) {
            return PageImpl(queryResults!!.results, pageable, queryResults!!.total)
        } else {
            return PageImpl(queryResults!!.results)
        }
    }

    fun contribute(query: JPQLQuery<E>, queryEntity: Q, pageable: Pageable?) {
        if (pageable != null) {
            query.offset(pageable.offset)
            query.limit(pageable.pageSize.toLong())

            if (pageable.sort != null) {
                val builder = FACTORY.create(queryEntity.type)
                for (order in pageable.sort) {
                    query.orderBy(OrderSpecifier(if (order.isAscending) Order.ASC else Order.DESC, builder.get(order.property) as Expression<Comparable<Any>>, OrderSpecifier.NullHandling.NullsLast))
                }
            }
        }
        doContribute(query, queryEntity)
    }

    protected abstract fun doContribute(query: JPQLQuery<E>, queryEntity: Q)

    companion object : Any(), KLoggable {
        private val FACTORY = PathBuilderFactory()
        override val logger = logger()
    }
}
