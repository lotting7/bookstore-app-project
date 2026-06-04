// ten plik to lacznik miedzy baza danych a spring security. pobiera dane uzytkownika i role do logowania.
package com.bookstore.service;

import com.bookstore.model.User;
import com.bookstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository; // repozytorium uzytkownikow

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // szukanie uzytkownika w bazie po loginie
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("nie znaleziono uzytkownika: " + username));

        // tlumaczenie naszego uzytkownika na format spring security
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .disabled(!user.isEnabled())
                .authorities(user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName()))
                        .collect(Collectors.toList())) // mapowanie ról na uprawnienia
                .build();
    }
}
