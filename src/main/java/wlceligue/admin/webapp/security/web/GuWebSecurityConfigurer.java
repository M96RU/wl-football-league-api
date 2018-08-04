package wlceligue.admin.webapp.security.web;

import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Interface to be implemented by a custom bean to customize web security context (like adding new intercept url rules)
 */
public interface GuWebSecurityConfigurer {

    default void doExtraConfig(AuthenticationManagerBuilder auth, WebSecurityConfigurerAdapter webSecurityConfigurerAdapter) throws Exception {}

    default void doExtraConfig(HttpSecurity http, WebSecurityConfigurerAdapter webSecurityConfigurerAdapter) throws Exception {}

}
