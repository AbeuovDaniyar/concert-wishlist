package com.teamrhythm.concertwishlist.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "artists")
public class Artist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(name = "spotify_artist_id", unique = true, nullable = false)
    private String spotifyArtistId;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String name;

    @Column
    private Integer popularity = 0;

    @Size(max = 500)
    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "artist", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ConcertWishlist> wishlistEntries;

    @OneToMany(mappedBy = "artist", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AttendedConcert> attendedConcerts;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSpotifyArtistId() { return spotifyArtistId; }
    public void setSpotifyArtistId(String spotifyArtistId) { this.spotifyArtistId = spotifyArtistId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getPopularity() { return popularity; }
    public void setPopularity(Integer popularity) { this.popularity = popularity; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<ConcertWishlist> getWishlistEntries() { return wishlistEntries; }
    public void setWishlistEntries(List<ConcertWishlist> wishlistEntries) { this.wishlistEntries = wishlistEntries; }

    public List<AttendedConcert> getAttendedConcerts() { return attendedConcerts; }
    public void setAttendedConcerts(List<AttendedConcert> attendedConcerts) { this.attendedConcerts = attendedConcerts; }
}