package com.teamrhythm.concertwishlist.service;

import com.teamrhythm.concertwishlist.dto.ArtistDto;
import com.teamrhythm.concertwishlist.dto.WishlistCreateDto;
import com.teamrhythm.concertwishlist.entity.Artist;
import com.teamrhythm.concertwishlist.entity.ConcertWishlist;
import com.teamrhythm.concertwishlist.entity.User;
import com.teamrhythm.concertwishlist.repository.ArtistRepository;
import com.teamrhythm.concertwishlist.repository.ConcertWishlistRepository;
import com.teamrhythm.concertwishlist.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class WishlistService {

    private static final Logger log = LoggerFactory.getLogger(WishlistService.class);

    private final ConcertWishlistRepository wishlistRepository;
    private final ArtistRepository artistRepository;
    private final UserRepository userRepository;
    private final SpotifyService spotifyService;

    @Autowired
    public WishlistService(ConcertWishlistRepository wishlistRepository,
                          ArtistRepository artistRepository,
                          UserRepository userRepository,
                          SpotifyService spotifyService) {
        this.wishlistRepository = wishlistRepository;
        this.artistRepository = artistRepository;
        this.userRepository = userRepository;
        this.spotifyService = spotifyService;
    }

    @Transactional
    public ConcertWishlist create(Long userId, WishlistCreateDto dto) {
        log.info("Creating wishlist entry for user {} and artist {}", userId, dto.getSpotifyArtistId());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User {} not found while creating wishlist entry", userId);
                    return new RuntimeException("User not found");
                });

        Artist artist = getOrCreateArtist(dto.getSpotifyArtistId());
        Optional<ConcertWishlist> existing = wishlistRepository
                .findByUserIdAndArtistIdAndCity(userId, artist.getId(), dto.getCity());

        if (existing.isPresent()) {
            log.warn("Wishlist entry already exists for user {} artist {} city {}", userId, artist.getSpotifyArtistId(), dto.getCity());
            throw new RuntimeException("This artist is already in your wishlist for this city");
        }

        ConcertWishlist wishlist = new ConcertWishlist();
        wishlist.setUser(user);
        wishlist.setArtist(artist);
        wishlist.setCity(dto.getCity());
        wishlist.setVenue(dto.getVenue());
        wishlist.setPriority(dto.getPriority());
        wishlist.setTargetDate(dto.getTargetDate());
        wishlist.setNotes(dto.getNotes());

        ConcertWishlist saved = wishlistRepository.save(wishlist);
        log.info("Created wishlist entry {} for user {}", saved.getId(), userId);
        return saved;
    }

    @Transactional
    public ConcertWishlist update(Long wishlistId, WishlistCreateDto dto) {
        log.info("Updating wishlist entry {}", wishlistId);
        ConcertWishlist wishlist = wishlistRepository.findById(wishlistId)
                .orElseThrow(() -> {
                    log.warn("Wishlist item {} not found for update", wishlistId);
                    return new RuntimeException("Wishlist item not found");
                });

        wishlist.setCity(dto.getCity());
        wishlist.setVenue(dto.getVenue());
        wishlist.setPriority(dto.getPriority());
        wishlist.setTargetDate(dto.getTargetDate());
        wishlist.setNotes(dto.getNotes());

        ConcertWishlist saved = wishlistRepository.save(wishlist);
        log.info("Updated wishlist entry {}", saved.getId());
        return saved;
    }

    @Transactional
    public void delete(Long wishlistId) {
        log.info("Deleting wishlist entry {}", wishlistId);
        if (!wishlistRepository.existsById(wishlistId)) {
            log.warn("Wishlist item {} not found for deletion", wishlistId);
            throw new RuntimeException("Wishlist item not found");
        }
        wishlistRepository.deleteById(wishlistId);
        log.info("Deleted wishlist entry {}", wishlistId);
    }

    public List<ConcertWishlist> getUserWishlist(Long userId) {
        log.debug("Fetching pending wishlist items for user {}", userId);
        return wishlistRepository.findByUserIdAndStatusOrderByPriorityAndCreatedAt(
                userId, ConcertWishlist.Status.PENDING);
    }

    public List<ConcertWishlist> getUserWishlistSortedByPriority(Long userId) {
        log.debug("Fetching wishlist items for user {} sorted by priority", userId);
        return wishlistRepository.findByUserIdOrderByPriorityDesc(userId);
    }

    public List<ConcertWishlist> getUserWishlistSortedByDate(Long userId) {
        log.debug("Fetching wishlist items for user {} sorted by date", userId);
        return wishlistRepository.findByUserIdOrderByTargetDateAsc(userId);
    }

    private Artist getOrCreateArtist(String spotifyArtistId) {
        return artistRepository.findBySpotifyArtistId(spotifyArtistId)
                .map(artist -> {
                    log.debug("Found existing artist {} in repository", spotifyArtistId);
                    return artist;
                })
                .orElseGet(() -> {
                    log.info("Artist {} not found locally. Fetching from Spotify", spotifyArtistId);
                    ArtistDto artistDto = spotifyService.getArtist(spotifyArtistId);
                    Artist artist = new Artist();
                    artist.setSpotifyArtistId(artistDto.getId());
                    artist.setName(artistDto.getName());
                    artist.setPopularity(artistDto.getPopularity());
                    artist.setImageUrl(artistDto.getImageUrl());
                    Artist saved = artistRepository.save(artist);
                    log.info("Created artist {} with repository id {}", spotifyArtistId, saved.getId());
                    return saved;
                });
    }
}
