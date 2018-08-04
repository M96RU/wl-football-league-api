package wlceligue.admin.webapp.security.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;

/**
 * Created by a131199 on 24/03/2016.
 */
@Configuration
@EnableConfigurationProperties(GuWebSecurityProperties.class)
public class GuWebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired(required = false)
    private UserDetailsService userDetailsService;

    @Inject
    private PasswordEncoder passwordEncoder;

    @Inject
    private List<GuWebSecurityConfigurer> configurers;

    @Inject
    private GuWebSecurityProperties guWebSecurityProperties;

    @PostConstruct
    public void init() {
        configurers.sort(AnnotationAwareOrderComparator.INSTANCE);
    }

    @Override
    public final void configure(WebSecurity web) throws Exception {
        guWebSecurityProperties.getIgnoredPaths().forEach(ignoredPath -> web.ignoring().antMatchers(ignoredPath));
    }

    @Override
    protected final UserDetailsService userDetailsService() {
        if (userDetailsService != null) {
            return userDetailsService;
        }
        else {
            return super.userDetailsService();
        }
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.logout().logoutUrl(guWebSecurityProperties.getLogoutUrl()).logoutSuccessHandler((httpServletRequest, httpServletResponse, authentication) -> httpServletResponse.setStatus(HttpServletResponse.SC_OK));
        if (guWebSecurityProperties.getUsernamePassword().isEnabled()) {
            http.formLogin()
                    .loginPage(guWebSecurityProperties.getUsernamePassword().getLoginPage())
                    .defaultSuccessUrl(guWebSecurityProperties.getDefaultSuccessUrl(), guWebSecurityProperties.isAlwaysUseDefaultSuccessUrl());
        }
        for (GuWebSecurityConfigurer configurer : configurers) {
            configurer.doExtraConfig(http, this);
        }
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        if (userDetailsService != null) {
            auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
        }
        auth.authenticationEventPublisher(eventPublisher());
        for (GuWebSecurityConfigurer configurer : configurers) {
            configurer.doExtraConfig(auth, this);
        }
    }

    @Bean
    public AuthenticationEventPublisher eventPublisher() {
        return new DefaultAuthenticationEventPublisher();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
