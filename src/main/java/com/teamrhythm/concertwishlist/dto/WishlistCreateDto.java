package com.teamrhythm.concertwishlist.dto;

import com.teamrhythm.concertwishlist.entity.ConcertWishlist;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class WishlistCreateDto {
    @NotBlank
    private String spotifyArtistId;
    
    @NotBlank
    @Size(max = 100)
    private String city;
    
    @Size(max = 255)
    private String venue;
    
    @NotNull
    private ConcertWishlist.Priority priority = ConcertWishlist.Priority.MEDIUM;
    
    private LocalDate targetDate;
    
    private String notes;

    // Constructors
    public WishlistCreateDto() {}

    // Getters and Setters
    public String getSpotifyArtistId() { return spotifyArtistId; }
    public void setSpotifyArtistId(String spotifyArtistId) { this.spotifyArtistId = spotifyArtistId; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public ConcertWishlist.Priority getPriority() { return priority; }
    public void setPriority(ConcertWishlist.Priority priority) { this.priority = priority; }

    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}