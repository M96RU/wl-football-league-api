package wlceligue.admin.webapp.security.web.cas;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Slf4j
public class CasAuthenticationUserDetailsService implements AuthenticationUserDetailsService<CasAssertionAuthenticationToken> {

    @Value("${gu.web.cas.autoCreateUsers:false}")
    private boolean autoCreateUsers;

    private UserDetailsService delegate;

    public CasAuthenticationUserDetailsService(UserDetailsService delegate) {
        this.delegate = delegate;
    }

    @Override
    public UserDetails loadUserDetails(CasAssertionAuthenticationToken token) throws UsernameNotFoundException {
        String login = token.getName();
        try {
            return delegate.loadUserByUsername(login);
        } catch (UsernameNotFoundException e) {
            if (autoCreateUsers) {
                log.error("TODO : Trying to auto create user {}", login);
//                connectorService.syncUser(connector, login, true);
                return delegate.loadUserByUsername(login);
            }
            throw e;
        }
    }
}
