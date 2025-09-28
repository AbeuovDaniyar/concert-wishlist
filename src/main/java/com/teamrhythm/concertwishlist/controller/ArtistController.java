package com.teamrhythm.concertwishlist.controller;

import com.teamrhythm.concertwishlist.dto.ArtistDto;
import com.teamrhythm.concertwishlist.service.SpotifyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@Controller
@RequestMapping("/artists")
public class ArtistController {

    private static final Logger log = LoggerFactory.getLogger(ArtistController.class);

    private final SpotifyService spotifyService;

    @Autowired
    public ArtistController(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
    }

    @GetMapping
    public String artists() {
        log.debug("Rendering artist search page");
        return "artists";
    }

    @GetMapping("/search")
    public String searchArtists(@RequestParam String query, Model model) {
        log.info("Searching Spotify for artists with query '{}'", query);
        try {
            List<ArtistDto> artists = spotifyService.searchArtists(query);
            log.debug("Found {} artists for query '{}'", artists.size(), query);
            model.addAttribute("artists", artists);
            model.addAttribute("query", query);
            return "artists";
        } catch (RuntimeException e) {
            log.error("Failed to search artists for query '{}'", query, e);
            throw e;
        }
    }
}
