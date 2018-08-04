package wlceligue.admin.webapp

import com.querydsl.jpa.JPQLQueryFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import wlceligue.admin.webapp.config.JPQLQueryFactoryFactoryBean
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import wlceligue.admin.webapp.config.AppConfiguration


@SpringBootApplication
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
@EnableConfigurationProperties(AppConfiguration::class)
open class Application {

    @Bean
    @ConditionalOnMissingBean(JPQLQueryFactory::class)
    open fun jpqlQueryFactory(): JPQLQueryFactoryFactoryBean {
        return JPQLQueryFactoryFactoryBean()
    }

    @Configuration
    open class AllResources : WebMvcConfigurer {

        override fun configurePathMatch(matcher: PathMatchConfigurer) {
            matcher.isUseRegisteredSuffixPatternMatch = true
        }

    }


}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
