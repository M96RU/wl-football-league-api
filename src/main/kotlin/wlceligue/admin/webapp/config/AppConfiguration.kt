package wlceligue.admin.webapp.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app")
data class AppConfiguration(var emailDestOverride: String = "",
                            var emailFrom: String = "",
                            var siteUrl: String = "",
                            var calendarDomain: String = "")
