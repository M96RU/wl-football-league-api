package wlceligue.admin.webapp.security.web.userdetails;

import com.google.common.collect.Lists;
import com.querydsl.jpa.JPQLQueryFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import wlceligue.admin.webapp.model.jpa.QUser;
import wlceligue.admin.webapp.model.jpa.User;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;

/**
 * UserDetailsService implementation for internal users
 */
@Named
public class UserDetailsServiceImpl implements UserDetailsService {

    @Inject
    private JPQLQueryFactory jpqlQueryFactory;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User persistentUser = jpqlQueryFactory.selectFrom(QUser.user).where(QUser.user.das.equalsIgnoreCase(username)).fetchOne();
        if (persistentUser == null) {
            throw new UsernameNotFoundException(String.format("Username %s not found", username));
        }

        ArrayList<SimpleGrantedAuthority> roles = Lists.newArrayList(new SimpleGrantedAuthority("ROLE_USER"));
        if (persistentUser.admin) {
            roles.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        org.springframework.security.core.userdetails.User springSecurityUserDetails = new org.springframework.security.core.userdetails.User(username, "", roles);

        return springSecurityUserDetails;
    }
}
