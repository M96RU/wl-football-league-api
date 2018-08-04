package wlceligue.admin.webapp.security.web.basic;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import wlceligue.admin.webapp.security.web.GuWebSecurityConfigurer;
import wlceligue.admin.webapp.security.web.GuWebSecurityProperties;

/**
 * Created by a131199 on 31/03/2016.
 */
@Configuration
@ConditionalOnProperty(prefix = "gu.web.basic", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(GuWebSecurityProperties.class)
public class BasicConfigurer implements GuWebSecurityConfigurer {

    @Value("${gu.web.basic.order:0}")
    private int order;

    @Override
    public void doExtraConfig(HttpSecurity http, WebSecurityConfigurerAdapter webSecurityConfigurerAdapter) throws Exception {
        http.httpBasic();
    }

}
