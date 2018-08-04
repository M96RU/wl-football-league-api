package wlceligue.admin.webapp.config

import com.querydsl.jpa.JPQLQueryFactory
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.beans.factory.FactoryBean
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

class JPQLQueryFactoryFactoryBean : FactoryBean<JPQLQueryFactory> {

    @PersistenceContext
    lateinit var entityManager: EntityManager

    @Throws(Exception::class)
    override fun getObject(): JPQLQueryFactory {
        return JPAQueryFactory(entityManager)
    }

    override fun getObjectType(): Class<*> {
        return JPQLQueryFactory::class.java
    }

    override fun isSingleton(): Boolean {
        return true
    }
}
