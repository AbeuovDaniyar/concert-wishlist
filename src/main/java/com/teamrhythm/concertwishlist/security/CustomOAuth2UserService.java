package com.teamrhythm.concertwishlist.security;

import com.teamrhythm.concertwishlist.entity.User;
import com.teamrhythm.concertwishlist.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomOAuth2UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("OAuth2 login attempt from provider: {}", registrationId);

        if ("spotify".equals(registrationId)) {
            return processSpotifyUser(oAuth2User);
        }

        return oAuth2User;
    }

    private OAuth2User processSpotifyUser(OAuth2User oAuth2User) {
        String spotifyId = oAuth2User.getAttribute("id");
        String email = oAuth2User.getAttribute("email");
        String displayName = oAuth2User.getAttribute("display_name");
        
        log.info("Processing Spotify user - ID: {}, Email: {}, Display Name: {}", 
                 spotifyId, email, displayName);

        if (spotifyId == null) {
            log.error("Spotify ID is null, cannot process user");
            throw new OAuth2AuthenticationException("Spotify user ID not found");
        }

        // Check if user already exists by Spotify ID
        User user = userRepository.findBySpotifyUserId(spotifyId)
                .orElseGet(() -> {
                    // Check if user exists by email
                    if (email != null) {
                        return userRepository.findByEmail(email).orElse(null);
                    }
                    return null;
                });

        if (user != null) {
            // Update existing user
            log.info("Found existing user with ID: {}", user.getId());
            if (user.getSpotifyUserId() == null) {
                user.setSpotifyUserId(spotifyId);
                log.info("Linked Spotify ID to existing user: {}", user.getId());
            }
            userRepository.save(user);
        } else {
            // Create new user
            log.info("Creating new user from Spotify OAuth");
            user = new User();
            user.setSpotifyUserId(spotifyId);
            user.setEmail(email != null ? email : spotifyId + "@spotify.user");
            
            // Generate username from display name or email
            String username = generateUsername(displayName, email, spotifyId);
            user.setUsername(username);
            
            // Set random password (won't be used for OAuth logins)
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setRole(User.Role.ROLE_USER);
            user.setEnabled(true);
            
            user = userRepository.save(user);
            log.info("Created new user with ID: {} for Spotify user: {}", user.getId(), spotifyId);
        }

        return new CustomOAuth2User(oAuth2User, user);
    }

    private String generateUsername(String displayName, String email, String spotifyId) {
        String baseUsername;
        
        if (displayName != null && !displayName.isBlank()) {
            baseUsername = displayName.toLowerCase()
                    .replaceAll("[^a-z0-9]", "")
                    .substring(0, Math.min(displayName.length(), 20));
        } else if (email != null && !email.isBlank()) {
            baseUsername = email.split("@")[0]
                    .toLowerCase()
                    .replaceAll("[^a-z0-9]", "");
        } else {
            baseUsername = "spotify_" + spotifyId.substring(0, Math.min(10, spotifyId.length()));
        }

        // Ensure uniqueness
        String username = baseUsername;
        int counter = 1;
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }

        return username;
    }
}