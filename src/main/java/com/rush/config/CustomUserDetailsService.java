package com.rush.config;

import com.rush.model.Role;
import com.rush.model.User;
import com.rush.model.UserRole;
import com.rush.repository.UserRepository;
import com.rush.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** This handles lookup to username, password and granted authorities to any given user.
 *
 *   @author Erwin Villagomesa
 */
@Component("userDetailsService")
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;

    @Transactional("rushTransactionManager")
    @Override
    public UserDetails loadUserByUsername(final String login) {
        String lowercaseLogin = login.toLowerCase();
        Optional<User> userFromDatabase =  userRepository.findOneByUsername(lowercaseLogin);
        return userFromDatabase.map(user -> {
            List<UserRole> userRoles = userRoleRepository.findByUser(user);
            List<Role> roles = new ArrayList<>();
            userRoles.stream().forEach(ur-> roles.add(ur.getRole()));
            List<GrantedAuthority> grantedAuthorities = roles.stream()
                    .map(authority -> new SimpleGrantedAuthority(authority.getName()))
                    .collect(Collectors.toList());
            return new org.springframework.security.core.userdetails.User(lowercaseLogin,
                    user.getPassword(),
                    grantedAuthorities);
        }).orElseThrow(() -> new UsernameNotFoundException("User " + lowercaseLogin + " was not found in the database"));
    }
}
