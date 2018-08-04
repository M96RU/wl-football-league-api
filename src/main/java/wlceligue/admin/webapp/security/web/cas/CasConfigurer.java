package wlceligue.admin.webapp.security.web.cas;

import lombok.Getter;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import wlceligue.admin.webapp.security.web.GuWebSecurityConfigurer;
import wlceligue.admin.webapp.security.web.GuWebSecurityProperties;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Configurer for Cas integration
 */
@Configuration
@ConditionalOnProperty(prefix = "gu.web.cas", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(GuWebSecurityProperties.class)
public class CasConfigurer implements GuWebSecurityConfigurer, Ordered {

    @Inject
    private UserDetailsService userDetailsService;

    @Inject
    private GuWebSecurityProperties guWebSecurityProperties;

    @Value("${gu.web.cas.order:0}")
    @Getter
    private int order;

    @Bean
    public ServiceProperties serviceProperties() {
        ServiceProperties serviceProperties = new ServiceProperties();
        serviceProperties.setService(guWebSecurityProperties.getCas().getSiteUrl() + "/api/login/cas");
        serviceProperties.setSendRenew(guWebSecurityProperties.getCas().isSendRenew());
        return serviceProperties;
    }

    @Bean
    public CasAuthenticationProvider casAuthenticationProvider() {
        CasAuthenticationProvider casAuthenticationProvider = new CasAuthenticationProvider();
        casAuthenticationProvider.setAuthenticationUserDetailsService(authenticationUserDetailsService());
        casAuthenticationProvider.setServiceProperties(serviceProperties());
        casAuthenticationProvider.setTicketValidator(cas20ServiceTicketValidator());
        casAuthenticationProvider.setKey("wl-football-league-cas");
        return casAuthenticationProvider;
    }

    @Bean
    public AuthenticationUserDetailsService<CasAssertionAuthenticationToken> authenticationUserDetailsService() {
        return new CasAuthenticationUserDetailsService(userDetailsService);
    }

    @Bean
    public Cas20ServiceTicketValidator cas20ServiceTicketValidator() {
        return new Cas20ServiceTicketValidator(guWebSecurityProperties.getCas().getCasBaseUrl());
    }

    public CasAuthenticationFilter casAuthenticationFilter(AuthenticationManager authenticationManager) throws Exception {
        CasAuthenticationFilter casAuthenticationFilter = new CasAuthenticationFilter();
        casAuthenticationFilter.setFilterProcessesUrl("/api/login/cas");
        casAuthenticationFilter.setAuthenticationManager(authenticationManager);
        casAuthenticationFilter.setAuthenticationSuccessHandler(new SimpleUrlAuthenticationSuccessHandler("/loginSuccess"));
        return casAuthenticationFilter;
    }

    @Bean
    public CasAuthenticationEntryPoint casAuthenticationEntryPoint() {
        CasAuthenticationEntryPoint casAuthenticationEntryPoint = new CasAuthenticationEntryPoint();
        casAuthenticationEntryPoint.setLoginUrl(guWebSecurityProperties.getCas().getCasBaseUrl() + guWebSecurityProperties.getCas().getCasLoginPath());
        casAuthenticationEntryPoint.setServiceProperties(serviceProperties());
        return casAuthenticationEntryPoint;
    }

    @Override
    public void doExtraConfig(AuthenticationManagerBuilder auth, WebSecurityConfigurerAdapter webSecurityConfigurerAdapter) throws Exception {
        auth.authenticationProvider(casAuthenticationProvider());
    }

    @Override
    public void doExtraConfig(HttpSecurity http, WebSecurityConfigurerAdapter webSecurityConfigurerAdapter) throws Exception {
        http.addFilter(casAuthenticationFilter(webSecurityConfigurerAdapter.authenticationManagerBean()));
        http.exceptionHandling().authenticationEntryPoint(new Http401AuthenticationEntryPoint(casAuthenticationEntryPoint().getLoginUrl()));
    }

    private static class Http401AuthenticationEntryPoint implements AuthenticationEntryPoint {

        private final String headerValue;

        public Http401AuthenticationEntryPoint(String headerValue) {
            this.headerValue = headerValue;
        }

        @Override
        public void commence(HttpServletRequest request, HttpServletResponse response,
                             AuthenticationException authException) throws IOException, ServletException {
            response.setHeader("WWW-Authenticate", this.headerValue);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    authException.getMessage());
        }

    }
}

