package com.teamrhythm.concertwishlist.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class AttendedConcertDto {
    @NotNull
    private Long wishlistId;
    
    @NotNull
    private LocalDate concertDate;
    
    @NotBlank
    @Size(max = 255)
    private String venue;
    
    @Min(1)
    @Max(5)
    private Integer rating;
    
    private String memories;

    // Constructors
    public AttendedConcertDto() {}

    // Getters and Setters
    public Long getWishlistId() { return wishlistId; }
    public void setWishlistId(Long wishlistId) { this.wishlistId = wishlistId; }

    public LocalDate getConcertDate() { return concertDate; }
    public void setConcertDate(LocalDate concertDate) { this.concertDate = concertDate; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getMemories() { return memories; }
    public void setMemories(String memories) { this.memories = memories; }
}
