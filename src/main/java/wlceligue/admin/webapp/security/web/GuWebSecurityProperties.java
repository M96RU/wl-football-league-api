package wlceligue.admin.webapp.security.web;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "gu.web")
@Getter
@Setter
public class GuWebSecurityProperties {

    private List<String> ignoredPaths;

    private CasConfiguration cas = new CasConfiguration();

    private UsernamePasswordConfiguration usernamePassword = new UsernamePasswordConfiguration();

    private BasicConfiguration basic = new BasicConfiguration();

    private String defaultSuccessUrl = "/";

    private String logoutUrl = "/api/logout";

    private boolean alwaysUseDefaultSuccessUrl;

    @Getter
    @Setter
    public static class CasConfiguration {

        private boolean enabled;

        private String siteUrl;

        private String casBaseUrl;

        private String casLoginPath;

        private boolean sendRenew;

        private boolean autoCreateUsers;

        private String supportedConnector;

    }

    @Getter
    @Setter
    public static class UsernamePasswordConfiguration {

        private boolean enabled;

        private String loginPage = "/login";

    }

    @Getter
    @Setter
    public static class BasicConfiguration {

        private boolean enabled;

    }

}
