package com.streamflix.service;

import com.streamflix.entity.User;
import com.streamflix.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

// Bridges our User entity to Spring Security's UserDetails contract.
@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(u.getUsername())
                .password(u.getPasswordHash())
                .disabled(!Boolean.TRUE.equals(u.getIsActive()))
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name())))
                .build();
    }
}
