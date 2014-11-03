package com.pvnsys.ttts.tttsGwtClient.server.authentication;

import java.util.Collection;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.pvnsys.ttts.tttsGwtClient.server.utils.authorization.UserAuthorityUtils;
import com.pvnsys.ttts.tttsGwtClient.shared.authorization.User;

/**
 * A Spring Security {@link AuthenticationProvider} that uses our {@link CalendarService} for authentication. Compare
 * this to our {@link CalendarUserDetailsService} which is called by Spring Security's {@link DaoAuthenticationProvider}.
 *
 * @author Rob Winch
 * @see CalendarUserDetailsService
 */
@Component
public class UserAuthenticationProvider implements AuthenticationProvider {
//    private final CalendarService calendarService;

//    public UserAuthenticationProvider(CalendarService calendarService) {
//        if (calendarService == null) {
//            throw new IllegalArgumentException("calendarService cannot be null");
//        }
//        this.calendarService = calendarService;
//    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;
        String email = token.getName();
        User user = email == null ? null : new User(1, "Cracker", "Ritz", "pvn", "wtfwtfwtf");
        if(user == null) {
            throw new UsernameNotFoundException("Invalid username/password");
        }
        String password = user.getPassword();
        if(!user.getUsername().equals(token.getPrincipal()) || !password.equals(token.getCredentials())) {
            throw new BadCredentialsException("Invalid username/password");
        }
        Collection<? extends GrantedAuthority> authorities = UserAuthorityUtils.createAuthorities(user);
        return new UsernamePasswordAuthenticationToken(user, password, authorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.equals(authentication);
    }
}
