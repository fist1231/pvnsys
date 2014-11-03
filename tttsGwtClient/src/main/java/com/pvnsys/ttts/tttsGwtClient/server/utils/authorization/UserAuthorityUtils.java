package com.pvnsys.ttts.tttsGwtClient.server.utils.authorization;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import com.pvnsys.ttts.tttsGwtClient.shared.authorization.User;
	
public final class UserAuthorityUtils {
    private static final List<GrantedAuthority> ADMIN_ROLES = AuthorityUtils.createAuthorityList("PVN_ADMIN", "PVN_USER");
    private static final List<GrantedAuthority> USER_ROLES = AuthorityUtils.createAuthorityList("PVN_USER");

    public static Collection<? extends GrantedAuthority> createAuthorities(User user) {
        String username = user.getUsername();
        if (username.startsWith("pvn")) {
            return ADMIN_ROLES;
        }
        return USER_ROLES;
    }

    private UserAuthorityUtils() {
    }
}
