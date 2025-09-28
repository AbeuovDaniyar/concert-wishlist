package com.teamrhythm.concertwishlist.repository;

import com.teamrhythm.concertwishlist.entity.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {
    Optional<Artist> findBySpotifyArtistId(String spotifyArtistId);
    boolean existsBySpotifyArtistId(String spotifyArtistId);
}