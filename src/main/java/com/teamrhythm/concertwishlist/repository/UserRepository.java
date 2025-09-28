package com.teamrhythm.concertwishlist.repository;

import com.teamrhythm.concertwishlist.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findBySpotifyUserId(String spotifyUserId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}