package com.teamrhythm.concertwishlist.service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamrhythm.concertwishlist.dto.ArtistDto;

@Service
public class SpotifyService {
    
    private final WebClient webClient;
    private final String clientId;
    private final String clientSecret;
    private final String baseUrl;
    private final ObjectMapper objectMapper;

    public SpotifyService(WebClient.Builder webClientBuilder,
                         @Value("${spotify.client-id}") String clientId,
                         @Value("${spotify.client-secret}") String clientSecret,
                         @Value("${spotify.api.base-url}") String baseUrl) {
        this.webClient = webClientBuilder.build();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.baseUrl = baseUrl;
        this.objectMapper = new ObjectMapper();
    }

    private String getAccessToken() {
        String auth = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
        
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");

        String response = webClient.post()
                .uri("https://accounts.spotify.com/api/token")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + auth)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.get("access_token").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get Spotify access token", e);
        }
    }

    @Cacheable(value = "artistSearch", key = "#query")
    public List<ArtistDto> searchArtists(String query) {
        return searchArtists(query, 20);
    }

    public List<ArtistDto> searchArtists(String query, int limit) {
        String accessToken = getAccessToken();
        
        String response = webClient.get()
                .uri(baseUrl + "/search?q={query}&type=artist&limit={limit}", query, limit)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return parseSearchResponse(response);
    }

    @Cacheable(value = "artistDetails", key = "#artistId")
    public ArtistDto getArtist(String artistId) {
        String accessToken = getAccessToken();
        
        String response = webClient.get()
                .uri(baseUrl + "/artists/{id}", artistId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return parseArtistResponse(response);
    }

    private List<ArtistDto> parseSearchResponse(String response) {
        List<ArtistDto> artists = new ArrayList<>();
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            JsonNode artistsNode = jsonNode.get("artists").get("items");
            
            for (JsonNode artistNode : artistsNode) {
                artists.add(parseArtistNode(artistNode));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Spotify search response", e);
        }
        return artists;
    }

    private ArtistDto parseArtistResponse(String response) {
        try {
            JsonNode artistNode = objectMapper.readTree(response);
            return parseArtistNode(artistNode);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Spotify artist response", e);
        }
    }

    private ArtistDto parseArtistNode(JsonNode artistNode) {
        String id = artistNode.get("id").asText();
        String name = artistNode.get("name").asText();
        int popularity = artistNode.get("popularity").asInt();
        
        List<String> genres = new ArrayList<>();
        JsonNode genresNode = artistNode.get("genres");
        if (genresNode != null && genresNode.isArray()) {
            for (JsonNode genreNode : genresNode) {
                genres.add(genreNode.asText());
            }
        }
        
        String imageUrl = null;
        JsonNode imagesNode = artistNode.get("images");
        if (imagesNode != null && imagesNode.isArray() && imagesNode.size() > 0) {
            imageUrl = imagesNode.get(0).get("url").asText();
        }
        
        return new ArtistDto(id, name, genres, popularity, imageUrl);
    }
}