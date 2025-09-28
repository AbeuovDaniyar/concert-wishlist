package com.teamrhythm.concertwishlist.dto;

import java.util.List;

public class ArtistDto {
    private String id;
    private String name;
    private List<String> genres;
    private Integer popularity;
    private String imageUrl;

    // Constructors
    public ArtistDto() {}

    public ArtistDto(String id, String name, List<String> genres, Integer popularity, String imageUrl) {
        this.id = id;
        this.name = name;
        this.genres = genres;
        this.popularity = popularity;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }

    public Integer getPopularity() { return popularity; }
    public void setPopularity(Integer popularity) { this.popularity = popularity; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}