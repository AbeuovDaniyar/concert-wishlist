package com.teamrhythm.concertwishlist.service;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.teamrhythm.concertwishlist.entity.User;
import com.teamrhythm.concertwishlist.repository.UserRepository;
import com.teamrhythm.concertwishlist.security.CustomOAuth2User;
import com.teamrhythm.concertwishlist.security.CustomUserDetails;

@Service
public class AuthenticatedUserService {

    private final UserRepository userRepository;

    public AuthenticatedUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Long requireUserId(Authentication authentication) {
        return findUser(authentication)
                .map(User::getId)
                .orElseThrow(() -> new IllegalStateException("User must be authenticated"));
    }

    public Optional<User> findUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails customUserDetails) {
            Long id = customUserDetails.getId();
            if (id != null) {
                return userRepository.findById(id);
            }
            return userRepository.findByUsername(customUserDetails.getUsername());
        }

        if (principal instanceof User user) {
            if (user.getId() != null) {
                return userRepository.findById(user.getId());
            }
            return findByIdentifier(user.getUsername());
        }

        if (principal instanceof UserDetails userDetails) {
            return findByIdentifier(userDetails.getUsername());
        }

        if (principal instanceof OAuth2User oauth2User) {
            Optional<User> userOptional = resolveOAuth2User(oauth2User);
            if (userOptional.isPresent()) {
                return userOptional;
            }
        }

        return findByIdentifier(authentication.getName());
    }

    private Optional<User> findByIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return Optional.empty();
        }
        return userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .or(() -> userRepository.findBySpotifyUserId(identifier));
    }

    private Optional<User> resolveOAuth2User(OAuth2User oauth2User) {
        // Check if it's our CustomOAuth2User
        if (oauth2User instanceof CustomOAuth2User customOAuth2User) {
            return Optional.of(customOAuth2User.getUser());
        }

        // Fallback to existing logic
        String spotifyId = oauth2User.getAttribute("id");
        if (spotifyId != null) {
            Optional<User> user = userRepository.findBySpotifyUserId(spotifyId);
            if (user.isPresent()) {
                return user;
            }
        }

        String email = oauth2User.getAttribute("email");
        if (email != null) {
            Optional<User> user = userRepository.findByEmail(email);
            if (user.isPresent()) {
                return user;
            }
        }

        String username = oauth2User.getAttribute("username");
        if (username == null) {
            username = oauth2User.getAttribute("login");
        }
        if (username == null) {
            username = oauth2User.getAttribute("display_name");
        }
        if (username != null) {
            Optional<User> user = userRepository.findByUsername(username);
            if (user.isPresent()) {
                return user;
            }
        }

        return Optional.empty();
    }
}
