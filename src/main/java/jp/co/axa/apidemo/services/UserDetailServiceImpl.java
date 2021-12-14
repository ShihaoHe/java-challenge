package jp.co.axa.apidemo.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * according to username, retrieve certain user to authenticate and authorize
 *
 * here we don't persist user info, just use in-memory info
 */
@Service
public class UserDetailServiceImpl implements UserDetailsService {

    @Value("${customized.credential.username}")
    private String username;
    @Value("${customized.credential.password}")
    private String password;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        return new User(
                username
                , new BCryptPasswordEncoder().encode(password)
                , AuthorityUtils.commaSeparatedStringToAuthorityList("user")
        );
    }
}
