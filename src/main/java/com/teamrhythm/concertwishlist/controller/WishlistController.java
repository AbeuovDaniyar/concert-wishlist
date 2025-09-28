package com.teamrhythm.concertwishlist.controller;

import com.teamrhythm.concertwishlist.dto.WishlistCreateDto;
import com.teamrhythm.concertwishlist.entity.ConcertWishlist;
import com.teamrhythm.concertwishlist.entity.User;
import com.teamrhythm.concertwishlist.service.AuthenticatedUserService;
import com.teamrhythm.concertwishlist.service.WishlistService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/wishlist")
public class WishlistController {

    private static final Logger log = LoggerFactory.getLogger(WishlistController.class);

    private final WishlistService wishlistService;
    private final AuthenticatedUserService authenticatedUserService;

    @Autowired
    public WishlistController(WishlistService wishlistService, AuthenticatedUserService authenticatedUserService) {
        this.wishlistService = wishlistService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @GetMapping
    public String wishlist(Model model, Authentication authentication) {
        Optional<User> userOptional = authenticatedUserService.findUser(authentication);
        if (userOptional.isEmpty()) {
            log.warn("Unable to resolve user for wishlist page");
            return "redirect:/login";
        }

        Long userId = userOptional.get().getId();
        log.debug("Fetching wishlist for user {}", userId);
        List<ConcertWishlist> wishlist = wishlistService.getUserWishlist(userId);
        log.debug("Retrieved {} wishlist items for user {}", wishlist.size(), userId);
        model.addAttribute("wishlist", wishlist);
        return "wishlist";
    }

    @GetMapping("/add")
    public String addForm(@RequestParam String spotifyArtistId,
                         @RequestParam String artistName,
                         Model model) {
        log.debug("Rendering add wishlist form for artist {}", spotifyArtistId);
        WishlistCreateDto dto = new WishlistCreateDto();
        dto.setSpotifyArtistId(spotifyArtistId);
        model.addAttribute("wishlistDto", dto);
        model.addAttribute("artistName", artistName);
        return "add-wishlist";
    }

    @PostMapping("/add")
    public String addToWishlist(@Valid @ModelAttribute("wishlistDto") WishlistCreateDto dto,
                               BindingResult result,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            log.warn("Validation errors while adding artist {} to wishlist", dto.getSpotifyArtistId());
            return "add-wishlist";
        }

        Optional<User> userOptional = authenticatedUserService.findUser(authentication);
        if (userOptional.isEmpty()) {
            log.warn("Unable to resolve user while adding to wishlist");
            redirectAttributes.addFlashAttribute("errorMessage", "Please sign in again to manage your wishlist.");
            return "redirect:/login";
        }

        Long userId = userOptional.get().getId();
        try {
            log.info("Adding artist {} to wishlist for user {}", dto.getSpotifyArtistId(), userId);
            wishlistService.create(userId, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Artist added to wishlist successfully!");
            log.info("Artist {} added to wishlist for user {}", dto.getSpotifyArtistId(), userId);
        } catch (RuntimeException e) {
            log.warn("Failed to add artist {} to wishlist for user {}", dto.getSpotifyArtistId(), userId, e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/wishlist";
    }

    @PostMapping("/delete/{id}")
    public String deleteFromWishlist(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            log.info("Deleting wishlist item {}", id);
            wishlistService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Item removed from wishlist.");
            log.info("Wishlist item {} deleted", id);
        } catch (RuntimeException e) {
            log.warn("Failed to delete wishlist item {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/wishlist";
    }
}
