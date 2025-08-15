package com.example.shiftcraft.web.security;

import com.example.shiftcraft.persistence.entity.Role;
import com.example.shiftcraft.persistence.entity.User;
import com.example.shiftcraft.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new UsernameNotFoundException("User account is not active: " + email);
        }
        
        return new CustomUserPrincipal(user);
    }
    
    /**
     * Custom UserDetails implementation that wraps our User entity
     */
    public static class CustomUserPrincipal implements UserDetails {
        
        private final User user;
        
        public CustomUserPrincipal(User user) {
            this.user = user;
        }
        
        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());
        }
        
        @Override
        public String getPassword() {
            return user.getPasswordHash();
        }
        
        @Override
        public String getUsername() {
            return user.getEmail();
        }
        
        @Override
        public boolean isAccountNonExpired() {
            return true;
        }
        
        @Override
        public boolean isAccountNonLocked() {
            return user.getStatus() != User.UserStatus.SUSPENDED;
        }
        
        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }
        
        @Override
        public boolean isEnabled() {
            return user.getStatus() == User.UserStatus.ACTIVE;
        }
        
        // Additional methods to access user information
        public User getUser() {
            return user;
        }
        
        public String getUserId() {
            return user.getId();
        }
        
        public String getFullName() {
            return user.getFullName();
        }
        
        public boolean hasRole(String roleName) {
            return user.getRoles().stream()
                .map(Role::getName)
                .anyMatch(roleName::equals);
        }
    }
}